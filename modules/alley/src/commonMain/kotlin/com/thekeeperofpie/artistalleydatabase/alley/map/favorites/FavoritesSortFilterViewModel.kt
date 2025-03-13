package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_favorites
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class FavoritesSortFilterViewModel(
    val settings: ArtistAlleySettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val showOnlyFavorites =
        savedStateHandle.getMutableStateFlow("showOnlyFavorites") { false }
    val onlyFavoritesSection = SortFilterSectionState.Switch(
        Res.string.alley_filter_favorites,
        defaultEnabled = false,
        enabled = showOnlyFavorites,
    )

    val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_random_catalog_image,
        settings.showRandomCatalogImage,
    )

    private val sections = listOf(
        onlyFavoritesSection,
        randomCatalogImageSection,
    )

    private val filterParams =
        combineStates(showOnlyFavorites, settings.showRandomCatalogImage) {
            FilterParams(
                showOnlyFavorites = it[0] as Boolean,
                showRandomCatalogImage = it[1] as Boolean,
            )
        }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    data class FilterParams(
        val showOnlyFavorites: Boolean,
        val showRandomCatalogImage: Boolean,
    )
}
