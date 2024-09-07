package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.navigation.NavType
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListComponent
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.PlatformOAuthStore
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeComponent
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryComponent
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.browse.BrowseComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.media.MediaPlayer
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistComponent
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.settings.SettingsComponent
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppMetadataProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_room.DatabaseSyncer
import com.thekeeperofpie.artistalleydatabase.utils_room.Exporter
import com.thekeeperofpie.artistalleydatabase.utils_room.Importer
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbComponent
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import okhttp3.OkHttpClient
import kotlin.reflect.KType

@SingletonScope
@Component
abstract class ApplicationComponent(
    @get:Provides val application: Application,
    @get:Provides val masterKey: MasterKey,
    @get:Provides val applicationScope: ApplicationScope,
    @get:Provides val vgmdbDatabase: VgmdbDatabase,
    @get:Provides val json: Json,
    @get:Provides val musicalArtistDatabase: MusicalArtistDatabase,
    @get:Provides val aniListDatabase: AniListDatabase,
    @get:Provides val featureOverrideProvider: FeatureOverrideProvider,
    @get:Provides val appFileSystem: AppFileSystem,
    @get:Provides val cdEntryDatabase: CdEntryDatabase,
    @get:Provides val artEntryDatabase: ArtEntryDatabase,
    @get:Provides val animeDatabase: AnimeDatabase,
    @get:Provides val appMetadataProvider: AppMetadataProvider,
) : AniListComponent, AnimeComponent, Anime2AnimeComponent, ArtEntryComponent, BrowseComponent,
    CdEntryComponent, MusicalArtistComponent, SettingsComponent, VariantComponent, VgmdbComponent {

    abstract val artistRepository: ArtistRepository
    abstract val albumRepository: AlbumRepository
    abstract val albumEntryDao: AlbumEntryDao
    abstract val vgmdbDataConverter: VgmdbDataConverter
    abstract val vgmdbArtistDao: VgmdbArtistDao
    abstract val vgmdbApi: VgmdbApi
    abstract val vgmdbAutocompleter: VgmdbAutocompleter
    abstract val musicalArtistDao: MusicalArtistDao
    abstract val dataConverter: DataConverter
    abstract val characterRepository: CharacterRepository
    abstract val mediaRepository: MediaRepository
    abstract val characterEntryDao: CharacterEntryDao
    abstract val mediaEntryDao: MediaEntryDao
    abstract val aniListDataConverter: AniListDataConverter
    abstract val authedAniListApi: AuthedAniListApi
    abstract val aniListAutocompleter: AniListAutocompleter
    abstract val cdEntryNavigator: CdEntryNavigator
    abstract val cdEntryDao: CdEntryDao
    abstract val importers: Set<Importer>
    abstract val exporters: Set<Exporter>
    abstract val databaseSyncers: Set<DatabaseSyncer>
    abstract val artEntryNavigator: ArtEntryNavigator
    abstract val artEntryDetailsDao: ArtEntryDetailsDao
    abstract val historyController: HistoryController
    abstract val networkSettings: NetworkSettings
    abstract val aniListSettings: AniListSettings

    open val appUpdateChecker: (ComponentActivity) -> AppUpdateChecker?
        get() = { null }
    abstract val ignoreController: IgnoreController
    abstract val markdown: Markdown
    abstract val mediaGenreDialogController: MediaGenreDialogController
    abstract val mediaTagDialogController: MediaTagDialogController
    abstract val monetizationController: MonetizationController
    open val monetizationProvider: (ComponentActivity) -> MonetizationProvider?
        get() = { null }
    abstract val navigationTypeMap: NavigationTypeMap
    abstract val notificationsController: NotificationsController
    abstract val platformOAuthStore: PlatformOAuthStore
    abstract val settingsProvider: SettingsProvider
    open val subscriptionProvider: (ComponentActivity) -> SubscriptionProvider?
        get() = { null }

    protected val AppMonetizationOverrideProvider.bind: MonetizationOverrideProvider
        @Provides get() = this

    @SingletonScope
    @Provides
    fun provideNetworkClient(
        scope: ApplicationScope,
        application: Application,
        networkSettings: NetworkSettings,
        networkAuthProvider: NetworkAuthProvider,
    ) = buildNetworkClient(
        scope = scope,
        application = application,
        networkSettings = networkSettings,
        authProviders = mapOf(networkAuthProvider.host to networkAuthProvider),
    )

    @SingletonScope
    @Provides
    fun provideWebScraper(networkClient: NetworkClient) = networkClient.webScraper

    @SingletonScope
    @Provides
    fun provideOkHttpClient(networkClient: NetworkClient) = networkClient.okHttpClient

    @SingletonScope
    @Provides
    fun provideHttpClient(networkClient: NetworkClient) = networkClient.httpClient

    @SingletonScope
    @Provides
    fun provideMediaPlayer(
        scope: ApplicationScope,
        application: Application,
        okHttpClient: OkHttpClient,
        featureOverrideProvider: FeatureOverrideProvider,
    ) = MediaPlayer(
        scope = scope,
        application = application,
        okHttpClient = okHttpClient,
        enableCache = featureOverrideProvider.enableAppMediaPlayerCache,
    )

    @SingletonScope
    @Provides
    @IntoSet
    fun provideBaseTypeMap() : Map<KType, NavType<*>> = CustomNavTypes.baseTypeMap

    @SingletonScope
    @Provides
    fun bindsTypeMap(typeMaps: @JvmSuppressWildcards Set<Map<KType, NavType<*>>>): NavigationTypeMap =
        NavigationTypeMap(typeMaps.fold(mapOf<KType, NavType<*>>()) { acc, map -> acc + map })
}
