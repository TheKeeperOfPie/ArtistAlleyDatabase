package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.lifecycle.SavedStateHandle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_source_chip_state_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_source_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_source_label
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource

class SeriesSortFilterController(
    scope: CoroutineScope,
    settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
) {
    val sortOption = settings.seriesSortOption
    val sortAscending = settings.seriesSortAscending
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.alley_sort_label,
        defaultSort = SeriesSearchSortOption.RANDOM,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val sourceIn = savedStateHandle.getMutableStateFlow<String, Set<SeriesFilterOption>>(
        scope = scope,
        key = "sourceIn",
        initialValue = { emptySet() },
        serialize = Json::encodeToString,
        deserialize = Json::decodeFromString,
    )
    private val sourceSection = SortFilterSectionState.Filter(
        title = Res.string.alley_series_filter_source_label,
        titleDropdownContentDescription = Res.string.alley_series_filter_source_content_description,
        includeExcludeIconContentDescription = Res.string.alley_series_filter_source_chip_state_content_description,
        options = MutableStateFlow(SeriesFilterOption.entries),
        filterIn = sourceIn,
        filterNotIn = MutableStateFlow(emptySet()),
        valueToText = { stringResource(it.title) },
        selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ONLY_INCLUDE_WITH_EXCLUSIVE_FIRST,
    )

    private val filterParams = combineStates(sortOption, sortAscending, sourceIn, ::FilterParams)

    val state = SortFilterState(
        sections = listOf(sortSection, sourceSection),
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    data class FilterParams(
        val sortOption: SeriesSearchSortOption,
        val sortAscending: Boolean,
        val sourceIn: Set<SeriesFilterOption>,
    )
}
