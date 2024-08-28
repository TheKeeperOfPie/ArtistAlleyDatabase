package com.thekeeperofpie.artistalleydatabase.alley.artist.map

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.map.HighlightedTableCell
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize

@OptIn(ExperimentalMaterial3Api::class)
object ArtistMapScreen {

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        val viewModel = hiltViewModel<ArtistMapViewModel>()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { viewModel.artist?.let { ArtistTitle(it.value) } },
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.artist?.let {
                                    val newFavorite = !it.favorite
                                    it.favorite = it.favorite
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
                                imageVector = if (viewModel.artist?.favorite == true) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = stringResource(
                                    R.string.alley_favorite_icon_content_description
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
            val artist = viewModel.artist?.value ?: return@Scaffold
            val mapViewModel = hiltViewModel<MapViewModel>()
            val gridData = mapViewModel.gridData.result ?: return@Scaffold
            val targetTable = gridData.tables.find { it.booth == artist.booth }
            val transformState = MapScreen.rememberTransformState()
            MapScreen(
                transformState = transformState,
                initialGridPosition = targetTable?.run { IntOffset(gridX, gridY) },
                modifier = Modifier.padding(it)
            ) {
                HighlightedTableCell(
                    table = it,
                    highlight = it.booth == viewModel.id,
                    onArtistClick = onArtistClick,
                )
            }
        }
    }
}
