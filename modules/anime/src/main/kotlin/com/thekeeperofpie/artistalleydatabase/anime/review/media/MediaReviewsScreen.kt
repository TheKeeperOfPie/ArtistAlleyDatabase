package com.thekeeperofpie.artistalleydatabase.anime.review.media

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.MediaAndReviewsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

object MediaReviewsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_REVIEWS.id

    @Composable
    operator fun invoke(
        viewModel: MediaReviewsViewModel,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry.result?.media
        var coverImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.coverImageWidthToHeightRatio)
        }

        HeaderAndListScreen(
            viewModel = viewModel,
            headerTextRes = R.string.anime_reviews_header,
            header = {
                MediaHeader(
                    screenKey = SCREEN_KEY,
                    upIconOption = upIconOption,
                    viewer = viewModel.viewer.collectAsState().value,
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
                    enableCoverImageSharedElement = false,
                    onImageWidthToHeightRatioAvailable = {
                        coverImageWidthToHeightRatio = it
                    }
                )
            },
            itemKey = { it.id },
            item = { review ->
                ReviewSmallCard(
                    screenKey = SCREEN_KEY,
                    review = review,
                    onClick = {
                        if (review != null) {
                            it.onReviewClick(
                                reviewId = review.id.toString(),
                                media = media,
                                favorite = viewModel.favoritesToggleHelper.favorite,
                                imageWidthToHeightRatio = coverImageWidthToHeightRatio
                            )
                        }
                    },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        )
    }

    data class Entry(
        val media: MediaAndReviewsQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}
