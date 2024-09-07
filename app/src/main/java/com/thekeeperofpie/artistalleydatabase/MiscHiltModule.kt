package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppMetadataProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.utils_room.DatabaseSyncer
import com.thekeeperofpie.artistalleydatabase.utils_room.Exporter
import com.thekeeperofpie.artistalleydatabase.utils_room.Importer
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
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
    @Singleton
    fun provideApplicationComponent(
        application: Application,
        masterKey: MasterKey,
        applicationScope: ApplicationScope,
        vgmdbDatabase: VgmdbDatabase,
        json: Json,
        musicalArtistDatabase: MusicalArtistDatabase,
        aniListDatabase: AniListDatabase,
        featureOverrideProvider: FeatureOverrideProvider,
        appFileSystem: AppFileSystem,
        cdEntryDatabase: CdEntryDatabase,
        artEntryDatabase: ArtEntryDatabase,
        animeDatabase: AnimeDatabase,
        appMetadataProvider: AppMetadataProvider,
    ) = ApplicationComponent::class.create(
        application = application,
        masterKey = masterKey,
        applicationScope = applicationScope,
        vgmdbDatabase = vgmdbDatabase,
        json = json,
        musicalArtistDatabase = musicalArtistDatabase,
        aniListDatabase = aniListDatabase,
        featureOverrideProvider = featureOverrideProvider,
        appFileSystem = appFileSystem,
        cdEntryDatabase = cdEntryDatabase,
        artEntryDatabase = artEntryDatabase,
        animeDatabase = animeDatabase,
        appMetadataProvider = appMetadataProvider,
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

    @Provides
    fun provideNetworkAuthProvider(aniListOAuthStore: AniListOAuthStore): NetworkAuthProvider =
        aniListOAuthStore

    @Provides
    fun provideAniListAutocompleter(applicationComponent: ApplicationComponent) =
        applicationComponent.aniListAutocompleter

    @Provides
    fun provideCdEntryNavigator(applicationComponent: ApplicationComponent) =
        applicationComponent.cdEntryNavigator

    @Provides
    fun provideCdEntryDao(applicationComponent: ApplicationComponent) =
        applicationComponent.cdEntryDao

    @Provides
    @ElementsIntoSet
    fun provideImporters(applicationComponent: ApplicationComponent): Set<Importer> =
        applicationComponent.importers

    @Provides
    @ElementsIntoSet
    fun provideExporters(applicationComponent: ApplicationComponent): Set<Exporter> =
        applicationComponent.exporters

    @Provides
    @ElementsIntoSet
    fun provideDatabaseSyncer(applicationComponent: ApplicationComponent): Set<DatabaseSyncer> =
        applicationComponent.databaseSyncers

    @Provides
    fun provideArtEntryNavigator(applicationComponent: ApplicationComponent) =
        applicationComponent.artEntryNavigator

    @Provides
    fun provideArtEntryDetailsDao(applicationComponent: ApplicationComponent) =
        applicationComponent.artEntryDetailsDao

    @Provides
    fun provideHistoryController(applicationComponent: ApplicationComponent) =
        applicationComponent.historyController

    @Provides
    fun provideIgnoreController(applicationComponent: ApplicationComponent) =
        applicationComponent.ignoreController

    @Provides
    fun provideMonetizationController(applicationComponent: ApplicationComponent) =
        applicationComponent.monetizationController

    @Provides
    fun provideNetworkSettings(applicationComponent: ApplicationComponent) =
        applicationComponent.networkSettings

    @Provides
    fun provideAniListSettings(applicationComponent: ApplicationComponent) =
        applicationComponent.aniListSettings

    @Provides
    fun provideSettingsProvider(applicationComponent: ApplicationComponent) =
        applicationComponent.settingsProvider

    @Provides
    fun providePlatformOAuthStore(applicationComponent: ApplicationComponent) =
        applicationComponent.platformOAuthStore
}
