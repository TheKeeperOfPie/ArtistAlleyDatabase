package com.thekeeperofpie.artistalleydatabase.anime.character.media

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_medias_header
import com.anilist.CharacterAndMediasQuery
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeader
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey

object CharacterMediasScreen {

    @Composable
    operator fun invoke(
        viewModel: CharacterMediasViewModel,
        upIconOption: UpIconOption,
        headerValues: CharacterHeaderValues,
        sharedTransitionKey: SharedTransitionKey?,
    ) {
        val viewer by viewModel.viewer.collectAsState()
        val animeComponent = LocalAnimeComponent.current
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        HeaderAndMediaListScreen(
            viewModel = viewModel,
            editViewModel = editViewModel,
            headerTextRes = Res.string.anime_character_medias_header,
            header = {
                CharacterHeader(
                    upIconOption = upIconOption,
                    characterId = viewModel.characterId,
                    progress = it,
                    headerValues = headerValues,
                    sharedTransitionKey = sharedTransitionKey,
                    onFavoriteChanged = {
                        viewModel.favoritesToggleHelper
                            .set(FavoriteType.CHARACTER, viewModel.characterId, it)
                    },
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
        val character: CharacterAndMediasQuery.Data.Character,
    )
}
