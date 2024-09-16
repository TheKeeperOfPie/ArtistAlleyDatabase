package com.thekeeperofpie.artistalleydatabase.apollo.utils

import co.touchlab.kermit.Logger
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.api.Query
import com.thekeeperofpie.artistalleydatabase.utils.debug
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.deleteRecursively
import com.thekeeperofpie.artistalleydatabase.utils.io.resolve
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.time.Duration

@OptIn(
    ExperimentalSerializationApi::class,
    InternalSerializationApi::class,
    ApolloExperimental::class
)
class ApolloCache(
    scope: ApplicationScope,
    private val apolloClient: ApolloClient,
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
        scope.launch(Dispatchers.IO) {
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

    private data class CacheFileName(
        val operationId: String,
        val hash: Int,
    ) {
        val fileName = "$operationId-$hash.json"
    }

    suspend inline fun <reified DataType : Query.Data, reified QueryType : Query<DataType>> query(
        query: QueryType,
        cacheTime: Duration,
    ) = query(query, QueryType::class, DataType::class, cacheTime)

    suspend fun <DataType : Query.Data, QueryType : Query<DataType>> query(
        query: QueryType,
        queryType: KClass<QueryType>,
        dataType: KClass<DataType>,
        cacheTime: Duration,
    ): DataType {
        var cacheFileName: CacheFileName? = null
        var cachedPath: Path? = null
        try {
            val operationId = query.id().hashCode().toString()
            val hash = hash(queryType, query)
            cacheFileName = CacheFileName(operationId, hash)
            cachedPath = appFileSystem.list(cacheDir).find { it.name == cacheFileName.fileName }
            if (cachedPath != null && appFileSystem.exists(cachedPath)) {
                val lastModifiedTime = appFileSystem.lastModifiedTime(cachedPath)
                if (lastModifiedTime.plus(cacheTime) > Clock.System.now()) {
                    return appFileSystem.source(cachedPath).buffered().use {
                        json.decodeFromSource(dataType.serializer(), it)
                    }.also {
                        Logger.debug(TAG) { "Cached value: $it" }
                    }
                }
            }
        } catch (t: Throwable) {
            Logger.debug(TAG, t) { "Failed to read cache" }
        }

        Logger.debug(TAG) { "Cached missed: $cacheFileName, $query" }
        return apolloClient.query(query)
            .execute()
            .dataOrThrow()
            .also { result ->
                if (cacheFileName == null) return@also
                try {
                    val outputPath = cachedPath ?: cacheDir.resolve(cacheFileName.fileName)
                    appFileSystem.sink(outputPath).buffered().use {
                        json.encodeToSink(dataType.serializer(), result, it)
                    }
                } catch (t: Throwable) {
                    Logger.debug(TAG, t) { "Failed to write cache" }
                }
            }
    }
}
