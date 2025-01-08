package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_favorites
import artistalleydatabase.modules.alley.generated.resources.alley_filter_force_one_display_column
import artistalleydatabase.modules.alley.generated.resources.alley_filter_only_catalogs
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_grid_by_default
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_ignored
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
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
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapMutableState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
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

    private val sortOption = settings.artistsSortOption
        .mapMutableState(
            viewModelScope,
            deserialize = {
                ArtistSearchSortOption.entries.find { option -> option.name == it }
                    ?: ArtistSearchSortOption.RANDOM
            },
            serialize = { it.name.orEmpty() },
        )
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.alley_sort_label,
        defaultSort = ArtistSearchSortOption.RANDOM,
        sortOption = sortOption,
        sortAscending = settings.artistsSortAscending,
    )
    private val onlyFavoritesSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_favorites,
        settings.showOnlyFavorites,
    )

    val onlyCatalogImages = savedStateHandle.getMutableStateFlow("onlyCatalogImages", false)
    private val onlyCatalogImagesSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_only_catalogs,
        defaultEnabled = false,
        enabled = onlyCatalogImages
    )

    private val gridByDefaultSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_grid_by_default,
        settings.showGridByDefault,
    )

    private val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_random_catalog_image,
        settings.showRandomCatalogImage,
    )

    private val onlyConfirmedTagsSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_only_confirmed_tags,
        settings.showOnlyConfirmedTags,
    )

    private val showIgnored = savedStateHandle.getMutableStateFlow("showIgnored", true)
    private val showIgnoredSection = SortFilterSectionState.Switch(
        title = Res.string.alley_filter_show_ignored,
        defaultEnabled = true,
        enabled = showIgnored,
    )

    private val forceOneDisplayColumnSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_force_one_display_column,
        settings.forceOneDisplayColumn
    )

    private val sections = listOf(
        sortSection,
        onlyFavoritesSection,
        onlyCatalogImagesSection,
        gridByDefaultSection,
        randomCatalogImageSection,
        onlyConfirmedTagsSection,
        showIgnoredSection,
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
        settings.showOnlyFavorites,
        onlyCatalogImages,
        settings.showOnlyConfirmedTags,
        showIgnored,
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
            showOnlyFavorites = it[3] as Boolean,
            showOnlyWithCatalog = it[4] as Boolean,
            showOnlyConfirmedTags = it[5] as Boolean,
            showIgnored = it[6] as Boolean,
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
        val showOnlyFavorites: Boolean,
        val showOnlyWithCatalog: Boolean,
        val showOnlyConfirmedTags: Boolean,
        val showIgnored: Boolean,
    )
}