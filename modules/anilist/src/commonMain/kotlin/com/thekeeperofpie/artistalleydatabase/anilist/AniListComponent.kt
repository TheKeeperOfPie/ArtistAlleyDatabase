package com.thekeeperofpie.artistalleydatabase.anilist

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.KtorHttpEngine
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthViewModel
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApiWrapper
import com.thekeeperofpie.artistalleydatabase.inject.Named
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import io.ktor.client.HttpClient
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
        apolloHttpInterceptors: Set<HttpInterceptor>,
        sqlCache: AniListSqlCache,
    ): ApolloClient {
        val memoryThenDiskCache = MemoryCacheFactory(
            maxSizeBytes = MEMORY_CACHE_BYTE_SIZE,
            expireAfterMillis = 10.minutes.inWholeMilliseconds,
        ).apply {
            // TODO: Re-enable caching
            val cache = sqlCache.cache
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
    ) = if (featureOverrideProvider.isReleaseBuild) {
        AuthedAniListApiWrapper(
            scope,
            aniListOAuthStore,
            aniListSettings,
            httpClient,
            apolloClient,
        )
    } else {
        AuthedAniListApi(
            scope,
            aniListOAuthStore,
            aniListSettings,
            httpClient,
            apolloClient,
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
