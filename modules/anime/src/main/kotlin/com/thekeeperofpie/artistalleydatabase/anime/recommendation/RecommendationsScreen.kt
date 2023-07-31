package com.thekeeperofpie.artistalleydatabase.anime.recommendation

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
import com.anilist.MediaAndRecommendationsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

object RecommendationsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_RECOMMENDATIONS.id

    @Composable
    operator fun invoke(
        viewModel: RecommendationsViewModel,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val entry = viewModel.entry
        val media = entry?.media
        var coverImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.coverImageWidthToHeightRatio)
        }

        val viewer by viewModel.viewer.collectAsState()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        HeaderAndMediaListScreen(
            screenKey = SCREEN_KEY,
            viewModel = viewModel,
            editViewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            headerTextRes = R.string.anime_recommendations_header,
            header = {
                MediaHeader(
                    screenKey = SCREEN_KEY,
                    upIconOption = upIconOption,
                    mediaId = viewModel.headerId,
                    mediaType = viewModel.entry?.media?.type,
                    titles = entry?.titlesUnique,
                    averageScore = media?.averageScore,
                    popularity = media?.popularity,
                    progress = it,
                    headerValues = headerValues,
                    onFavoriteChanged = {
                        viewModel.favoritesToggleHelper.set(
                            headerValues.type.toFavoriteType(),
                            viewModel.headerId,
                            it,
                        )
                    },
                    colorCalculationState = colorCalculationState,
                    enableCoverImageSharedElement = false,
                    onImageWidthToHeightRatioAvailable = {
                        coverImageWidthToHeightRatio = it
                    }
                )
            },
            itemKey = { it.recommendation.id },
            item = {
                // TODO: Show recommendation rating alongside user rating
                AnimeMediaListRow(
                    screenKey = SCREEN_KEY,
                    entry = it?.entry,
                    viewer = viewer,
                    onClickListEdit = { editViewModel.initialize(it.media) },
                    onLongClick = viewModel::onMediaLongClick,
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        )
    }

    data class Entry(
        val media: MediaAndRecommendationsQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}
