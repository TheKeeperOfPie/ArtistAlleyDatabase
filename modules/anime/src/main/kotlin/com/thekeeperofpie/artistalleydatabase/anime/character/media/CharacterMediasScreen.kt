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
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeader
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

object CharacterMediasScreen {

    private val SCREEN_KEY = AnimeNavDestinations.CHARACTER_MEDIAS.id

    @Composable
    operator fun invoke(
        viewModel: CharacterMediasViewModel,
        upIconOption: UpIconOption,
        headerValues: CharacterHeaderValues,
    ) {
        var characterImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.imageWidthToHeightRatio)
        }

        val viewer by viewModel.viewer.collectAsState()

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        HeaderAndMediaListScreen(
            screenKey = SCREEN_KEY,
            viewModel = viewModel,
            editViewModel = editViewModel,
            headerTextRes = R.string.anime_character_medias_header,
            header = {
                CharacterHeader(
                    screenKey = SCREEN_KEY,
                    upIconOption = upIconOption,
                    viewer = viewer,
                    characterId = viewModel.characterId,
                    progress = it,
                    headerValues = headerValues,
                    onFavoriteChanged = {
                        viewModel.favoritesToggleHelper
                            .set(FavoriteType.CHARACTER, viewModel.characterId, it)
                    },
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
                    onClickListEdit = editViewModel::initialize,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                )
            },
        )
    }

    data class Entry(
        val character: CharacterAndMediasQuery.Data.Character,
    )
}
