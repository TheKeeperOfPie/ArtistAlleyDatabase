package com.thekeeperofpie.artistalleydatabase.apollo.utils

import co.touchlab.kermit.Logger
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.thekeeperofpie.artistalleydatabase.inject.Named
import com.thekeeperofpie.artistalleydatabase.utils.debug
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.deleteRecursively
import com.thekeeperofpie.artistalleydatabase.utils.io.resolve
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@OptIn(
    ExperimentalSerializationApi::class,
    InternalSerializationApi::class,
    ApolloExperimental::class
)
@Inject
class ApolloCache(
    scope: ApplicationScope,
    @Named("AniList") private val apolloClient: ApolloClient,
    private val appFileSystem: AppFileSystem,
    private val json: Json,
) {
    companion object {
        private const val TAG = "ApolloCache"
    }

    private val cacheDir = appFileSystem.cachePath("apolloAniListQueries").also {
        appFileSystem.createDirectories(it)
    }

    init {
        scope.launch(PlatformDispatchers.IO) {
            try {
                val list = appFileSystem.list(cacheDir)
                    .sortedBy { appFileSystem.lastModifiedTime(it) }

                val maxSize = 4 * 1024 * 1024
                var accumulatedSize = 0L
                var countToKeep = 0
                list.forEach {
                    if (accumulatedSize < maxSize) {
                        accumulatedSize += appFileSystem.metadataOrNull(it)?.size ?: 0L
                        countToKeep++
                    }
                }

                list.drop(countToKeep).forEach {
                    appFileSystem.deleteRecursively(it)
                }
            } catch (t: Throwable) {
                Logger.debug(TAG, t) { "Failed to prune cache" }
            }
        }
    }

    private fun <QueryType : Query<*>> hash(queryType: KClass<QueryType>, query: QueryType) =
        json.encodeToString(queryType.serializer(), query).hashCode()

    data class CacheFileName(
        val queryName: String,
        val hash: Int,
    ) {
        val fileName = "$queryName-$hash.json"
    }

    // TODO: Make all of these methods return LoadingResult
    suspend inline fun <reified DataType : Query.Data, reified QueryType : Query<DataType>> query(
        query: QueryType,
        cacheTime: Duration,
        skipCache: Boolean = false,
    ) = query(query, QueryType::class, DataType::class, skipCache, cacheTime)

    suspend inline fun <reified DataType : Query.Data, reified QueryType : Query<DataType>> query(
        query: QueryType,
        skipCache: Boolean = false,
        noinline cacheTime: (DataType) -> Duration,
    ) = query(query, QueryType::class, DataType::class, skipCache, cacheTime)

    suspend fun <DataType : Query.Data, QueryType : Query<DataType>> query(
        query: QueryType,
        queryType: KClass<QueryType>,
        dataType: KClass<DataType>,
        skipCache: Boolean,
        cacheTime: (DataType) -> Duration,
    ) = queryInternal(
        query = query,
        queryType = queryType,
        dataType = dataType,
        skipCache = skipCache,
        cacheDuration = cacheTime,
        cacheEntry = cached@{
            val entry = cachedEntry(dataType, it)
            val cacheExpiry = cacheTime(entry.data)
            val stillValid = entry.created.plus(cacheExpiry) > Clock.System.now()
            entry.data.takeIf { stillValid }
        }
    )

    suspend fun <DataType : Query.Data, QueryType : Query<DataType>> query(
        query: QueryType,
        queryType: KClass<QueryType>,
        dataType: KClass<DataType>,
        skipCache: Boolean,
        cacheTime: Duration,
    ) = queryInternal(
        query = query,
        queryType = queryType,
        dataType = dataType,
        skipCache = skipCache,
        cacheDuration = { cacheTime },
        cacheEntry = cached@{
            val lastModifiedTime = appFileSystem.lastModifiedTime(it)
            if (lastModifiedTime.plus(cacheTime) < Clock.System.now()) return@cached null
            cachedEntry(dataType, it).data
        }
    )

    /**
     * Delete a cache entry immediately.
     * @return true if something was deleted
     */
    inline fun <reified QueryType : Query<*>> evict(query: QueryType) =
        evict(query, QueryType::class)

    fun <QueryType : Query<*>> evict(query: QueryType, queryType: KClass<QueryType>): Boolean {
        try {
            val queryName = query.name()
            val hash = hash(queryType, query)
            val cacheFileName = CacheFileName(queryName, hash)
            val cachedPath = appFileSystem.list(cacheDir).find { it.name == cacheFileName.fileName }
            if (cachedPath != null) {
                appFileSystem.deleteRecursively(cachedPath)
                return true
            }
        } catch (t: Throwable) {
            Logger.debug(TAG, t) { "Failed to read cache" }
        }
        return false
    }

    private fun <DataType : Query.Data> cachedEntry(dataType: KClass<DataType>, path: Path) =
        appFileSystem.source(path).buffered()
            .use { json.decodeFromSource(CacheEntry.serializer(dataType.serializer()), it) }
            .also { Logger.debug(TAG) { "Cached value: $it" } }

    private suspend fun <DataType : Query.Data, QueryType : Query<DataType>> queryInternal(
        query: QueryType,
        queryType: KClass<QueryType>,
        dataType: KClass<DataType>,
        skipCache: Boolean,
        cacheDuration: (DataType) -> Duration,
        cacheEntry: (cachedPath: Path) -> DataType?,
    ): DataType {
        var cacheFileName: CacheFileName? = null
        var cachedPath: Path? = null
        try {
            val queryName = query.name()
            val hash = hash(queryType, query)
            cacheFileName = CacheFileName(queryName, hash)
            cachedPath = appFileSystem.list(cacheDir).find { it.name == cacheFileName.fileName }
            if (!skipCache && cachedPath != null && appFileSystem.exists(cachedPath)) {
                val entry = cacheEntry(cachedPath)
                if (entry != null) {
                    return entry
                }
            }
        } catch (t: Throwable) {
            Logger.debug(TAG, t) { "Failed to read cache" }
        }

        if (skipCache) {
            Logger.debug(TAG) { "Cached skipped: $cacheFileName, $query" }
        } else {
            Logger.debug(TAG) { "Cached missed: $cacheFileName, $query" }
        }
        return apolloClient.query(query)
            .fetchPolicy(if (skipCache) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst)
            .execute()
            .dataOrThrow()
            .also { result ->
                if (cacheFileName == null) return@also
                try {
                    val outputPath = cachedPath ?: cacheDir.resolve(cacheFileName.fileName)
                    val newExpiry = cacheDuration(result)
                    val newEntry = CacheEntry(expiryAtCreate = newExpiry, data = result)
                    appFileSystem.sink(outputPath).buffered().use {
                        json.encodeToSink(
                            serializer = CacheEntry.serializer(dataType.serializer()),
                            value = newEntry,
                            sink = it,
                        )
                    }
                    Logger.debug(TAG) { "Cache written: $cacheFileName, $newEntry" }
                } catch (t: Throwable) {
                    Logger.debug(TAG, t) { "Failed to write cache" }
                }
            }
    }

    @Serializable
    data class CacheEntry<Data : Query.Data>(
        val expiryAtCreate: Duration? = null,
        val created: Instant = Clock.System.now(),
        val data: Data,
    )
}
