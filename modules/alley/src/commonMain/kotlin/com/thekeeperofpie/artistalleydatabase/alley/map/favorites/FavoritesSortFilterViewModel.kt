package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_data_year
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@Inject
class FavoritesSortFilterViewModel(
    val settings: ArtistAlleySettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_random_catalog_image,
        settings.showRandomCatalogImage,
    )

    val dataYearSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.alley_filter_data_year,
        values = DataYear.entries,
        valueToText = { stringResource(it.shortName) },
        property = settings.dataYear,
    )

    private val sections = listOf(
        dataYearSection,
        randomCatalogImageSection,
    )

    private val filterParams = settings.showRandomCatalogImage
        .mapState(viewModelScope) {
            FilterParams(
                showRandomCatalogImage = it,
            )
        }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    data class FilterParams(
        val showRandomCatalogImage: Boolean,
    )
}
