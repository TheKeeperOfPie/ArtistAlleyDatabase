package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.utils_compose.generated.resources.close
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen.TransformState
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen.ZoomSlider
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.DropdownFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldSheetContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SwitchSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.toggle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

@Composable
internal fun FavoritesMapFilterSheetContent(
    state: FavoritesMapFilterState,
    mapTransformState: TransformState,
    sheetState: SheetState,
    scrollState: ScrollState = rememberScrollState(),
) {
    val scope = rememberCoroutineScope()
    SortFilterBottomScaffoldSheetContent(
        sheetState = sheetState,
        scrollState = scrollState,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.padding(18.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Box(Modifier.size(width = 32.dp, height = 4.dp))
                }
                ZoomSlider(
                    transformState = mapTransformState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 64.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        },
        footer = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { scope.launch { sheetState.toggle() } }) {
                    Text(text = stringResource(UtilsComposeRes.string.close))
                }
            }
        },
    ) {
        DropdownFilterSection(
            options = DataYear.entries,
            selected = state.dataYear,
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
    val dataYear: () -> DataYear,
    val onDataYearChange: (DataYear) -> Unit,
    val showOnlyConfirmedTags: () -> Boolean,
    val onShowOnlyConfirmedTagsChanged: (Boolean) -> Unit,
    val showRandomCatalogImage: () -> Boolean,
    val onShowRandomCatalogImageChanged: (Boolean) -> Unit,
)
