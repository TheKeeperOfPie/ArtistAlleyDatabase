package com.thekeeperofpie.artistalleydatabase.alley.rallies.map

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.TableCell
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton

@OptIn(ExperimentalMaterial3Api::class)
object StampRallyMapScreen {

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        val stampRallyMapViewModel = hiltViewModel<StampRallyMapViewModel>()
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(stampRallyMapViewModel.stampRally?.fandom.orEmpty()) },
                )
            },
        ) {
            val stampRally = stampRallyMapViewModel.stampRally ?: return@Scaffold
            val mapViewModel = hiltViewModel<MapViewModel>()
            val gridData = mapViewModel.gridData.result ?: return@Scaffold
            val targetTable = gridData.tables.find { it.booth == stampRally.hostTable }
            val transformState = MapScreen.rememberTransformState(initialScale = 0.5f)
            MapScreen(
                transformState = transformState,
                initialGridPosition = targetTable?.run { IntOffset(gridX, gridY) },
                onArtistClick = onArtistClick,
                modifier = Modifier.padding(it)
            ) { table ->
                val shouldHighlight = stampRallyMapViewModel.artistTables.contains(table.booth)
                val borderWidth = if (shouldHighlight) 2.dp else 1.dp
                val borderColor = if (shouldHighlight) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                val background = if (shouldHighlight) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
                val textColor = if (shouldHighlight) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                TableCell(
                    table = table,
                    background = background,
                    borderWidth = borderWidth,
                    borderColor = borderColor,
                    textColor = textColor,
                    onArtistClick = onArtistClick,
                )
            }
        }
    }
}
