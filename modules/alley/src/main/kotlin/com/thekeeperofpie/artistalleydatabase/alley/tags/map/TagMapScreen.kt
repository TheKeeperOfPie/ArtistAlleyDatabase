package com.thekeeperofpie.artistalleydatabase.alley.tags.map

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.map.HighlightedTableCell
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton

@OptIn(ExperimentalMaterial3Api::class)
object TagMapScreen {

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        val viewModel = hiltViewModel<TagMapViewModel>()
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
            val mapViewModel = hiltViewModel<MapViewModel>()
            val gridData = mapViewModel.gridData.result ?: return@Scaffold

            val highlightedBooths = viewModel.booths
            val targetTable = gridData.tables.find { highlightedBooths.contains(it.booth) }
            val transformState = MapScreen.rememberTransformState(initialScale = 0.5f)
            MapScreen(
                transformState = transformState,
                initialGridPosition = targetTable?.run { IntOffset(gridX, gridY) },
                modifier = Modifier.padding(it)
            ) {
                HighlightedTableCell(
                    table = it,
                    highlight = highlightedBooths.contains(it.booth),
                    onArtistClick = onArtistClick,
                )
            }
        }
    }
}
