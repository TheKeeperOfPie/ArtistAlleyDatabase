package com.thekeeperofpie.artistalleydatabase.alley.settings

import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import kotlinx.coroutines.flow.MutableStateFlow

interface ArtistAlleySettings {

    val appTheme: MutableStateFlow<AppThemeSetting>
    val lastKnownArtistsCsvSize: MutableStateFlow<Long>
    val lastKnownStampRalliesCsvSize: MutableStateFlow<Long>
    val displayType: MutableStateFlow<SearchScreen.DisplayType>
    val artistsSortOption: MutableStateFlow<ArtistSearchSortOption>
    val artistsSortAscending: MutableStateFlow<Boolean>
    val stampRalliesSortOption: MutableStateFlow<StampRallySearchSortOption>
    val stampRalliesSortAscending: MutableStateFlow<Boolean>
    val showGridByDefault: MutableStateFlow<Boolean>
    val showRandomCatalogImage: MutableStateFlow<Boolean>
    val showOnlyConfirmedTags: MutableStateFlow<Boolean>
    val showOnlyWithCatalog: MutableStateFlow<Boolean>
    val forceOneDisplayColumn: MutableStateFlow<Boolean>
    val dataYear: MutableStateFlow<DataYear>
    val languageOption: MutableStateFlow<AniListLanguageOption>
}
