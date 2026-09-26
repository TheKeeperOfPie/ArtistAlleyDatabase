package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_favorited
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_fandom_merch_warning
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_merch
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_merch_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_limit_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_merch
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_merch_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_total_cost
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_total_cost_expand_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.clear
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryProvider
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagSection2
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterSaveableState.Section
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesFilterSection
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesFilterState
import com.thekeeperofpie.artistalleydatabase.alley.ui.assertInPreview
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeDataFilterSection2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeDataSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SectionGroup
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SectionsExpandIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldSheetContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SwitchSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.SnapshotStateSetSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.rememberSerializable
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

@Composable
internal fun StampRallySortFilterSheetContent(
    state: StampRallySortFilterState,
    sheetState: SheetState,
    seriesAutocompleteResults: () -> List<SeriesInfo>,
    scrollState: ScrollState = rememberScrollState(),
    showHideFavorited: Boolean = true,
) {
    SortFilterBottomScaffoldSheetContent(
        sheetState = sheetState,
        scrollState = scrollState,
        actions = {
            val expandedSections = state.saveableState.expandedSections
            val allSectionsExpanded by remember(expandedSections) {
                derivedStateOf {
                    expandedSections.containsAll(Section.entries)
                }
            }
            SectionsExpandIndicator(
                allSectionsExpanded = { allSectionsExpanded },
                onSectionsExpandedChanged = {
                    if (it) {
                        expandedSections.addAll(Section.entries)
                    } else {
                        expandedSections.clear()
                    }
                },
                activatedCount = { state.activeCount },
                targetValue = { sheetState.targetValue },
            )
        },
        footer = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { state.clear() }) {
                    Text(text = stringResource(UtilsComposeRes.string.clear))
                }
            }
        }
    ) {
        val saveableState = state.saveableState
        val persistentState = state.persistentState
        var sortOption by persistentState.sortOption.collectAsMutableStateWithLifecycle()
        var sortAscending by persistentState.sortAscending.collectAsMutableStateWithLifecycle()
        SortSection(
            header = { Text(stringResource(Res.string.alley_sort_label)) },
            expanded = { Section.SORT in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.SORT) },
            sortOptions = { StampRallySearchSortOption.entries },
            sortOption = { sortOption },
            onSortChanged = { sortOption = it },
            sortOptionLabel = { Text(stringResource(it.textRes)) },
            sortAscending = { sortAscending },
            onSortAscendingChange = { sortAscending = it },
        )

        HorizontalDivider()

        SeriesFilterSection(
            expanded = { Section.SERIES in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.SERIES) },
            state = saveableState.series,
            lockedSeries = state.lockedSeries,
            autocompleteResults = seriesAutocompleteResults,
        )

        HorizontalDivider()

        MerchTagSection2(
            expanded = { Section.MERCH in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.MERCH) },
            state = saveableState.merch,
            merchTagData = state.merchTagData,
            merchIdsLockedIn = state.merchIdsLockedIn,
            sectionHeader = { Text(stringResource(Res.string.alley_stamp_rally_filter_merch)) },
            sectionHeaderDropdownContentDescriptionRes = Res.string.alley_stamp_rally_filter_merch_content_description,
            header = {
                Text(
                    text = stringResource(Res.string.alley_stamp_rally_filter_fandom_merch_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(
                        start = 32.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    )
                )
            },
        )

        HorizontalDivider()

        MerchTagSection2(
            expanded = { Section.PRIZE_MERCH in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.PRIZE_MERCH) },
            state = saveableState.prizeMerch,
            merchTagData = state.merchTagData,
            merchIdsLockedIn = { emptySet() },
            sectionHeader = { Text(stringResource(Res.string.alley_stamp_rally_filter_prize_merch)) },
            sectionHeaderDropdownContentDescriptionRes = Res.string.alley_stamp_rally_filter_prize_merch_content_description,
        )

        HorizontalDivider()

        RangeDataFilterSection2(
            expanded = { Section.TOTAL_COST in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.TOTAL_COST) },
            state = saveableState.totalCost,
            header = { Text(stringResource(Res.string.alley_stamp_rally_filter_total_cost)) },
            headerDropdownContentDescriptionRes = Res.string.alley_stamp_rally_filter_total_cost_expand_content_description,
        )

        HorizontalDivider()

        RangeDataFilterSection2(
            expanded = { Section.PRIZE_LIMIT in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.PRIZE_LIMIT) },
            state = saveableState.prizeLimit,
            header = { Text(stringResource(Res.string.alley_stamp_rally_filter_prize_limit)) },
            headerDropdownContentDescriptionRes = Res.string.alley_stamp_rally_filter_prize_limit_expand_content_description,
        )

        HorizontalDivider()

        AdvancedSection(
            expanded = { Section.ADVANCED in saveableState.expandedSections },
            onExpandedChange = { saveableState.expandedSections.toggle(Section.ADVANCED) },
            state = saveableState,
            persistentState = persistentState,
            showHideFavorited = showHideFavorited,
        )

        HorizontalDivider()
    }
}

@Composable
private fun AdvancedSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    state: StampRallySortFilterSaveableState,
    persistentState: StampRallySortFilterPersistentState,
    showHideFavorited: Boolean,
) {
    SectionGroup(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        header = { Text(stringResource(Res.string.alley_filter_advanced)) },
        headerDropdownContentDescriptionRes = Res.string.alley_filter_advanced_expand_content_description,
    ) { expandedState ->
        if (expandedState) {
            var showGridByDefault by persistentState.showGridByDefault.collectAsMutableStateWithLifecycle()
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_show_grid_by_default)) },
                enabled = { showGridByDefault },
                onEnabledChanged = { showGridByDefault = it },
            )

            HorizontalDivider()

            var showRandomCatalogImage by persistentState.showRandomCatalogImage.collectAsMutableStateWithLifecycle()
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_show_random_catalog_image)) },
                enabled = { showRandomCatalogImage },
                onEnabledChanged = { showRandomCatalogImage = it },
            )

            HorizontalDivider()

            var forceOneDisplayColumn by persistentState.forceOneDisplayColumn.collectAsMutableStateWithLifecycle()
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_force_one_display_column)) },
                enabled = { forceOneDisplayColumn },
                onEnabledChanged = { forceOneDisplayColumn = it },
            )

            HorizontalDivider()
        }

        if (showHideFavorited && (expandedState || state.hideFavorited)) {
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_hide_favorited)) },
                enabled = { state.hideFavorited },
                onEnabledChanged = { state.hideFavorited = it },
            )
        }

        if (showHideFavorited && (expandedState || (state.hideFavorited && !state.hideIgnored))) {
            HorizontalDivider()
        }

        if (expandedState || state.hideIgnored) {
            SwitchSection(
                title = { Text(stringResource(Res.string.alley_filter_hide_ignored)) },
                enabled = { state.hideIgnored },
                onEnabledChanged = { state.hideIgnored = it },
            )
        }
    }
}

class StampRallySortFilterState(
    val persistentState: StampRallySortFilterPersistentState,
    val saveableState: StampRallySortFilterSaveableState,
    val lockedSeries: () -> SeriesInfo?,
    val merchTagData: () -> MerchTagData,
    val merchIdsLockedIn: () -> Set<String>, // TODO: Not wired up
) {
    val activeCount get() = saveableState.activeCount

    fun clear() {
        Snapshot.withMutableSnapshot {
            saveableState.clear()
        }
    }

    companion object {
        @Composable
        fun rememberForPreview(): StampRallySortFilterState {
            assertInPreview()
            val persistentState = StampRallySortFilterPersistentState.rememberForPreview()
            val saveableState = rememberSerializable {
                StampRallySortFilterSaveableState()
                    .apply {
                        totalCost.data = RangeData(startString = "20", endString = "50")
                        prizeLimit.data = RangeData(startString = "0", endString = "25")
                    }
            }
            return remember(persistentState, saveableState) {
                StampRallySortFilterState(
                    persistentState = persistentState,
                    saveableState = saveableState,
                    lockedSeries = { null },
                    merchTagData = { MerchTagData(MerchEntryProvider.values.toList()) },
                    merchIdsLockedIn = { emptySet() },
                )
            }
        }
    }
}

class StampRallySortFilterPersistentState(
    val sortOption: MutableStateFlow<StampRallySearchSortOption>,
    val sortAscending: MutableStateFlow<Boolean>,
    val showGridByDefault: MutableStateFlow<Boolean>,
    val showRandomCatalogImage: MutableStateFlow<Boolean>,
    val forceOneDisplayColumn: MutableStateFlow<Boolean>,
) {
    companion object {
        @Composable
        fun rememberForPreview(): StampRallySortFilterPersistentState {
            assertInPreview()
            return StampRallySortFilterPersistentState(
                sortOption = MutableStateFlow(StampRallySearchSortOption.RANDOM),
                sortAscending = MutableStateFlow(true),
                showGridByDefault = MutableStateFlow(false),
                showRandomCatalogImage = MutableStateFlow(true),
                forceOneDisplayColumn = MutableStateFlow(false),
            )
        }
    }
}

@Serializable
class StampRallySortFilterSaveableState(
    @Serializable(SnapshotStateSetSerializer::class)
    val expandedSections: SnapshotStateSet<Section> = mutableStateSetOf(),
    val series: SeriesFilterState = SeriesFilterState(),
    val merch: TagSectionState = TagSectionState(),
    val prizeMerch: TagSectionState = TagSectionState(),
    val totalCost: RangeDataSectionState = RangeDataSectionState(RangeData(100)),
    val prizeLimit: RangeDataSectionState = RangeDataSectionState(RangeData(50)),
    // TODO: Store hides persistently?
    @Serializable(MutableStateSerializer::class)
    private val _hideFavorited: MutableState<Boolean> = mutableStateOf(false),
    @Serializable(MutableStateSerializer::class)
    private val _hideIgnored: MutableState<Boolean> = mutableStateOf(false),
) {
    var hideFavorited by _hideFavorited
    var hideIgnored by _hideIgnored

    val activeCount by derivedStateOf {
        var count = 0
        if (!series.isDefault) count++
        if (!merch.isDefault) count++
        if (!prizeMerch.isDefault) count++
        if (!totalCost.isDefault) count++
        if (!prizeLimit.isDefault) count++
        count
    }

    fun clear() {
        Snapshot.withMutableSnapshot {
            series.clear()
            merch.clear()
            prizeMerch.clear()
            totalCost.clear()
            prizeLimit.clear()
        }
    }

    enum class Section {
        SORT,
        SERIES,
        MERCH,
        PRIZE_MERCH,
        TOTAL_COST,
        PRIZE_LIMIT,
        ADVANCED,
    }
}

@Composable
private fun StampRallySortFilterSheetContentPreview(
    state: StampRallySortFilterState,
    scrollState: ScrollState = rememberScrollState(),
) {
    StampRallySortFilterSheetContent(
        state = state,
        scrollState = scrollState,
        sheetState = rememberBottomSheetState(SheetValue.PartiallyExpanded),
        seriesAutocompleteResults = { emptyList() },
    )
}

@AlleyPreview
@Composable
private fun StampRallySortFilterSheetContentPreview() {
    StampRallySortFilterSheetContentPreview(StampRallySortFilterState.rememberForPreview())
}

@AlleyPreview
@Composable
private fun StampRallySortFilterSheetContentExpandedPreview0() {
    val state = StampRallySortFilterState.rememberForPreview().apply {
        saveableState.expandedSections.addAll(Section.entries.take(4))
    }
    StampRallySortFilterSheetContentPreview(state)
}

@AlleyPreview
@Composable
private fun StampRallySortFilterSheetContentExpandedPreview1() {
    val scrollState = rememberScrollState(1000)
    val state = StampRallySortFilterState.rememberForPreview().apply {
        saveableState.expandedSections.addAll(Section.entries.drop(4))
    }
    StampRallySortFilterSheetContentPreview(state, scrollState)
}
