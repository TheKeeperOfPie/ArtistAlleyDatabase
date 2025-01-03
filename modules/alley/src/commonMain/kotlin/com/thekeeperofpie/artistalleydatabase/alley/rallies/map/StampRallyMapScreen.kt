package com.thekeeperofpie.artistalleydatabase.alley.rallies.map

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.map.HighlightedTableCell
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton

@OptIn(ExperimentalMaterial3Api::class)
object StampRallyMapScreen {

    @Composable
    operator fun invoke(
        viewModel: StampRallyMapViewModel,
        mapViewModel: MapViewModel,
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(viewModel.stampRally?.fandom.orEmpty()) },
                )
            },
        ) {
            val stampRally = viewModel.stampRally ?: return@Scaffold
            val gridData = mapViewModel.gridData.result ?: return@Scaffold
            val targetTable = gridData.tables.find { it.booth == stampRally.hostTable }
            val transformState = MapScreen.rememberTransformState(initialScale = 0.5f)
            MapScreen(
                viewModel = mapViewModel,
                transformState = transformState,
                initialGridPosition = targetTable?.run { IntOffset(gridX, gridY) },
                modifier = Modifier.padding(it)
            ) {
                HighlightedTableCell(
                    mapViewModel = mapViewModel,
                    table = it,
                    highlight = viewModel.artistTables.contains(it.booth),
                    onArtistClick = onArtistClick,
                )
            }
        }
    }
}
