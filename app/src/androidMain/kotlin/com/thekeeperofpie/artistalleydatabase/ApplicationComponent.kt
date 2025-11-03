package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.navigation.NavType
import androidx.room.Room
import androidx.security.crypto.MasterKey
import androidx.work.WorkManager
import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListComponent
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.PlatformOAuthStore
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
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
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkComponent
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbComponent
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.work.WorkerComponent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import kotlin.reflect.KType

@SingleIn(AppScope::class)
@DependencyGraph
interface ApplicationComponent : AppComponent, AniListComponent, AnimeComponent, Anime2AnimeComponent, ArtEntryComponent,
    BrowseComponent, CdEntryComponent, MusicalArtistComponent, NetworkComponent, SettingsComponent,
    ApplicationVariantComponent, VgmdbComponent, WorkerComponent {

    val activityComponentFactory: ActivityComponent.Factory

    val chooserViewModel: Provider<ChooserViewModel>
    val exportViewModel: Provider<ExportViewModel>
    val importViewModel: Provider<ImportViewModel>

    val appMetadataProvider: AppMetadataProvider
    val artEntryNavigator: ArtEntryNavigator
    val cdEntryNavigator: CdEntryNavigator
    val featureOverrideProvider: FeatureOverrideProvider
    val monetizationController: MonetizationController
    val navigationTypeMap: NavigationTypeMap
    val notificationsController: NotificationsController
    val platformOAuthStore: PlatformOAuthStore
    val settingsProvider: SettingsProvider
    val workManager: WorkManager

    @Provides
    fun bindSettingsProvider(settingsProvider: AndroidSettingsProvider): SettingsProvider =
        settingsProvider

    @Provides
    fun bindAppSettings(settingsProvider: AndroidSettingsProvider): AppSettings = settingsProvider

    @Provides
    fun bindMonetizationOverrideProvider(
        monetizationOverrideProvider: AppMonetizationOverrideProvider,
    ): MonetizationOverrideProvider = monetizationOverrideProvider

    @Provides
    fun bindFeatureOverrideProvider(
        featureOverrideProvider: AppFeatureOverrideProvider
    ): FeatureOverrideProvider = featureOverrideProvider

    @Provides
    fun bindAniListDatabase(database: AppDatabase): AniListDatabase = database

    @Provides
    fun bindAnimeDatabase(database: AppDatabase): AnimeDatabase = database

    @Provides
    fun bindArtEntryDatabase(database: AppDatabase): ArtEntryDatabase = database

    @Provides
    fun bindCdEntryDatabase(database: AppDatabase): CdEntryDatabase = database

    @Provides
    fun bindMusicalArtistDatabase(database: AppDatabase): MusicalArtistDatabase = database

    @Provides
    fun bindVgmdbDatabase(database: AppDatabase): VgmdbDatabase = database

    @SingleIn(AppScope::class)
    @Provides
    fun provideMasterKey(application: Application): MasterKey =
        CryptoUtils.masterKey(application, "AnichiveMasterKey")

    @SingleIn(AppScope::class)
    @Provides
    fun provideApplicationScope(application: Application): ApplicationScope =
        (application as CustomApplication).scope

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, "appDatabase")
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .addMigrations(Migration_6_7)
            .build()

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppMetadataProvider(): AppMetadataProvider = object : AppMetadataProvider {
        override val versionCode = BuildConfig.VERSION_CODE
        override val versionName = BuildConfig.VERSION_NAME
        override val appDrawableModel = R.mipmap.ic_launcher
    }

    // TODO: Move this somewhere shared?
    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideNetworkClient(
        scope: ApplicationScope,
        application: Application,
        networkSettings: NetworkSettings,
        networkAuthProvider: NetworkAuthProvider,
    ): NetworkClient = buildNetworkClient(
        scope = scope,
        application = application,
        networkSettings = networkSettings,
        authProviders = mapOf(networkAuthProvider.host to networkAuthProvider),
    )

    @SingleIn(AppScope::class)
    @Provides
    fun provideOkHttpClient(networkClient: NetworkClient): OkHttpClient = networkClient.okHttpClient

    @SingleIn(AppScope::class)
    @Provides
    fun provideMediaPlayer(
        scope: ApplicationScope,
        application: Application,
        okHttpClient: OkHttpClient,
        featureOverrideProvider: FeatureOverrideProvider,
    ): MediaPlayer = MediaPlayer(
        scope = scope,
        application = application,
        okHttpClient = okHttpClient,
        enableCache = featureOverrideProvider.enableAppMediaPlayerCache,
    )

    @SingleIn(AppScope::class)
    @Provides
    @IntoSet
    fun provideBaseTypeMap(): Map<KType, NavType<*>> = CustomNavTypes.baseTypeMap

    @SingleIn(AppScope::class)
    @Provides
    fun bindsTypeMap(typeMaps: @JvmSuppressWildcards Set<Map<KType, NavType<*>>>): NavigationTypeMap =
        NavigationTypeMap(typeMaps.fold(mapOf<KType, NavType<*>>()) { acc, map -> acc + map })

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides application: Application): ApplicationComponent
    }
}
