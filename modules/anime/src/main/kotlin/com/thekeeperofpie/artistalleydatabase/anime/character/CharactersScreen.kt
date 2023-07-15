package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.anilist.MediaAndCharactersQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

object CharactersScreen {

    private val MIN_IMAGE_HEIGHT = 100.dp
    private val IMAGE_WIDTH = 72.dp
    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_CHARACTERS.id

    @Composable
    operator fun invoke(
        viewModel: CharactersViewModel,
        headerValues: MediaHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val entry = viewModel.entry
        val media = entry?.media

        HeaderAndListScreen(
            viewModel = viewModel,
            headerTextRes = R.string.anime_characters_header,
            header = {
                MediaHeader(
                    screenKey = SCREEN_KEY,
                    mediaId = viewModel.headerId,
                    titles = entry?.titlesUnique,
                    averageScore = media?.averageScore,
                    popularity = media?.popularity,
                    progress = it,
                    headerValues = headerValues,
                    colorCalculationState = colorCalculationState,
                    enableCoverImageSharedElement = false,
                )
            },
            itemKey = { it.id },
            item = {
                CharacterCard(
                    screenKey = SCREEN_KEY,
                    character = it,
                    imageWidth = IMAGE_WIDTH,
                    minHeight = MIN_IMAGE_HEIGHT,
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                )
            },
        )
    }

    data class Entry(
        val media: MediaAndCharactersQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}