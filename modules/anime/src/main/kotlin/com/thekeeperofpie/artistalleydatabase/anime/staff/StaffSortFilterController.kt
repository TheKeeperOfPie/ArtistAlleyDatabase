package com.thekeeperofpie.artistalleydatabase.anime.staff

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
class StaffSortFilterController(
    private val settings: AnimeSettings,
    private val allowRelevanceSort: Boolean = false,
) : SortFilterController {

    @Composable
    override fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value

    private val sortSection = SortFilterSection.Sort(
        enumClass = StaffSortOption::class,
        defaultEnabled = StaffSortOption.SEARCH_MATCH,
        headerTextRes = R.string.anime_staff_filter_sort_label,
    ).apply {
        if (!allowRelevanceSort) {
            sortOptions = sortOptions.filter { it.value != StaffSortOption.RELEVANCE }
        }
    }

    private val birthdaySection = SortFilterSection.Switch(
        titleRes = R.string.anime_staff_filter_birthday_label,
        enabled = false,
    )

    override var sections = listOf(
        sortSection,
        birthdaySection,
        SortFilterSection.Spacer(height = 32.dp),
    )
    override val state = SortFilterSection.ExpandedState()

    fun filterParams() = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
            isBirthday = birthdaySection.enabled,
        )
    }.debounce(500.milliseconds)

    data class FilterParams(
        val sort: List<SortEntry<StaffSortOption>>,
        val sortAscending: Boolean,
        val isBirthday: Boolean,
    )
}
