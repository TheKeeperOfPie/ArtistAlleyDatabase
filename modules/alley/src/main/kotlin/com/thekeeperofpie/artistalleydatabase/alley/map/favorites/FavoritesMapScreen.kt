package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.map.HighlightedTableCell
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen.ZoomSlider
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.TableCell
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterOptionsPanel

@OptIn(ExperimentalMaterial3Api::class)
object FavoritesMapScreen {

    @Composable
    operator fun invoke(
        mapTransformState: MapScreen.TransformState,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        val viewModel = hiltViewModel<FavoritesMapViewModel>()
        BottomSheetScaffold(
            sheetPeekHeight = 80.dp,
            sheetDragHandle = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ZoomSlider(
                        transformState = mapTransformState,
                        modifier = Modifier
                            .heightIn(max = 64.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .align(Alignment.Center)
                    )
                }
            },
            sheetContent = {
                SortFilterOptionsPanel(
                    sections = { viewModel.sortFilterController.sections },
                    sectionState = { viewModel.sortFilterController.state },
                    modifier = Modifier.fillMaxWidth()
                )
            },
        ) {
            val mapViewModel = hiltViewModel<MapViewModel>()
            val showOnlyFavorites by viewModel.sortFilterController.onlyFavoritesSection
                .property.collectAsState()
            MapScreen(
                transformState = mapTransformState,
                showSlider = false,
                modifier = Modifier.padding(it)
            ) { table ->
                if (showOnlyFavorites) {
                    HighlightedTableCell(
                        table = table,
                        highlight = table.favorite,
                        showImages = table.favorite,
                        onArtistClick = onArtistClick,
                    )
                } else {
                    TableCell(table = table, onArtistClick = onArtistClick)
                }
            }
        }
    }
}
