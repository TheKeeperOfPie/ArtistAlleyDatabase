package com.thekeeperofpie.artistalleydatabase.anime.studio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class StudioSortFilterController(settings: AnimeSettings) : SortFilterController(settings) {

    @Composable
    override fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value

    private val sortSection = SortFilterSection.Sort(
        enumClass = StudioSortOption::class,
        defaultEnabled = StudioSortOption.SEARCH_MATCH,
        headerTextRes = R.string.anime_studio_filter_sort_label,
    )

    override var sections = listOf(
        sortSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    fun filterParams() = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
        )
    }.debounce(500.milliseconds)

    data class FilterParams(
        val sort: List<SortEntry<StudioSortOption>>,
        val sortAscending: Boolean,
    )
}
