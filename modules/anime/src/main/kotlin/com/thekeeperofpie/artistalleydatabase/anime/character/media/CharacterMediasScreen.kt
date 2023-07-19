package com.thekeeperofpie.artistalleydatabase.anime.character.media

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.CharacterAndMediasQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeader
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

object CharacterMediasScreen {

    private val SCREEN_KEY = AnimeNavDestinations.CHARACTER_MEDIAS.id

    @Composable
    operator fun invoke(
        viewModel: CharacterMediasViewModel,
        upIconOption: UpIconOption,
        headerValues: CharacterHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        var characterImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.imageWidthToHeightRatio)
        }

        val viewer by viewModel.viewer.collectAsState()

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        HeaderAndMediaListScreen(
            screenKey = SCREEN_KEY,
            viewModel = viewModel,
            editViewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            headerTextRes = R.string.anime_character_medias_header,
            header = {
                CharacterHeader(
                    screenKey = SCREEN_KEY,
                    upIconOption = upIconOption,
                    characterId = viewModel.headerId,
                    progress = it,
                    headerValues = headerValues,
                    colorCalculationState = colorCalculationState,
                    onImageWidthToHeightRatioAvailable = {
                        characterImageWidthToHeightRatio = it
                    }
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
        val character: CharacterAndMediasQuery.Data.Character,
    )
}
