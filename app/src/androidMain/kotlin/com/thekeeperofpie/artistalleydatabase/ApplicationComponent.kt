package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.navigation.NavType
import androidx.room.Room
import androidx.work.WorkManager
import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListComponent
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.PlatformOAuthStore
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeComponent
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryComponent
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.browse.BrowseComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.chooser.ChooserViewModel
import com.thekeeperofpie.artistalleydatabase.export.ExportViewModel
import com.thekeeperofpie.artistalleydatabase.importing.ImportViewModel
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.media.MediaPlayer
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistComponent
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.settings.SettingsComponent
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppMetadataProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestinationProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkComponent
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbComponent
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.work.WorkerComponent
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
) : AniListComponent, AnimeComponent, Anime2AnimeComponent, ArtEntryComponent, BrowseComponent,
    CdEntryComponent, MusicalArtistComponent, NetworkComponent, SettingsComponent, ApplicationVariantComponent,
    VgmdbComponent, WorkerComponent {

    abstract val chooserViewModel: () -> ChooserViewModel
    abstract val exportViewModel: () -> ExportViewModel
    abstract val importViewModel: () -> ImportViewModel

    abstract val appMetadataProvider: AppMetadataProvider
    abstract val artEntryNavigator: ArtEntryNavigator
    abstract val cdEntryNavigator: CdEntryNavigator
    abstract val featureOverrideProvider: FeatureOverrideProvider
    abstract val ignoreController: IgnoreController
    abstract val markdown: Markdown
    abstract val mediaGenreDialogController: MediaGenreDialogController
    abstract val mediaTagDialogController: MediaTagDialogController
    abstract val monetizationController: MonetizationController
    abstract val navigationTypeMap: NavigationTypeMap
    abstract val notificationsController: NotificationsController
    abstract val platformOAuthStore: PlatformOAuthStore
    abstract val settingsProvider: SettingsProvider
    abstract val workManager: WorkManager

    abstract val navDestinationProviders: Set<NavDestinationProvider>

    val AppMonetizationOverrideProvider.bind: MonetizationOverrideProvider
        @Provides get() = this

    val AppFeatureOverrideProvider.bind: FeatureOverrideProvider
        @Provides get() = this

    val AppDatabase.bindAniListDatabase: AniListDatabase
        @Provides get() = this

    val AppDatabase.bindAnimeDatabase: AnimeDatabase
        @Provides get() = this

    val AppDatabase.bindArtEntryDatabase: ArtEntryDatabase
        @Provides get() = this

    val AppDatabase.bindCdEntryDatabase: CdEntryDatabase
        @Provides get() = this

    val AppDatabase.bindMusicalArtistDatabase: MusicalArtistDatabase
        @Provides get() = this

    val AppDatabase.bindVgmdbDatabase: VgmdbDatabase
        @Provides get() = this

    @SingletonScope
    @Provides
    fun provideMasterKey(application: Application) = CryptoUtils.masterKey(application)

    @SingletonScope
    @Provides
    fun provideApplicationScope(application: Application): ApplicationScope =
        (application as CustomApplication).scope

    @SingletonScope
    @Provides
    fun provideAppDatabase(application: Application) =
        Room.databaseBuilder(application, AppDatabase::class.java, "appDatabase")
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .addMigrations(Migration_6_7)
            .build()

    @SingletonScope
    @Provides
    fun provideAppMetadataProvider(): AppMetadataProvider = object : AppMetadataProvider {
        override val versionCode = BuildConfig.VERSION_CODE
        override val versionName = BuildConfig.VERSION_NAME
        override val appDrawableModel = R.mipmap.ic_launcher
    }

    // TODO: Move this somewhere shared?
    @SingletonScope
    @Provides
    fun provideJson() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

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
    fun provideOkHttpClient(networkClient: NetworkClient) = networkClient.okHttpClient

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
