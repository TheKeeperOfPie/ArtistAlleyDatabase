package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import kotlinx.coroutines.flow.MutableStateFlow
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class ArtistAlleyDesktopSettings : ArtistAlleySettings {
    override val appTheme = MutableStateFlow(AppThemeSetting.AUTO)
    override val lastKnownArtistsCsvSize = MutableStateFlow(-1L)
    override val lastKnownStampRalliesCsvSize = MutableStateFlow(-1L)
    override val displayType = MutableStateFlow(SearchScreen.DisplayType.CARD)
    override val artistsSortOption = MutableStateFlow("")
    override val artistsSortAscending = MutableStateFlow(true)
    override val stampRalliesSortOption = MutableStateFlow("")
    override val stampRalliesSortAscending = MutableStateFlow(true)
    override val showGridByDefault = MutableStateFlow(false)
    override val showRandomCatalogImage = MutableStateFlow(false)
    override val showOnlyConfirmedTags = MutableStateFlow(false)
    override val showOnlyHasCommissions = MutableStateFlow(false)
    override val forceOneDisplayColumn = MutableStateFlow(false)
    override val dataYear = MutableStateFlow(DataYear.YEAR_2025)
}
