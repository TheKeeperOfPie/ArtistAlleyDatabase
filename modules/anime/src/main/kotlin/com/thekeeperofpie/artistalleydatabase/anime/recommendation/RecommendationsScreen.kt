package com.thekeeperofpie.artistalleydatabase.anime.recommendation

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
import com.anilist.MediaAndRecommendationsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class)
object RecommendationsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_RECOMMENDATIONS.id

    @Composable
    operator fun invoke(
        viewModel: RecommendationsViewModel,
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
            val recommendations = viewModel.recommendations.collectAsLazyPagingItems()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(350.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(it)
            ) {
                item("header") {
                    DetailsSectionHeader(text = stringResource(R.string.anime_recommendations_header))
                }

                items(
                    count = recommendations.itemCount,
                    key = recommendations.itemKey { it.id },
                    contentType = recommendations.itemContentType { "character" },
                ) {
                    val recommendation = recommendations[it]
                    // TODO: Show recommendation rating alongside user rating
                    AnimeMediaListRow(
                        screenKey = SCREEN_KEY,
                        entry = recommendation?.mediaRecommendation?.let(AnimeMediaListRow::Entry),
                        onTagLongClick = { /* TODO */ },
                        colorCalculationState = colorCalculationState,
                        navigationCallback = navigationCallback,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }
    }

    data class Entry(
        val media: MediaAndRecommendationsQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()

        val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    }
}
