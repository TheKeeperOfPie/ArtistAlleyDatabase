package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.MediaAndRecommendationsQuery
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

object MediaRecommendationsScreen {

    @Composable
    operator fun invoke(
        viewModel: MediaRecommendationsViewModel,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry.result?.media

        val viewer by viewModel.viewer.collectAsState()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        HeaderAndMediaListScreen(
            viewModel = viewModel,
            editViewModel = editViewModel,
            headerTextRes = R.string.anime_recommendations_header,
            header = {
                MediaHeader(
                    upIconOption = upIconOption,
                    mediaId = viewModel.mediaId,
                    mediaType = media?.type,
                    titles = entry.result?.titlesUnique,
                    episodes = media?.episodes,
                    format = media?.format,
                    averageScore = media?.averageScore,
                    popularity = media?.popularity,
                    progress = it,
                    headerValues = headerValues,
                    onFavoriteChanged = {
                        viewModel.favoritesToggleHelper.set(
                            headerValues.type.toFavoriteType(),
                            viewModel.mediaId,
                            it,
                        )
                    },
                    enableCoverImageSharedElement = false
                )
            },
            itemKey = { it.recommendation.id },
            item = {
                AnimeMediaListRow(
                    entry = it?.entry,
                    viewer = viewer,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    onClickListEdit = editViewModel::initialize,
                    recommendation = it?.recommendationData,
                    onUserRecommendationRating = viewModel.recommendationToggleHelper::toggle
                )
            },
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
