package com.thekeeperofpie.artistalleydatabase.anilist

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.apollographql.apollo3.network.http.KtorHttpEngine
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthViewModel
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApiWrapper
import com.thekeeperofpie.artistalleydatabase.inject.Named
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import kotlin.time.Duration.Companion.minutes

interface AniListComponent : AniListSqlCacheComponent {
    companion object {
        private const val MEMORY_CACHE_BYTE_SIZE = 100 * 1024 * 1024 // 100 MB
    }

    val aniListOAuthViewModel: (text: String?) -> AniListOAuthViewModel

    @Provides
    @IntoSet
    fun bindEmptyApolloHttpInterceptorsSet(): Set<HttpInterceptor> = emptySet()

    @OptIn(ApolloExperimental::class)
    @Named("AniList")
    @SingletonScope
    @Provides
    fun provideAniListApolloClient(
        networkSettings: NetworkSettings,
        httpClient: HttpClient,
        apolloHttpInterceptors: Set<HttpInterceptor> = emptySet(),
        sqlCache: AniListSqlCache? = null,
    ): ApolloClient {
        val memoryThenDiskCache = MemoryCacheFactory(
            maxSizeBytes = MEMORY_CACHE_BYTE_SIZE,
            expireAfterMillis = 10.minutes.inWholeMilliseconds,
        ).apply {
            // TODO: Re-enable caching
            val cache = sqlCache?.cache
            if (cache != null && networkSettings.enableNetworkCaching.value) {
                chain(cache)
            }
        }

        return ApolloClient.Builder()
            .serverUrl(AniListUtils.GRAPHQL_API_URL)
            .httpEngine(KtorHttpEngine(httpClient))
            .addLoggingInterceptors("AniListApi", networkSettings)
            .normalizedCache(memoryThenDiskCache, writeToCacheAsynchronously = true)
            .apply { apolloHttpInterceptors.forEach(::addHttpInterceptor) }
            .addHttpInterceptor(object : HttpInterceptor {
                override suspend fun intercept(
                    request: HttpRequest,
                    chain: HttpInterceptorChain,
                ): HttpResponse {
                    val initialResponse = chain.proceed(request)
                    // Coerce all responses to 200 so that error parsing works as expected
                    // https://api.akeneo.com/graphql/error-codes.html#status-and-error-codes
                    return HttpResponse.Builder(200)
                        .apply {
                            addHeaders(initialResponse.headers)
                            initialResponse.body?.let(::body)
                        }
                        .build()
                }
            })
            .build()
    }

    @SingletonScope
    @Provides
    fun provideAuthedAniListApi(
        scope: ApplicationScope,
        aniListOAuthStore: AniListOAuthStore,
        aniListSettings: AniListSettings,
        httpClient: HttpClient,
        @Named("AniList") apolloClient: ApolloClient,
        featureOverrideProvider: FeatureOverrideProvider,
        appFileSystem: AppFileSystem,
        json: Json,
    ) = if (featureOverrideProvider.isReleaseBuild) {
        AuthedAniListApiWrapper(
            scope = scope,
            oAuthStore = aniListOAuthStore,
            aniListSettings = aniListSettings,
            httpClient = httpClient,
            apolloClient = apolloClient,
            appFileSystem = appFileSystem,
            json = json,
        )
    } else {
        AuthedAniListApi(
            scope = scope,
            oAuthStore = aniListOAuthStore,
            aniListSettings = aniListSettings,
            httpClient = httpClient,
            apolloClient = apolloClient,
            appFileSystem = appFileSystem,
            json = json,
        )
    }

    @SingletonScope
    @Provides
    fun provideMediaEntryDao(database: AniListDatabase) = database.mediaEntryDao()

    @SingletonScope
    @Provides
    fun provideCharacterEntryDao(database: AniListDatabase) = database.characterEntryDao()

    @SingletonScope
    @Provides
    fun provideAniListNetworkAuthProvider(oAuthStore: AniListOAuthStore): NetworkAuthProvider =
        oAuthStore
}
