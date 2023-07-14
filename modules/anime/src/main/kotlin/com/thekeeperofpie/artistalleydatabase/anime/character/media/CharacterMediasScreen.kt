package com.thekeeperofpie.artistalleydatabase.anime.character.media

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.CharacterAndMediasQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeader
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

object CharacterMediasScreen {

    private val SCREEN_KEY = AnimeNavDestinations.CHARACTER_MEDIAS.id

    @Composable
    operator fun invoke(
        viewModel: CharacterMediasViewModel,
        headerValues: CharacterHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        var characterImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.imageWidthToHeightRatio)
        }
        HeaderAndListScreen(
            viewModel = viewModel,
            headerTextRes = R.string.anime_character_medias_header,
            header = {
                CharacterHeader(
                    screenKey = SCREEN_KEY,
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
