package com.thekeeperofpie.artistalleydatabase.alley.tags.map

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.map.HighlightedTableCell
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton

@OptIn(ExperimentalMaterial3Api::class)
object TagMapScreen {

    @Composable
    operator fun invoke(
        viewModel: TagMapViewModel,
        mapViewModel: MapViewModel,
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = {
                        val route = viewModel.route
                        Text(route.series ?: route.merch ?: "")
                    },
                )
            },
        ) {
            val gridData = mapViewModel.gridData.result ?: return@Scaffold

            val highlightedBooths = viewModel.booths
            val targetTable = gridData.tables.find { highlightedBooths.contains(it.booth) }
            val transformState = MapScreen.rememberTransformState(initialScale = 0.5f)
            MapScreen(
                viewModel = mapViewModel,
                transformState = transformState,
                initialGridPosition = targetTable?.run { IntOffset(gridX, gridY) },
                bottomContentPadding = it.calculateBottomPadding(),
            ) {
                HighlightedTableCell(
                    mapViewModel = mapViewModel,
                    table = it,
                    highlight = highlightedBooths.contains(it.booth),
                    onArtistClick = onArtistClick,
                )
            }
        }
    }
}
