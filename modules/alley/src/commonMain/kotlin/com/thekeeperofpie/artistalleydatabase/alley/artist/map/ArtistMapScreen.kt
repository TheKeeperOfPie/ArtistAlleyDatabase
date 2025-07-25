package com.thekeeperofpie.artistalleydatabase.alley.artist.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.map.HighlightedTableCell
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.Table
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ArtistMapScreen {

    @Composable
    operator fun invoke(
        viewModel: ArtistMapViewModel,
        mapViewModel: MapViewModel,
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        val artist by viewModel.artist.collectAsStateWithLifecycle()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        artist?.artist?.let {
                            ArtistTitle(
                                year = it.year,
                                id = it.id,
                                booth = it.booth,
                                name = it.name,
                            )
                        }
                    },
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    actions = {
                        IconButton(
                            onClick = {
                                artist?.let {
                                    val newFavorite = !it.favorite
                                    it.favorite = newFavorite
                                    viewModel.onFavoriteToggle(it, newFavorite)
                                }
                            },
                            modifier = Modifier.sharedElement(
                                "favorite",
                                viewModel.id,
                                zIndexInOverlay = 1f,
                            )
                        ) {
                            Icon(
                                imageVector = if (artist?.favorite == true) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = stringResource(
                                    Res.string.alley_favorite_icon_content_description
                                ),
                            )
                        }
                    },
                    modifier = Modifier
                        .skipToLookaheadSize()
                        .sharedBounds("container", viewModel.id, zIndexInOverlay = 1f)
                )
            }
        ) {
            val artist = artist?.artist ?: return@Scaffold
            val gridData = mapViewModel.gridData.result ?: return@Scaffold
            val targetTable = gridData.tables.find { it.booth == artist.booth }
            val transformState = MapScreen.rememberTransformState()
            MapScreen(
                viewModel = mapViewModel,
                transformState = transformState,
                initialGridPosition = targetTable?.run { IntOffset(gridX, gridY) },
                bottomContentPadding = it.calculateBottomPadding(),
            ) {
                HighlightedTableCell(
                    mapViewModel = mapViewModel,
                    table = it,
                    highlight = when (it) {
                        is Table.Single -> it.artistId == viewModel.id
                        is Table.Shared -> it.artistIds.contains(viewModel.id)
                    },
                    onArtistClick = onArtistClick,
                )
            }
        }
    }
}
