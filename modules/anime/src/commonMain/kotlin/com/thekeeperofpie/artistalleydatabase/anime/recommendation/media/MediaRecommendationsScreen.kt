package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_recommendations_header
import com.anilist.MediaAndRecommendationsQuery
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndMediaListScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption

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
        val animeComponent = LocalAnimeComponent.current
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        HeaderAndMediaListScreen(
            viewModel = viewModel,
            editViewModel = editViewModel,
            headerTextRes = Res.string.anime_recommendations_header,
            header = {
                MediaHeader(
                    viewer = viewer,
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
                    entry = it?.media,
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
