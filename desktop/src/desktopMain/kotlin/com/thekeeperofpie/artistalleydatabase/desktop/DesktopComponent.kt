package com.thekeeperofpie.artistalleydatabase.desktop

import androidx.navigation.NavType
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.thekeeperofpie.artistalleydatabase.anilist.AniListComponent
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSettings
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestinationProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkComponent
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
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
) : AniListComponent, AnimeComponent, CdEntryComponent, NetworkComponent, VgmdbComponent {

    abstract val cdEntryNavigator: CdEntryNavigator
    abstract val httpClient: HttpClient
    abstract val ignoreController: IgnoreController
    abstract val markdown: Markdown
    abstract val mediaGenreDialogController: MediaGenreDialogController
    abstract val mediaTagDialogController: MediaTagDialogController
    abstract val navigationTypeMap: NavigationTypeMap
    abstract val settingsProvider: DesktopSettingsProvider

    abstract val navDestinationProviders: Set<NavDestinationProvider>

    val DesktopSettingsProvider.bindAniListSettings: AniListSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindAnimeSettings: AnimeSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindCharacterSettings: CharacterSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindCropSettings: CropSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindIgnoreSettings: IgnoreSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindMediaDataSettings: MediaDataSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindMonetizationSettings: MonetizationSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindNewsSettings: NewsSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindNetworkSettings: NetworkSettings
        @Provides get() = this

    val DesktopSettingsProvider.bindStaffSettings: StaffSettings
        @Provides get() = this

    val DesktopDatabase.bindAniListDatabase: AniListDatabase
        @Provides get() = this

    val DesktopDatabase.bindAnimeDatabase: AnimeDatabase
        @Provides get() = this

    val DesktopDatabase.bindCdEntryDatabase: CdEntryDatabase
        @Provides get() = this

    val DesktopDatabase.bindVgmdbDatabase: VgmdbDatabase
        @Provides get() = this

    @SingletonScope
    @Provides
    fun provideSettingsProvider() = DesktopSettingsProvider()

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
