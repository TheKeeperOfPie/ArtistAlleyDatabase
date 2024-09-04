package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope

class FavoritesSortFilterController(scope: CoroutineScope, settings: ArtistAlleySettings) :
    SortFilterController<FavoritesSortFilterController.FilterParams>(scope) {

    val onlyFavoritesSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_favorites,
        settings.showOnlyFavorites,
    )
    val randomCatalogImageSection = SortFilterSection.SwitchBySetting(
        R.string.alley_filter_show_random_catalog_image,
        settings.showRandomCatalogImage,
    )
    override val sections = listOf(
        onlyFavoritesSection,
        randomCatalogImageSection,
    )

    @Composable
    override fun filterParams() = FilterParams(
        showOnlyFavorites = onlyFavoritesSection.property.collectAsState().value,
    )

    data class FilterParams(
        val showOnlyFavorites: Boolean,
    )
}
