package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_favorited
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_merch
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_merch_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_limit_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_merch
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_merch_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_show_unconfirmed
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_total_cost
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_total_cost_expand_content_description
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchCache
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagSection
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesAutocompleteSection
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.serialization.json.Json

class StampRallySortFilterController(
    scope: CoroutineScope,
    lockedSeriesEntry: StateFlow<SeriesInfo?>,
    dispatchers: CustomDispatchers,
    dataYear: StateFlow<DataYear>,
    merchCache: MerchCache,
    seriesEntryDao: SeriesEntryDao,
    seriesImageLoader: SeriesImageLoader,
    val settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
    private val allowHideFavorited: Boolean,
    private val showUnconfirmedOption: Boolean,
) {
    val sortOption = settings.stampRalliesSortOption
    val sortAscending = settings.stampRalliesSortAscending
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.alley_sort_label,
        defaultSort = StampRallySearchSortOption.RANDOM,
        sortOption = sortOption,
        sortAscending = sortAscending,
        allowClear = false,
    )

    private val totalCostInitialData = RangeData(100)
    private val totalCost = savedStateHandle.getMutableStateFlow(
        scope = scope,
        json = Json,
        key = "totalCost",
        initialValue = { totalCostInitialData },
    )
    private val totalCostSection = SortFilterSectionState.Range(
        title = Res.string.alley_stamp_rally_filter_total_cost,
        titleDropdownContentDescription = Res.string.alley_stamp_rally_filter_total_cost_expand_content_description,
        initialData = totalCostInitialData,
        data = totalCost,
        unboundedMax = true,
    )

    private val prizeLimitInitialData = RangeData(50)
    private val prizeLimit = savedStateHandle.getMutableStateFlow(
        scope = scope,
        json = Json,
        key = "prizeLimit",
        initialValue = { prizeLimitInitialData },
    )
    private val prizeLimitSection = SortFilterSectionState.Range(
        title = Res.string.alley_stamp_rally_filter_prize_limit,
        titleDropdownContentDescription = Res.string.alley_stamp_rally_filter_prize_limit_expand_content_description,
        initialData = prizeLimitInitialData,
        data = prizeLimit,
        unboundedMax = true,
    )

    private val seriesAutocompleteSection = SeriesAutocompleteSection(
        scope = scope,
        dispatchers = dispatchers,
        lockedSeriesEntry = lockedSeriesEntry,
        seriesEntryDao = seriesEntryDao,
        seriesImageLoader = seriesImageLoader,
        savedStateHandle = savedStateHandle,
    )

    private val merch = dataYear.flatMapLatest(merchCache::merchTags)
    private val merchIdIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<String>>(
            scope = scope,
            key = "merchIdIn",
            initialValue = { emptySet() },
            serialize = { it.toList() },
            deserialize = { it.toSet() },
        )
    private val merchSearchQuery = savedStateHandle.getMutableStateFlow("merchSearchQuery", "")
    private val merchSection = object : SortFilterSectionState.Custom("merch") {
        override fun clear() {
            merchIdIn.value = emptySet()
            merchSearchQuery.value = ""
        }

        @Composable
        override fun isDefault() = merchIdIn.collectAsState().value.isEmpty()
                && merchSearchQuery.collectAsState().value.isEmpty()

        @Composable
        override fun Content(state: SortFilterExpandedState, showDivider: Boolean) {
            val tagData by merch.collectAsStateWithLifecycle(MerchTagData(emptyList()))
            var idIn by merchIdIn.collectAsMutableStateWithLifecycle()
            var searchQuery by merchSearchQuery.collectAsMutableStateWithLifecycle()

            MerchTagSection(
                merchTagData = { tagData },
                merchIdIn = { idIn },
                onMerchIdInChange = { idIn = it },
                merchIdsLockedIn = emptySet(),
                searchQuery = { searchQuery },
                onSearchQueryChange = { searchQuery = it },
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                showDivider = showDivider,
                titleRes = Res.string.alley_stamp_rally_filter_merch,
                titleDropdownContentDescriptionRes = Res.string.alley_stamp_rally_filter_merch_content_description,
            )
        }
    }

    private val prizeMerchIdIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<String>>(
            scope = scope,
            key = "prizeMerchIdIn",
            initialValue = { emptySet() },
            serialize = { it.toList() },
            deserialize = { it.toSet() },
        )
    private val prizeMerchSearchQuery = savedStateHandle.getMutableStateFlow("prizeMerchSearchQuery", "")
    private val prizeMerchSection = object : SortFilterSectionState.Custom("prizeMerch") {
        override fun clear() {
            prizeMerchIdIn.value = emptySet()
            prizeMerchSearchQuery.value = ""
        }

        @Composable
        override fun isDefault() = prizeMerchIdIn.collectAsState().value.isEmpty()
                && prizeMerchSearchQuery.collectAsState().value.isEmpty()

        @Composable
        override fun Content(state: SortFilterExpandedState, showDivider: Boolean) {
            val tagData by merch.collectAsStateWithLifecycle(MerchTagData(emptyList()))
            var idIn by prizeMerchIdIn.collectAsMutableStateWithLifecycle()
            var searchQuery by prizeMerchSearchQuery.collectAsMutableStateWithLifecycle()

            MerchTagSection(
                merchTagData = { tagData },
                merchIdIn = { idIn },
                onMerchIdInChange = { idIn = it },
                merchIdsLockedIn = emptySet(),
                searchQuery = { searchQuery },
                onSearchQueryChange = { searchQuery = it },
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                showDivider = showDivider,
                titleRes = Res.string.alley_stamp_rally_filter_prize_merch,
                titleDropdownContentDescriptionRes = Res.string.alley_stamp_rally_filter_prize_merch_content_description,
            )
        }
    }

    private val showUnconfirmed = savedStateHandle.getMutableStateFlow("showUnconfirmed", false)
    private val showUnconfirmedSection = SortFilterSectionState.Switch(
        title = Res.string.alley_stamp_rally_filter_show_unconfirmed,
        defaultEnabled = false,
        enabled = showUnconfirmed,
    )

    private val gridByDefaultSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_grid_by_default,
        property = settings.showGridByDefault,
        default = false,
        allowClear = true,
    )

    private val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_random_catalog_image,
        property = settings.showRandomCatalogImage,
        default = false,
        allowClear = true,
    )

    private val hideFavorited = if (allowHideFavorited) {
        savedStateHandle.getMutableStateFlow("hideFavorited", false)
    } else {
        MutableStateFlow(false)
    }
    private val hideFavoritedSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_hide_favorited,
        defaultEnabled = false,
        enabled = hideFavorited,
    )

    private val hideIgnored = savedStateHandle.getMutableStateFlow("hideIgnored", false)
    private val hideIgnoredSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_hide_ignored,
        defaultEnabled = false,
        enabled = hideIgnored,
    )

    private val forceOneDisplayColumnSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_force_one_display_column,
        property = settings.forceOneDisplayColumn,
        default = false,
        allowClear = true,
    )

    val advancedSection = SortFilterSectionState.Group(
        title = Res.string.alley_filter_advanced,
        titleDropdownContentDescription = Res.string.alley_filter_advanced_expand_content_description,
        children = MutableStateFlow(
            listOfNotNull(
                gridByDefaultSection,
                randomCatalogImageSection,
                hideFavoritedSection.takeIf { allowHideFavorited },
                hideIgnoredSection,
                forceOneDisplayColumnSection,
            )
        )
    )

    private val sections = listOfNotNull(
        sortSection,
        seriesAutocompleteSection.section,
        merchSection,
        prizeMerchSection,
        totalCostSection,
        prizeLimitSection,
        showUnconfirmedSection.takeIf { showUnconfirmedOption },
        advancedSection,
    )

    @Suppress("UNCHECKED_CAST")
    private val filterParams = combineStates(
        sortOption,
        settings.stampRalliesSortAscending,
        lockedSeriesEntry,
        seriesAutocompleteSection.seriesIn,
        merchIdIn,
        prizeMerchIdIn,
        totalCost,
        prizeLimit,
        showUnconfirmed,
        hideFavorited,
        hideIgnored,
    ) {
        FilterParams(
            sortOption = it[0] as StampRallySearchSortOption,
            sortAscending = it[1] as Boolean,
            seriesIn = setOfNotNull((it[2] as SeriesInfo?)?.id) +
                    (it[3] as List<SeriesAutocompleteSection.SeriesFilterEntry>).map { it.id },
            merchIdIn = it[4] as Set<String>,
            prizeMerchIdIn = it[5] as Set<String>,
            totalCost = it[6] as RangeData,
            prizeLimit = it[7] as RangeData,
            showUnconfirmed = it[8] as Boolean,
            hideFavorited = it[9] as Boolean,
            hideIgnored = it[10] as Boolean,
        )
    }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    fun clear() {
        sections.forEach { it.clear() }
        hideFavoritedSection.clear()
    }

    data class FilterParams(
        val sortOption: StampRallySearchSortOption,
        val sortAscending: Boolean,
        val seriesIn: Set<String>,
        val merchIdIn: Set<String>,
        val prizeMerchIdIn: Set<String>,
        val totalCost: RangeData,
        val prizeLimit: RangeData,
        val showUnconfirmed: Boolean,
        val hideFavorited: Boolean,
        val hideIgnored: Boolean,
    )
}
