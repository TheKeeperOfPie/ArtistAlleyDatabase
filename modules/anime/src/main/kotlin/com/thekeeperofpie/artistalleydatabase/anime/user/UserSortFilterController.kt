package com.thekeeperofpie.artistalleydatabase.anime.user

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
class UserSortFilterController(settings: AnimeSettings) : SortFilterController(settings) {

    @Composable
    override fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value

    private val sortSection = SortFilterSection.Sort(
        enumClass = UserSortOption::class,
        defaultEnabled = UserSortOption.SEARCH_MATCH,
        headerTextRes = R.string.anime_user_filter_sort_label,
    )

    private val moderatorSection = SortFilterSection.Switch(
        titleRes = R.string.anime_user_filter_moderator_label,
        enabled = false,
    )

    override var sections = listOf(
        sortSection,
        moderatorSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    fun filterParams() = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
            isModerator = moderatorSection.enabled,
        )
    }.debounce(500.milliseconds)

    data class FilterParams(
        val sort: List<SortEntry<UserSortOption>>,
        val sortAscending: Boolean,
        val isModerator: Boolean,
    )
}
