package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import kotlinx.coroutines.CoroutineScope

class ArtistSortFilterController(scope: CoroutineScope, settings: ArtistAlleySettings) :
    SortFilterController<ArtistSortFilterController.FilterParams>(scope) {

    val boothSection = EntrySection.LongText(headerRes = R.string.alley_search_option_booth)
    val artistSection = EntrySection.LongText(headerRes = R.string.alley_search_option_artist)
    val summarySection =
        EntrySection.LongText(headerRes = R.string.alley_search_option_summary)
    val seriesSection = EntrySection.MultiText(
        R.string.alley_series_header_zero,
        R.string.alley_series_header_one,
        R.string.alley_series_header_many,
    )
    val merchSection = EntrySection.MultiText(
        R.string.alley_search_option_merch_zero,
        R.string.alley_search_option_merch_one,
        R.string.alley_search_option_merch_many,
    )

    private val sortSection = SortFilterSection.SortBySetting<ArtistSearchSortOption>(
        enumClass = ArtistSearchSortOption::class,
        headerTextRes = R.string.alley_sort_label,
        sortProperty = settings.artistsSortOption,
        sortAscendingProperty = settings.artistsSortAscending,
        deserialize = {
            ArtistSearchSortOption.entries.find { option -> option.name == it }
                ?: ArtistSearchSortOption.RANDOM
        },
        serialize = { it?.name.orEmpty() },
    )
    val onlyFavoritesSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_favorites,
        settings.showOnlyFavorites,
    )
    val onlyCatalogImagesSection =
        SortFilterSection.Switch(R.string.alley_filter_only_catalogs, false)
    val gridByDefaultSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_show_grid_by_default,
        settings.showGridByDefault,
    )
    val randomCatalogImageSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_show_random_catalog_image,
        settings.showRandomCatalogImage,
    )
    private val onlyConfirmedTagsSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_show_only_confirmed_tags,
        settings.showOnlyConfirmedTags,
    )
    private val showIgnoredSection =
        SortFilterSection.Switch(R.string.alley_filter_show_ignored, true)
    val forceOneDisplayColumnSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_force_one_display_column,
        settings.forceOneDisplayColumn
    )

    override val sections = listOf(
        sortSection,
        onlyFavoritesSection,
        onlyCatalogImagesSection,
        gridByDefaultSection,
        randomCatalogImageSection,
        onlyConfirmedTagsSection,
        showIgnoredSection,
        forceOneDisplayColumnSection,
    )

    @Composable
    override fun filterParams(): FilterParams {
        val seriesContents = seriesSection.finalContents()
        return FilterParams(
            booth = boothSection.value.trim(),
            artist = artistSection.value.trim(),
            summary = summarySection.value.trim(),
            series = seriesContents.filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            seriesById = seriesContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::mediaId),
            merch = merchSection.finalContents().map { it.serializedValue },
            sortOption = sortSection.sortOptions.selectedOption(ArtistSearchSortOption.RANDOM),
            sortAscending = sortSection.sortAscending,
            showOnlyFavorites = onlyFavoritesSection.property.collectAsState().value,
            showOnlyWithCatalog = onlyCatalogImagesSection.enabled,
            showOnlyConfirmedTags = onlyConfirmedTagsSection.property.collectAsState().value,
            showIgnored = showIgnoredSection.enabled,
        )
    }

    data class FilterParams(
        val booth: String?,
        val artist: String?,
        val summary: String?,
        val series: List<String>,
        val seriesById: List<String>,
        val merch: List<String>,
        val sortOption: ArtistSearchSortOption,
        val sortAscending: Boolean,
        val showOnlyFavorites: Boolean,
        val showOnlyWithCatalog: Boolean,
        val showOnlyConfirmedTags: Boolean,
        val showIgnored: Boolean,
    )
}
