package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.MediaAndReviewsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

object ReviewsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_REVIEWS.id

    @Composable
    operator fun invoke(
        viewModel: ReviewsViewModel,
        headerValues: MediaHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val entry = viewModel.entry
        val media = entry?.media
        var coverImageWidthToHeightRatio by remember {
            mutableFloatStateOf(headerValues.coverImageWidthToHeightRatio)
        }

        HeaderAndListScreen(
            viewModel = viewModel,
            headerTextRes = R.string.anime_reviews_header,
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
                    onImageWidthToHeightRatioAvailable = {
                        coverImageWidthToHeightRatio = it
                    }
                )
            },
            itemKey = { it.id },
            item = {
                ReviewSmallCard(
                    review = it,
                    navigationCallback = navigationCallback,
                    onClick = {
                        if (it != null) {
                            navigationCallback.onReviewClick(
                                reviewId = it.id.toString(),
                                media = entry?.media,
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
