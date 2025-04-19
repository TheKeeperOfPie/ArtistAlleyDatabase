package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_hide_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_only_catalogs
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_has_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_artist
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_booth
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_merch_many
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_merch_one
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_merch_zero
import artistalleydatabase.modules.alley.generated.resources.alley_search_option_summary
import artistalleydatabase.modules.alley.generated.resources.alley_series_header_many
import artistalleydatabase.modules.alley.generated.resources.alley_series_header_one
import artistalleydatabase.modules.alley.generated.resources.alley_series_header_zero
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
class ArtistSortFilterViewModel(
    // TODO: Hide this
    val settings: ArtistAlleySettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // TODO: Add specific search filters
    val boothSection = EntrySection.LongText(headerRes = Res.string.alley_search_option_booth)
    val artistSection = EntrySection.LongText(headerRes = Res.string.alley_search_option_artist)
    val summarySection =
        EntrySection.LongText(headerRes = Res.string.alley_search_option_summary)
    val seriesSection = EntrySection.MultiText(
        Res.string.alley_series_header_zero,
        Res.string.alley_series_header_one,
        Res.string.alley_series_header_many,
    )
    val merchSection = EntrySection.MultiText(
        Res.string.alley_search_option_merch_zero,
        Res.string.alley_search_option_merch_one,
        Res.string.alley_search_option_merch_many,
    )

    val sortOption = settings.artistsSortOption
    val sortAscending = settings.artistsSortAscending
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.alley_sort_label,
        defaultSort = ArtistSearchSortOption.RANDOM,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    val onlyCatalogImages = savedStateHandle.getMutableStateFlow("onlyCatalogImages", false)
    private val onlyCatalogImagesSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_only_catalogs,
        defaultEnabled = false,
        enabled = onlyCatalogImages
    )

    private val gridByDefaultSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_grid_by_default,
        property = settings.showGridByDefault,
        default = false,
    )

    private val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_random_catalog_image,
        property = settings.showRandomCatalogImage,
        default = false,
    )

    private val onlyConfirmedTagsSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_only_confirmed_tags,
        property = settings.showOnlyConfirmedTags,
        default = false,
    )

    private val onlyHasCommissionsSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_only_has_commissions,
        settings.showOnlyHasCommissions,
        default = false,
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
    )

    private val sections = listOf(
        sortSection,
        onlyCatalogImagesSection,
        gridByDefaultSection,
        randomCatalogImageSection,
        onlyConfirmedTagsSection,
        onlyHasCommissionsSection,
        hideIgnoredSection,
        forceOneDisplayColumnSection,
    )

    private val filterParams = combineStates(
        snapshotFlow {
            val seriesContents = seriesSection.finalContents()
            SnapshotState(
                booth = boothSection.value.trim(),
                artist = artistSection.value.trim(),
                summary = summarySection.value.trim(),
                series = seriesContents.map { it.serializedValue }
                    .filterNot(String::isBlank),
                merch = merchSection.finalContents().map { it.serializedValue },
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SnapshotState()),
        sortOption,
        settings.artistsSortAscending,
        onlyCatalogImages,
        settings.showOnlyConfirmedTags,
        settings.showOnlyHasCommissions,
        hideIgnored,
    ) {
        val snapshotState = it[0] as SnapshotState
        FilterParams(
            booth = snapshotState.booth,
            artist = snapshotState.artist,
            summary = snapshotState.summary,
            series = snapshotState.series,
            merch = snapshotState.merch,
            sortOption = it[1] as ArtistSearchSortOption,
            sortAscending = it[2] as Boolean,
            showOnlyWithCatalog = it[3] as Boolean,
            showOnlyConfirmedTags = it[4] as Boolean,
            showOnlyHasCommissions = it[5] as Boolean,
            hideIgnored = it[6] as Boolean,
        )
    }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    private data class SnapshotState(
        val booth: String? = null,
        val artist: String? = null,
        val summary: String? = null,
        val series: List<String> = emptyList(),
        val merch: List<String> = emptyList(),
    )

    data class FilterParams(
        val booth: String?,
        val artist: String?,
        val summary: String?,
        val series: List<String>,
        val merch: List<String>,
        val sortOption: ArtistSearchSortOption,
        val sortAscending: Boolean,
        val showOnlyWithCatalog: Boolean,
        val showOnlyConfirmedTags: Boolean,
        val showOnlyHasCommissions: Boolean,
        val hideIgnored: Boolean,
    )
}
