package com.thekeeperofpie.artistalleydatabase.alley.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.alley.home.AlleyHomeDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistTag
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ArtistAlleyDesktopSettings : ArtistAlleySettings {
    override var appTheme by mutableStateOf(AppThemeSetting.MIKU)

    override var artistsSortOption by mutableStateOf(ArtistSearchSortOption.RANDOM)
    override var artistsSortAscending by mutableStateOf(true)
    override var artistsTagsIn by mutableStateOf(emptySet<ArtistTag>())
    override var artistsTagsNotIn by mutableStateOf(emptySet<ArtistTag>())
    override var showOutdatedCatalogs by mutableStateOf(false)

    override var stampRalliesSortOption by mutableStateOf(StampRallySearchSortOption.RANDOM)
    override var stampRalliesSortAscending by mutableStateOf(true)

    override var displayType by mutableStateOf(SearchDisplayType.CARD)
    override var showGridByDefault by mutableStateOf(false)
    override var showRandomCatalogImage by mutableStateOf(false)
    override var showOnlyConfirmedTags by mutableStateOf(false)
    override var forceOneDisplayColumn by mutableStateOf(false)

    override var seriesSortOption by mutableStateOf(SeriesSearchSortOption.RANDOM)
    override var seriesSortAscending by mutableStateOf(true)

    override var dataYear by mutableStateOf(DataYear.LATEST)
    override var languageOption by mutableStateOf(AniListLanguageOption.DEFAULT)
    override var easterEggEnabled by mutableStateOf(true)

    override var rootDestination by mutableStateOf(AlleyHomeDestination.ARTISTS)
}
