package com.thekeeperofpie.artistalleydatabase.anime.studio

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.StudioMediasQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

// TODO: Use the same year segmented view as staff screen
@OptIn(ExperimentalMaterial3Api::class)
object StudioMediasScreen {

    private val SCREEN_KEY = AnimeNavDestinations.STUDIO_MEDIAS.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: StudioMediasViewModel,
        name: () -> String,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val viewer by viewModel.viewer.collectAsState()

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        HeaderAndMediaListScreen(
            screenKey = SCREEN_KEY,
            viewModel = viewModel,
            editViewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            headerTextRes = null,
            header = {
                AppBar(
                    text = name(),
                    upIconOption = upIconOption,
                )
            },
            itemKey = { it.media.id },
            item = {
                AnimeMediaListRow(
                    screenKey = SCREEN_KEY,
                    entry = it,
                    viewer = viewer,
                    onClickListEdit = { editViewModel.initialize(it.media) },
                    onLongClick = viewModel::onMediaLongClick,
                    onTagLongClick = { /* TODO */ },
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                )
            }
        )
    }

    data class Entry(
        val studio: StudioMediasQuery.Data.Studio,
    )
}
