package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.DropdownFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldSheetContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SwitchSection
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FavoritesMapFilterSheetContent(
    state: FavoritesMapFilterState,
    sheetState: SheetState,
    scrollState: ScrollState = rememberScrollState(),
) {
    SortFilterBottomScaffoldSheetContent(
        sheetState = sheetState,
        scrollState = scrollState,
    ) {
        DropdownFilterSection(
            options = DataYear.entries,
            selected = state.dataYear(),
            onSelectedChanged = state.onDataYearChange,
            label = { Text(stringResource(it.shortName)) },
        )

        // TODO: This doesn't actually work
//        HorizontalDivider()
//
//        val showRandomCatalogImage = state.showRandomCatalogImage()
//        SwitchSection(
//            title = { Text(stringResource(Res.string.alley_filter_show_random_catalog_image)) },
//            enabled = { showRandomCatalogImage },
//            onEnabledChanged = state.onShowRandomCatalogImageChanged,
//        )

        HorizontalDivider()

        val showOnlyConfirmedTags = state.showOnlyConfirmedTags()
        SwitchSection(
            title = { Text(stringResource(Res.string.alley_filter_show_only_confirmed_tags)) },
            enabled = { showOnlyConfirmedTags },
            onEnabledChanged = state.onShowOnlyConfirmedTagsChanged,
        )
    }
}

class FavoritesMapFilterState(
    val dataYear: @Composable () -> DataYear,
    val onDataYearChange: (DataYear) -> Unit,
    val showOnlyConfirmedTags: @Composable () -> Boolean,
    val onShowOnlyConfirmedTagsChanged: (Boolean) -> Unit,
    val showRandomCatalogImage: @Composable () -> Boolean,
    val onShowRandomCatalogImageChanged: (Boolean) -> Unit,
)
