package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import kotlinx.coroutines.CoroutineScope

class StampRallySortFilterController(scope: CoroutineScope, settings: ArtistAlleySettings) :
    SortFilterController<StampRallySortFilterController.FilterParams>(scope) {

    val fandomSection = EntrySection.LongText(headerRes = R.string.alley_search_option_fandom)

    // TODO: Multi-option
    val tablesSection = EntrySection.LongText(headerRes = R.string.alley_search_option_tables)
    val artistsSection = EntrySection.LongText(headerRes = R.string.alley_search_option_artist)

    private val sortSection = SortFilterSection.Sort<StampRallySearchSortOption>(
        enumClass = StampRallySearchSortOption::class,
        defaultEnabled = StampRallySearchSortOption.RANDOM,
        headerTextRes = R.string.alley_sort_label,
    )
    val onlyFavoritesSection =
        SortFilterSection.Switch(R.string.alley_filter_favorites, false)
    val gridByDefaultSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_show_grid_by_default,
        settings.showGridByDefault
    )
    val randomCatalogImageSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_show_random_catalog_image,
        settings.showRandomCatalogImage
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
        gridByDefaultSection,
        randomCatalogImageSection,
        showIgnoredSection,
        forceOneDisplayColumnSection,
    )

    @Composable
    override fun filterParams() = FilterParams(
        fandom = fandomSection.value.trim(),
        tables = tablesSection.value.trim(),
        sortOption = sortSection.sortOptions.selectedOption(StampRallySearchSortOption.RANDOM),
        sortAscending = sortSection.sortAscending,
        showOnlyFavorites = onlyFavoritesSection.enabled,
        showIgnored = showIgnoredSection.enabled,
    )

    data class FilterParams(
        val fandom: String?,
        val tables: String?,
        val sortOption: StampRallySearchSortOption,
        val sortAscending: Boolean,
        val showOnlyFavorites: Boolean,
        val showIgnored: Boolean,
    )
}
