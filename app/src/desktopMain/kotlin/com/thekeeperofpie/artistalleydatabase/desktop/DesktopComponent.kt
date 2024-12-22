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
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.settings.SettingsStore
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestinationProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkComponent
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbComponent
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import kotlin.reflect.KType

@SingletonScope
@Component
abstract class DesktopComponent(
    @get:Provides val scope: ApplicationScope,
) : AppComponent, AniListComponent, AnimeComponent, CdEntryComponent, NetworkComponent,
    VgmdbComponent {

    abstract val cdEntryNavigator: CdEntryNavigator
    abstract val httpClient: HttpClient
    abstract val navigationTypeMap: NavigationTypeMap
    abstract val settingsProvider: DesktopSettingsProvider

    abstract val navDestinationProviders: Set<NavDestinationProvider>

    val DesktopSettingsProvider.bindAppSettings: AppSettings
        @Provides get() = this

    val DesktopDatabase.bindAniListDatabase: AniListDatabase
        @Provides get() = this

    val DesktopDatabase.bindAnimeDatabase: AnimeDatabase
        @Provides get() = this

    val DesktopDatabase.bindCdEntryDatabase: CdEntryDatabase
        @Provides get() = this

    val DesktopDatabase.bindVgmdbDatabase: VgmdbDatabase
        @Provides get() = this

    // Remove this once SettingsProvider is unified
    @SingletonScope
    @Provides
    fun provideSettingsProvider(
        scope: ApplicationScope,
        json: Json,
        featureOverrideProvider: FeatureOverrideProvider,
        settingsStore: SettingsStore,
    ) = SettingsProvider(scope, json, featureOverrideProvider, settingsStore)

    @SingletonScope
    @Provides
    fun provideNetworkClient() = buildNetworkClient()

    @SingletonScope
    @Provides
    fun provideFeatureOverrideProvider(): FeatureOverrideProvider =
        object : FeatureOverrideProvider {
            // TODO
            override val isReleaseBuild = false
            override val enableAppMediaPlayerCache = false
        }

    @SingletonScope
    @Provides
    fun provideDesktopDatabase() = Room.inMemoryDatabaseBuilder<DesktopDatabase>()
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .build()

    @SingletonScope
    @Provides
    @IntoSet
    fun provideBaseTypeMap(): Map<KType, NavType<*>> = CustomNavTypes.baseTypeMap

    @SingletonScope
    @Provides
    fun bindsTypeMap(typeMaps: @JvmSuppressWildcards Set<Map<KType, NavType<*>>>): NavigationTypeMap =
        NavigationTypeMap(typeMaps.fold(mapOf<KType, NavType<*>>()) { acc, map -> acc + map })

    @SingletonScope
    @Provides
    fun provideJson() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @SingletonScope
    @Provides
    fun provideMonetizationOverrideProvider(): MonetizationOverrideProvider =
        object : MonetizationOverrideProvider {
            override val overrideUnlock = MutableStateFlow(false)
        }
}
