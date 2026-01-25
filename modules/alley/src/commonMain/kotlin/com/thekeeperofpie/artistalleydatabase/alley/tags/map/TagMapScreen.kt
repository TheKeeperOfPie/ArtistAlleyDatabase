package com.thekeeperofpie.artistalleydatabase.alley.tags.map

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.map.HighlightedTableCell
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton

@OptIn(ExperimentalMaterial3Api::class)
object TagMapScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        year: DataYear?,
        series: String?,
        merch: String?,
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        viewModel: TagMapViewModel = viewModel {
            graph.tagMapViewModelFactory.create(
                year = year,
                series = series,
                merch = merch,
                savedStateHandle = createSavedStateHandle(),
            )
        },
        mapViewModel: MapViewModel = viewModel {
            graph.mapViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        TagMapScreen(
            series = series,
            merch = merch,
            viewModel = viewModel,
            mapViewModel = mapViewModel,
            onClickBack = onClickBack,
            onArtistClick = onArtistClick,
        )
    }

    @Composable
    operator fun invoke(
        series: String?,
        merch: String?,
        viewModel: TagMapViewModel,
        mapViewModel: MapViewModel,
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(series ?: merch ?: "") },
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
