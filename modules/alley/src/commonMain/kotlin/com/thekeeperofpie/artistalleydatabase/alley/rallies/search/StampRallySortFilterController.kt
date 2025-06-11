package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced
import artistalleydatabase.modules.alley.generated.resources.alley_filter_advanced_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_favorited
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_artist
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_fandom
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_tables
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_prize_limit_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_show_unconfirmed
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_total_cost
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_filter_total_cost_expand_content_description
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesAutocompleteSection
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json

class StampRallySortFilterController(
    scope: CoroutineScope,
    lockedSeriesEntry: StateFlow<SeriesEntry?>,
    dispatchers: CustomDispatchers,
    seriesEntryDao: SeriesEntryDao,
    seriesImagesStore: SeriesImagesStore,
    val settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
    allowHideFavorited: Boolean,
) {

    val fandomSection = EntrySection.LongText(headerRes = Res.string.alley_search_option_fandom)

    // TODO: Multi-option
    val tablesSection = EntrySection.LongText(headerRes = Res.string.alley_search_option_tables)

    // TODO: Artists not wired up
    val artistsSection = EntrySection.LongText(headerRes = Res.string.alley_search_option_artist)

    val sortOption = settings.stampRalliesSortOption
    val sortAscending = settings.stampRalliesSortAscending
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.alley_sort_label,
        defaultSort = StampRallySearchSortOption.RANDOM,
        sortOption = sortOption,
        sortAscending = sortAscending,
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
        seriesImagesStore = seriesImagesStore,
        savedStateHandle = savedStateHandle,
    )

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

    private val sections = listOf(
        sortSection,
        seriesAutocompleteSection.section,
        totalCostSection,
        prizeLimitSection,
        showUnconfirmedSection,
        advancedSection,
    )

    @Suppress("UNCHECKED_CAST")
    private val filterParams = combineStates(
        snapshotFlow {
            SnapshotState(
                fandom = fandomSection.value.trim(),
                tables = tablesSection.value.trim(),
            )
        }.stateIn(scope, SharingStarted.Eagerly, SnapshotState()),
        sortOption,
        settings.stampRalliesSortAscending,
        lockedSeriesEntry,
        seriesAutocompleteSection.seriesIn,
        totalCost,
        prizeLimit,
        showUnconfirmed,
        hideFavorited,
        hideIgnored,
    ) {
        val snapshotState = it[0] as SnapshotState
        FilterParams(
            fandom = snapshotState.fandom,
            tables = snapshotState.tables,
            sortOption = it[1] as StampRallySearchSortOption,
            sortAscending = it[2] as Boolean,
            seriesIn = setOfNotNull((it[3] as SeriesEntry?)?.id) +
                    (it[4] as List<SeriesAutocompleteSection.SeriesFilterEntry>).map { it.id },
            totalCost = it[5] as RangeData,
            prizeLimit = it[6] as RangeData,
            showUnconfirmed = it[7] as Boolean,
            hideFavorited = it[8] as Boolean,
            hideIgnored = it[9] as Boolean,
        )
    }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    private data class SnapshotState(
        val fandom: String? = null,
        val tables: String? = null,
    )

    data class FilterParams(
        val fandom: String?,
        val tables: String?,
        val sortOption: StampRallySearchSortOption,
        val sortAscending: Boolean,
        val seriesIn: Set<String>,
        val totalCost: RangeData,
        val prizeLimit: RangeData,
        val showUnconfirmed: Boolean,
        val hideFavorited: Boolean,
        val hideIgnored: Boolean,
    )
}
