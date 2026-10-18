package com.thekeeperofpie.artistalleydatabase.alley.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.alley.AlleyRootDestination
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
import kotlinx.coroutines.flow.MutableStateFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ArtistAlleyDesktopSettings : ArtistAlleySettings {
    override var appTheme by mutableStateOf(AppThemeSetting.MIKU)
    override var displayType by mutableStateOf(SearchDisplayType.CARD)

    override var artistsSortOption by mutableStateOf(ArtistSearchSortOption.RANDOM)
    override var artistsSortAscending by mutableStateOf(true)
    override var artistsTagsIn by mutableStateOf(emptySet<ArtistTag>())
    override var artistsTagsNotIn by mutableStateOf(emptySet<ArtistTag>())
    override var showOutdatedCatalogs by mutableStateOf(false)

    override val stampRalliesSortOption = MutableStateFlow(StampRallySearchSortOption.RANDOM)
    override val stampRalliesSortAscending = MutableStateFlow(true)
    override val seriesSortOption = MutableStateFlow(SeriesSearchSortOption.RANDOM)
    override val seriesSortAscending = MutableStateFlow(true)
    override val showGridByDefault = MutableStateFlow(false)
    override val showRandomCatalogImage = MutableStateFlow(false)
    override val showOnlyConfirmedTags = MutableStateFlow(false)
    override val forceOneDisplayColumn = MutableStateFlow(false)
    override val dataYear = MutableStateFlow(DataYear.LATEST)
    override val languageOption = MutableStateFlow(AniListLanguageOption.DEFAULT)
    override val easterEggEnabled = MutableStateFlow(true)

    override val rootDestination = MutableStateFlow(AlleyRootDestination.ARTISTS)
}
