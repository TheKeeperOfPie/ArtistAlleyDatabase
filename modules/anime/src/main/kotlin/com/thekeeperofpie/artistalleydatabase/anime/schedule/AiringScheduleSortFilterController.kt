package com.thekeeperofpie.artistalleydatabase.anime.schedule

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
class AiringScheduleSortFilterController(
    settings: AnimeSettings,
) : SortFilterController(settings) {

    private val sortSection = SortFilterSection.Sort(
        AiringScheduleSortOption::class,
        AiringScheduleSortOption.POPULARITY,
        R.string.anime_airing_schedule_sort_label,
    )

    override val sections = listOf(
        sortSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    fun filterParams() = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
        )
    }.debounce(500.milliseconds)

    data class FilterParams(
        val sort: List<SortEntry<AiringScheduleSortOption>>,
        val sortAscending: Boolean,
    )
}
