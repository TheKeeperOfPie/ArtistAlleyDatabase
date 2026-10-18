package com.thekeeperofpie.artistalleydatabase.alley.settings

import com.thekeeperofpie.artistalleydatabase.alley.AlleyRootDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterSettings
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import kotlinx.coroutines.flow.MutableStateFlow

interface ArtistAlleySettings : ArtistSortFilterSettings {

    var appTheme: AppThemeSetting
    var displayType: SearchDisplayType

    val stampRalliesSortOption: MutableStateFlow<StampRallySearchSortOption>
    val stampRalliesSortAscending: MutableStateFlow<Boolean>
    val seriesSortOption: MutableStateFlow<SeriesSearchSortOption>
    val seriesSortAscending: MutableStateFlow<Boolean>
    val showGridByDefault: MutableStateFlow<Boolean>
    val showRandomCatalogImage: MutableStateFlow<Boolean>
    val showOnlyConfirmedTags: MutableStateFlow<Boolean>
    val forceOneDisplayColumn: MutableStateFlow<Boolean>
    val dataYear: MutableStateFlow<DataYear>
    val languageOption: MutableStateFlow<AniListLanguageOption>
    val easterEggEnabled: MutableStateFlow<Boolean>

    val rootDestination: MutableStateFlow<AlleyRootDestination>
}
