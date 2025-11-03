package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.schedule.generated.resources.Res
import artistalleydatabase.modules.anime.schedule.generated.resources.anime_airing_schedule_sort_label
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class AiringScheduleSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption = savedStateHandle
        .getMutableStateFlow(json, "sortOption", AiringScheduleSortOption.POPULARITY)
    private val sortAscending =
        savedStateHandle.getMutableStateFlow<Boolean>("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_airing_schedule_sort_label,
        defaultSort = AiringScheduleSortOption.POPULARITY,
        sortAscending = sortAscending,
        sortOption = sortOption,
    )

    private val sections = listOf(
        sortSection,
        makeAdvancedSection(),
    )

    private val filterParams = combineStates(sortOption, sortAscending) { sortOption, sortAscending ->
        FilterParams(sort = sortOption, sortAscending = sortAscending)
    }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class FilterParams(
        val sort: AiringScheduleSortOption,
        val sortAscending: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): AiringScheduleSortFilterViewModel
    }
}
