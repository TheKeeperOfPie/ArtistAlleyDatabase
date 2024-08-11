package com.thekeeperofpie.artistalleydatabase.anilist

import android.app.Application
import androidx.security.crypto.MasterKey
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApiWrapper
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.AppJson
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.FeatureOverrideProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Module
@InstallIn(SingletonComponent::class)
class AniListHiltModule {
    companion object {
        private const val MEMORY_CACHE_BYTE_SIZE = 100 * 1024 * 1024 // 100 MB
    }

    @Provides
    @ElementsIntoSet
    fun bindEmptyApolloHttpInterceptorsSet(): Set<HttpInterceptor> = emptySet()

    @Named("AniList")
    @Singleton
    @Provides
    fun provideAniListApolloClient(
        application: Application,
        masterKey: MasterKey,
        networkSettings: NetworkSettings,
        okHttpClient: OkHttpClient,
        apolloHttpInterceptors: @JvmSuppressWildcards Set<HttpInterceptor>,
    ): ApolloClient {
        val memoryThenDiskCache = MemoryCacheFactory(
            maxSizeBytes = MEMORY_CACHE_BYTE_SIZE,
            expireAfterMillis = 10.minutes.inWholeMilliseconds,
        ).apply {
            if (networkSettings.enableNetworkCaching.value) {
                chain(
                    SqlNormalizedCacheFactory(
                        application,
                        "apollo.db",
                        useNoBackupDirectory = true,
                    )
                )
            }
        }

        @OptIn(ApolloExperimental::class)
        return ApolloClient.Builder()
            .serverUrl(AniListUtils.GRAPHQL_API_URL)
            .httpEngine(DefaultHttpEngine(okHttpClient))
            .addLoggingInterceptors("AniListApi", networkSettings)
            .normalizedCache(memoryThenDiskCache, writeToCacheAsynchronously = true)
            .apply { apolloHttpInterceptors.forEach(::addHttpInterceptor) }
            .build()
    }

    @Singleton
    @Provides
    fun provideAniListOAuthStore(
        scopedApplication: ScopedApplication,
        masterKey: MasterKey,
        aniListSettings: AniListSettings,
    ) = AniListOAuthStore(scopedApplication, masterKey, aniListSettings)

    @Singleton
    @Provides
    fun provideAniListApi(
        application: ScopedApplication,
        @Named("AniList") apolloClient: ApolloClient,
    ) = AniListApi(application, apolloClient)

    @Singleton
    @Provides
    fun provideAuthedAniListApi(
        scopedApplication: ScopedApplication,
        aniListOAuthStore: AniListOAuthStore,
        aniListSettings: AniListSettings,
        okHttpClient: OkHttpClient,
        @Named("AniList") apolloClient: ApolloClient,
        featureOverrideProvider: FeatureOverrideProvider,
    ) = if (featureOverrideProvider.isReleaseBuild) {
        AuthedAniListApiWrapper(
            scopedApplication,
            aniListOAuthStore,
            aniListSettings,
            okHttpClient,
            apolloClient,
        )
    } else {
        AuthedAniListApi(
            scopedApplication,
            aniListOAuthStore,
            aniListSettings,
            okHttpClient,
            apolloClient,
        )
    }

    @Singleton
    @Provides
    fun provideAniListDataConverter(aniListJson: AniListJson) = AniListDataConverter(aniListJson)

    @Singleton
    @Provides
    fun provideAniListAutocompleter(
        aniListJson: AniListJson,
        aniListApi: AniListApi,
        characterRepository: CharacterRepository,
        mediaRepository: MediaRepository,
        aniListDataConverter: AniListDataConverter,
    ) = AniListAutocompleter(
        aniListJson,
        aniListApi,
        characterRepository,
        mediaRepository,
        aniListDataConverter
    )

    @Singleton
    @Provides
    fun provideMediaEntryDao(database: AniListDatabase) = database.mediaEntryDao()

    @Singleton
    @Provides
    fun provideMediaRepository(
        application: ScopedApplication,
        mediaEntryDao: MediaEntryDao,
        aniListApi: AniListApi,
    ) = MediaRepository(application, mediaEntryDao, aniListApi)

    @Singleton
    @Provides
    fun provideCharacterEntryDao(database: AniListDatabase) = database.characterEntryDao()

    @Singleton
    @Provides
    fun provideCharacterRepository(
        application: ScopedApplication,
        appJson: AppJson,
        characterEntryDao: CharacterEntryDao,
        aniListApi: AniListApi,
    ) = CharacterRepository(application, appJson, characterEntryDao, aniListApi)

    @Singleton
    @Provides
    @StringKey(AniListUtils.GRAPHQL_API_HOST)
    @IntoMap
    fun provideAniListNetworkAuthProvider(
        oAuthStore: AniListOAuthStore,
    ): NetworkAuthProvider = oAuthStore
}
