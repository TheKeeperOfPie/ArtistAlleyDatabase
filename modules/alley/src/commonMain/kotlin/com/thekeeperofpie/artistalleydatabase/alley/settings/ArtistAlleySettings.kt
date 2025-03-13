package com.thekeeperofpie.artistalleydatabase.alley.settings

import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import kotlinx.coroutines.flow.MutableStateFlow

interface ArtistAlleySettings {

    val appTheme: MutableStateFlow<AppThemeSetting>
    val lastKnownArtistsCsvSize: MutableStateFlow<Long>
    val lastKnownStampRalliesCsvSize: MutableStateFlow<Long>
    val displayType: MutableStateFlow<SearchScreen.DisplayType>
    val artistsSortOption: MutableStateFlow<String>
    val artistsSortAscending: MutableStateFlow<Boolean>
    val stampRalliesSortOption: MutableStateFlow<String>
    val stampRalliesSortAscending: MutableStateFlow<Boolean>
    val showGridByDefault: MutableStateFlow<Boolean>
    val showRandomCatalogImage: MutableStateFlow<Boolean>
    val showOnlyConfirmedTags: MutableStateFlow<Boolean>
    val showOnlyHasCommissions: MutableStateFlow<Boolean>
    val forceOneDisplayColumn: MutableStateFlow<Boolean>
    val dataYear: MutableStateFlow<DataYear>
}
