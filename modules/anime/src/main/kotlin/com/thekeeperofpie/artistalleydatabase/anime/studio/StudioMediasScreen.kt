package com.thekeeperofpie.artistalleydatabase.anime.studio

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.StudioMediasQuery
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

// TODO: Use the same year segmented view as staff screen
@OptIn(ExperimentalMaterial3Api::class)
object StudioMediasScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: StudioMediasViewModel = hiltViewModel(),
        name: () -> String,
        favorite: () -> Boolean?,
    ) {
        val viewer by viewModel.viewer.collectAsState()

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        HeaderAndMediaListScreen(
            viewModel = viewModel,
            editViewModel = editViewModel,
            headerTextRes = null,
            header = {
                TopAppBar(
                    title = { Text(text = name(), maxLines = 1) },
                    navigationIcon = {
                        if (upIconOption != null) {
                            UpIconButton(option = upIconOption)
                        }
                    },
                    actions = {
                        FavoriteIconButton(favorite = favorite(), onFavoriteChanged = {
                            viewModel.favoritesToggleHelper.set(
                                FavoriteType.STUDIO,
                                viewModel.studioId,
                                it
                            )
                        })
                    }
                )
            },
            itemKey = { it.media.id },
            item = {
                AnimeMediaListRow(
                    entry = it,
                    viewer = viewer,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    onClickListEdit = editViewModel::initialize
                )
            },
        )
    }

    data class Entry(
        val studio: StudioMediasQuery.Data.Studio,
    )
}
