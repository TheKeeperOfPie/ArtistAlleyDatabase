package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        viewModel: FavoritesSortFilterViewModel,
        mapViewModel: MapViewModel,
        mapTransformState: MapScreen.TransformState,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
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
                    state = viewModel.state,
                    modifier = Modifier.fillMaxWidth()
                )
            },
        ) {
            val showOnlyFavorites by viewModel.settings.showOnlyFavorites.collectAsStateWithLifecycle()
            MapScreen(
                viewModel = mapViewModel,
                transformState = mapTransformState,
                showSlider = false,
                modifier = Modifier.padding(it)
            ) { table ->
                if (showOnlyFavorites) {
                    HighlightedTableCell(
                        mapViewModel = mapViewModel,
                        table = table,
                        highlight = table.favorite,
                        showImages = table.favorite,
                        onArtistClick = onArtistClick,
                    )
                } else {
                    TableCell(
                        mapViewModel = mapViewModel,
                        table = table,
                        onArtistClick = onArtistClick,
                    )
                }
            }
        }
    }
}