package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.security.crypto.MasterKey
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.PlatformOAuthStore
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.image.crop.CropController
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Dumping ground for dependencies which need to eventually be migrated to multiplatform.
 */
@Module
@InstallIn(SingletonComponent::class)
class MiscHiltModule {

    @Provides
    fun providesBrowseViewModel(tabViewModels: Set<@JvmSuppressWildcards BrowseTabViewModel>) =
        BrowseViewModel(tabViewModels)

    @Provides
    fun provideCropController(
        scope: CoroutineScope,
        application: Application,
        appFileSystem: AppFileSystem,
        settings: CropSettings,
    ) = CropController(scope, application, appFileSystem, settings)

    @Provides
    @Singleton
    fun provideApplicationComponent(
        application: Application,
        networkClient: NetworkClient,
        applicationScope: ApplicationScope,
        httpClient: HttpClient,
        vgmdbDatabase: VgmdbDatabase,
        json: Json,
        musicalArtistDatabase: MusicalArtistDatabase,
        masterKey: MasterKey,
        aniListSettings: AniListSettings,
        aniListDatabase: AniListDatabase,
        networkSettings: NetworkSettings,
        httpInterceptors: @JvmSuppressWildcards Set<HttpInterceptor>,
        featureOverrideProvider: FeatureOverrideProvider,
        aniListOAuthStore: AniListOAuthStore,
        platformOAuthStore: PlatformOAuthStore,
    ) = ApplicationComponent::class.create(
        application = application,
        networkClient = networkClient,
        applicationScope = applicationScope,
        httpClient = httpClient,
        vgmdbDatabase = vgmdbDatabase,
        json = json,
        musicalArtistDatabase = musicalArtistDatabase,
        masterKey = masterKey,
        aniListSettings = aniListSettings,
        aniListDatabase = aniListDatabase,
        networkSettings = networkSettings,
        httpInterceptors = httpInterceptors,
        featureOverrideProvider = featureOverrideProvider,
        aniListOAuthStore = aniListOAuthStore,
        platformOAuthStore = platformOAuthStore,
    )

    @Provides
    fun provideJson(appJson: AppJson) = appJson.json

    @Provides
    fun provideArtistRepository(applicationComponent: ApplicationComponent) =
        applicationComponent.artistRepository

    @Provides
    fun provideAlbumRepository(applicationComponent: ApplicationComponent) =
        applicationComponent.albumRepository

    @Provides
    fun provideAlbumEntryDao(applicationComponent: ApplicationComponent) =
        applicationComponent.albumEntryDao

    @Provides
    fun provideVgmdbDataConverter(applicationComponent: ApplicationComponent) =
        applicationComponent.vgmdbDataConverter

    @Provides
    fun provideVgmdbArtistDao(applicationComponent: ApplicationComponent) =
        applicationComponent.vgmdbArtistDao

    @Provides
    fun provideVgmdbApi(applicationComponent: ApplicationComponent) = applicationComponent.vgmdbApi

    @Provides
    fun provideVgmdbAutocompleter(applicationComponent: ApplicationComponent) =
        applicationComponent.vgmdbAutocompleter

    @Provides
    fun provideMusicalArtistDao(applicationComponent: ApplicationComponent) =
        applicationComponent.musicalArtistDao

    @Provides
    fun provideDataConverter(applicationComponent: ApplicationComponent) =
        applicationComponent.dataConverter

    @Provides
    fun provideCharacterRepository(applicationComponent: ApplicationComponent) =
        applicationComponent.characterRepository

    @Provides
    fun provideMediaRepository(applicationComponent: ApplicationComponent) =
        applicationComponent.mediaRepository

    @Provides
    fun provideCharacterEntryDao(applicationComponent: ApplicationComponent) =
        applicationComponent.characterEntryDao

    @Provides
    fun provideMediaEntryDao(applicationComponent: ApplicationComponent) =
        applicationComponent.mediaEntryDao

    @Provides
    fun provideAniListDataConverter(applicationComponent: ApplicationComponent) =
        applicationComponent.aniListDataConverter

    @Provides
    fun provideAuthedAniListApi(applicationComponent: ApplicationComponent) =
        applicationComponent.authedAniListApi

    // TODO: Move this out of Dagger
    @Singleton
    @Provides
    fun providePlatformOAuthStore(application: Application, masterKey: MasterKey) =
        PlatformOAuthStore(application, masterKey)

    @Singleton
    @Provides
    fun provideAniListOAuthStore(
        scope: ApplicationScope,
        platformOAuthStore: PlatformOAuthStore,
        aniListSettings: AniListSettings,
    ) = AniListOAuthStore(scope, platformOAuthStore, aniListSettings)

    @Provides
    fun provideNetworkAuthProvider(aniListOAuthStore: AniListOAuthStore): NetworkAuthProvider =
        aniListOAuthStore

    @Provides
    fun provideAniListAutocompleter(applicationComponent: ApplicationComponent) =
        applicationComponent.aniListAutocompleter
}
