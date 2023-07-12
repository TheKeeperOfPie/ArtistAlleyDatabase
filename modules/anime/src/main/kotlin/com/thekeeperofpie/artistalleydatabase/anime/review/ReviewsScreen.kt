package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.MediaAndReviewsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class)
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
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        var coverImageWidthToHeightRatio by remember { mutableFloatStateOf(1f) }
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    MediaHeader(
                        screenKey = SCREEN_KEY,
                        mediaId = viewModel.mediaId,
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
                }
            },
            snackbarHost = {
                val error = viewModel.error
                SnackbarErrorText(
                    error?.first,
                    error?.second,
                    onErrorDismiss = { viewModel.error = null }
                )
            },
        ) {
            val reviews = viewModel.reviews.collectAsLazyPagingItems()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(350.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(it)
            ) {
                item("header") {
                    DetailsSectionHeader(text = stringResource(R.string.anime_reviews_header))
                }

                items(
                    count = reviews.itemCount,
                    key = reviews.itemKey { it.id },
                    contentType = reviews.itemContentType { "character" },
                ) {
                    val review = reviews[it]
                    ReviewSmallCard(
                        review = review,
                        navigationCallback = navigationCallback,
                        onClick = {
                            if (review != null) {
                                navigationCallback.onReviewClick(
                                    reviewId = review.id.toString(),
                                    media = entry?.media,
                                    imageWidthToHeightRatio = coverImageWidthToHeightRatio
                                )
                            }
                        },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }
    }

    data class Entry(
        val media: MediaAndReviewsQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()

        val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    }
}
