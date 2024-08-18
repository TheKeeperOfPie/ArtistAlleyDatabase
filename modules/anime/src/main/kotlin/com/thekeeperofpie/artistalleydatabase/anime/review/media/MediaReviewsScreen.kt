package com.thekeeperofpie.artistalleydatabase.anime.review.media

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.MediaAndReviewsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption

object MediaReviewsScreen {

    @Composable
    operator fun invoke(
        viewModel: MediaReviewsViewModel,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry.result?.media
        val coverImageState = rememberCoilImageState(headerValues.coverImage)

        val mediaTitle = media?.title?.primaryTitle()
        HeaderAndListScreen(
            viewModel = viewModel,
            headerTextRes = R.string.anime_reviews_header,
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
                    coverImageState = coverImageState,
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
            itemKey = { it.id },
            item = { review ->
                ReviewSmallCard(
                    review = review,
                    onClick = {
                        if (review != null) {
                            it.navigate(
                                AnimeDestination.ReviewDetails(
                                    reviewId = review.id.toString(),
                                    headerParams = MediaHeaderParams(
                                        title = mediaTitle,
                                        coverImage = coverImageState.toImageState(),
                                        media = media,
                                        favorite = viewModel.favoritesToggleHelper.favorite
                                            ?: media?.isFavourite,
                                    )
                                )
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
