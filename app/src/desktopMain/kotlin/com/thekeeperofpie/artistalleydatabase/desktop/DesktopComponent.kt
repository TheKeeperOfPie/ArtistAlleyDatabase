package com.thekeeperofpie.artistalleydatabase.desktop

import androidx.navigation.NavType
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.thekeeperofpie.artistalleydatabase.AppComponent
import com.thekeeperofpie.artistalleydatabase.AppSettings
import com.thekeeperofpie.artistalleydatabase.anilist.AniListComponent
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryComponent
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.settings.SettingsStore
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkComponent
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbComponent
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import kotlin.reflect.KType

@SingleIn(AppScope::class)
@DependencyGraph
interface DesktopComponent : AppComponent, AniListComponent, AnimeComponent, ArtEntryComponent,
    CdEntryComponent, NetworkComponent, VgmdbComponent {

    val artEntryNavigator: ArtEntryNavigator
    val cdEntryNavigator: CdEntryNavigator
    val httpClient: HttpClient
    val navigationTypeMap: NavigationTypeMap
    val settingsProvider: DesktopSettingsProvider
    val monetizationController: MonetizationController

    @Binds
    val DesktopSettingsProvider.bindAppSettings: AppSettings

    @Binds
    val DesktopDatabase.bindAniListDatabase: AniListDatabase

    @Binds
    val DesktopDatabase.bindAnimeDatabase: AnimeDatabase

    @Binds
    val DesktopDatabase.bindArtEntryDatabase: ArtEntryDatabase

    @Binds
    val DesktopDatabase.bindCdEntryDatabase: CdEntryDatabase

    @Binds
    val DesktopDatabase.bindVgmdbDatabase: VgmdbDatabase

    // Remove this once SettingsProvider is unified
    @SingleIn(AppScope::class)
    @Provides
    fun provideSettingsProvider(
        scope: ApplicationScope,
        json: Json,
        featureOverrideProvider: FeatureOverrideProvider,
        settingsStore: SettingsStore,
    ): SettingsProvider = SettingsProvider(scope, json, featureOverrideProvider, settingsStore)

    @SingleIn(AppScope::class)
    @Provides
    fun provideNetworkClient(): NetworkClient = buildNetworkClient()

    @SingleIn(AppScope::class)
    @Provides
    fun provideFeatureOverrideProvider(): FeatureOverrideProvider =
        object : FeatureOverrideProvider {
            // TODO
            override val isReleaseBuild = false
            override val enableAppMediaPlayerCache = false
        }

    @SingleIn(AppScope::class)
    @Provides
    fun provideDesktopDatabase(): DesktopDatabase = Room.inMemoryDatabaseBuilder<DesktopDatabase>()
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .build()

    @SingleIn(AppScope::class)
    @Provides
    @IntoSet
    fun provideBaseTypeMap(): Map<KType, NavType<*>> = CustomNavTypes.baseTypeMap

    @SingleIn(AppScope::class)
    @Provides
    fun providesNavigationTypeMap(
        typeMaps: @JvmSuppressWildcards Set<Map<KType, NavType<*>>>,
    ): NavigationTypeMap = NavigationTypeMap(typeMaps.fold(mapOf()) { acc, map -> acc + map })

    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideMonetizationOverrideProvider(): MonetizationOverrideProvider =
        object : MonetizationOverrideProvider {
            override val overrideUnlock = MutableStateFlow(false)
        }

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides scope: ApplicationScope): DesktopComponent
    }
}
