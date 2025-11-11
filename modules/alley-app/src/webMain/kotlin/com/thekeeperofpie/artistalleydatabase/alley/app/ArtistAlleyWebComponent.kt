package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.MutableStateFlow

expect class ArtistAlleyWebSettings : ArtistAlleySettings {
    override val appTheme: MutableStateFlow<AppThemeSetting>
    override val lastKnownArtistsCsvSize: MutableStateFlow<Long>
    override val lastKnownStampRalliesCsvSize: MutableStateFlow<Long>
    override val displayType: MutableStateFlow<SearchScreen.DisplayType>
    override val artistsSortOption: MutableStateFlow<ArtistSearchSortOption>
    override val artistsSortAscending: MutableStateFlow<Boolean>
    override val stampRalliesSortOption: MutableStateFlow<StampRallySearchSortOption>
    override val stampRalliesSortAscending: MutableStateFlow<Boolean>
    override val seriesSortOption: MutableStateFlow<SeriesSearchSortOption>
    override val seriesSortAscending: MutableStateFlow<Boolean>
    override val showGridByDefault: MutableStateFlow<Boolean>
    override val showRandomCatalogImage: MutableStateFlow<Boolean>
    override val showOnlyConfirmedTags: MutableStateFlow<Boolean>
    override val showOnlyWithCatalog: MutableStateFlow<Boolean>
    override val forceOneDisplayColumn: MutableStateFlow<Boolean>
    override val dataYear: MutableStateFlow<DataYear>
    override val languageOption: MutableStateFlow<AniListLanguageOption>
    override val showOutdatedCatalogs: MutableStateFlow<Boolean>
}

@DependencyGraph
interface ArtistAlleyWebComponent : ArtistAlleyAppComponent {
    val appFileSystem: AppFileSystem
    val artistImageCache: ArtistImageCache
    val deepLinker: DeepLinker

    @Binds
    val ArtistAlleyWebSettings.bindArtistAlleySettings: ArtistAlleySettings

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides scope: ApplicationScope): ArtistAlleyWebComponent
    }
}
