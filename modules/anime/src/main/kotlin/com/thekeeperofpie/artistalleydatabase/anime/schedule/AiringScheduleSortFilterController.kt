package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class AiringScheduleSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : AnimeSettingsSortFilterController<AiringScheduleSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        AiringScheduleSortOption::class,
        AiringScheduleSortOption.POPULARITY,
        R.string.anime_airing_schedule_sort_label,
    )

    override val sections = listOf(
        sortSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
    )

    data class FilterParams(
        val sort: List<SortEntry<AiringScheduleSortOption>>,
        val sortAscending: Boolean,
    )
}
