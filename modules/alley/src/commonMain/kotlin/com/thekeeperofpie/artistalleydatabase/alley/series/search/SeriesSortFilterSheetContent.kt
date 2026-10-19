package com.thekeeperofpie.artistalleydatabase.alley.series.search

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_source_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_series_filter_source_label
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesFilterOption
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterSaveableState.Section
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterSection2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldSheetContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SwitchSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.SnapshotStateSetSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.rememberSerializable
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import com.thekeeperofpie.artistalleydatabase.utils_preview.assertInPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SeriesSortFilterSheetContent(
    state: SeriesSortFilterState,
    sheetState: SheetState,
    scrollState: ScrollState = rememberScrollState(),
) {
    SortFilterBottomScaffoldSheetContent(
        sheetState = sheetState,
        scrollState = scrollState,
    ) {
        val persistentState = state.persistentState
        val saveableState = state.saveableState
        var sortOption by persistentState.sortOption.collectAsMutableStateWithLifecycle()
        var sortAscending by persistentState.sortAscending.collectAsMutableStateWithLifecycle()
        SortSection(
            header = { Text(stringResource(Res.string.alley_sort_label)) },
            expanded = { Section.SORT in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.SORT) },
            sortOptions = { SeriesSearchSortOption.entries },
            sortOption = { sortOption },
            onSortChanged = { sortOption = it },
            sortOptionLabel = { Text(stringResource(it.textRes)) },
            sortAscending = { sortAscending },
            onSortAscendingChange = { sortAscending = it },
        )

        HorizontalDivider()

        FilterSection2(
            expanded = { Section.SOURCE in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.SOURCE) },
            options = SeriesFilterOption.entries,
            state = saveableState.sourceIn,
            selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ONLY_INCLUDE_WITH_EXCLUSIVE_FIRST,
            title = { Text(stringResource(Res.string.alley_series_filter_source_label)) },
            titleDropdownContentDescriptionRes = Res.string.alley_series_filter_source_content_description,
            optionLabel = { Text(stringResource(it.title)) },
        )

        HorizontalDivider()

        var showOnlyConfirmedTags by persistentState.showOnlyConfirmedTags.collectAsMutableStateWithLifecycle()
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_show_only_confirmed_tags)) },
            enabled = { showOnlyConfirmedTags },
            onEnabledChanged = { showOnlyConfirmedTags = it },
        )
    }
}

class SeriesSortFilterState(
    val persistentState: SeriesSortFilterPersistentState,
    val saveableState: SeriesSortFilterSaveableState,
) {

    companion object {
        @Composable
        fun rememberForPreview(): SeriesSortFilterState {
            assertInPreview()
            val persistentState = SeriesSortFilterPersistentState.rememberForPreview()
            val saveableState = rememberSerializable { SeriesSortFilterSaveableState() }
            return remember(persistentState, saveableState) {
                SeriesSortFilterState(
                    persistentState = persistentState,
                    saveableState = saveableState,
                )
            }
        }
    }
}

@Serializable
class SeriesSortFilterPersistentState(
    val sortOption: MutableStateFlow<SeriesSearchSortOption>,
    val sortAscending: MutableStateFlow<Boolean>,
    val showOnlyConfirmedTags: MutableStateFlow<Boolean>,
) {
    companion object {
        @Composable
        fun rememberForPreview(): SeriesSortFilterPersistentState {
            assertInPreview()
            return SeriesSortFilterPersistentState(
                sortOption = MutableStateFlow(SeriesSearchSortOption.RANDOM),
                sortAscending = MutableStateFlow(true),
                showOnlyConfirmedTags = MutableStateFlow(false),
            )
        }
    }
}

@Serializable
class SeriesSortFilterSaveableState @RememberInComposition constructor(
    val sourceIn: FilterSectionState<SeriesFilterOption> = FilterSectionState(),
    @Serializable(SnapshotStateSetSerializer::class)
    val expandedSections: SnapshotStateSet<Section> = mutableStateSetOf(),
) {
    enum class Section {
        SORT,
        SOURCE,
    }
}

@Composable
private fun SeriesSortFilterSheetContentPreview(
    state: SeriesSortFilterState,
    scrollState: ScrollState = rememberScrollState(),
) {
    SeriesSortFilterSheetContent(
        state = state,
        scrollState = scrollState,
        sheetState = rememberBottomSheetState(SheetValue.PartiallyExpanded),
    )
}

@AlleyPreview
@Composable
private fun SeriesSortFilterSheetContentPreview() {
    SeriesSortFilterSheetContentPreview(SeriesSortFilterState.rememberForPreview())
}

@AlleyPreview
@Composable
private fun SeriesSortFilterSheetContentExpandedPreview() {
    val scrollState = rememberScrollState(1000)
    val state = SeriesSortFilterState.rememberForPreview().apply {
        saveableState.expandedSections.addAll(Section.entries)
    }
    SeriesSortFilterSheetContentPreview(state, scrollState)
}
