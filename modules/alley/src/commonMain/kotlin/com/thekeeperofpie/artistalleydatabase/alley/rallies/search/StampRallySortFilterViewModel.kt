package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_artist
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_fandom
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_tables
import artistalleydatabase.modules.alley.generated.resources.alley_sort_label
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class StampRallySortFilterViewModel(
    val settings: ArtistAlleySettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

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

    private val gridByDefaultSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_grid_by_default,
        settings.showGridByDefault,
    )

    private val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_random_catalog_image,
        settings.showRandomCatalogImage,
    )

    private val showIgnored = savedStateHandle.getMutableStateFlow("showIgnored", true)
    private val showIgnoredSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_show_ignored,
        defaultEnabled = true,
        enabled = showIgnored,
    )
    private val forceOneDisplayColumnSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_force_one_display_column,
        settings.forceOneDisplayColumn,
    )

    private val sections = listOf(
        sortSection,
        gridByDefaultSection,
        randomCatalogImageSection,
        showIgnoredSection,
        forceOneDisplayColumnSection,
    )

    private val filterParams = combineStates(
        snapshotFlow {
            SnapshotState(
                fandom = fandomSection.value.trim(),
                tables = tablesSection.value.trim(),
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SnapshotState()),
        sortOption,
        settings.stampRalliesSortAscending,
        showIgnored,
    ) {
        val snapshotState = it[0] as SnapshotState
        FilterParams(
            fandom = snapshotState.fandom,
            tables = snapshotState.tables,
            sortOption = it[1] as StampRallySearchSortOption,
            sortAscending = it[2] as Boolean,
            showIgnored = it[3] as Boolean,
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
        val showIgnored: Boolean,
    )
}
