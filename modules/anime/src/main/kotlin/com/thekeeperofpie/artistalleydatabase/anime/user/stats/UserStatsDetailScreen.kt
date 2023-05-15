package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("NAME_SHADOWING")
object UserStatsDetailScreen {

    @Composable
    operator fun <Value> invoke(
        statistics: @Composable () -> AniListUserScreen.Entry.Statistics?,
        navigationCallback: AnimeNavigator.NavigationCallback,
        state: AniListUserViewModel.States.State<Value>,
        isAnime: Boolean,
        bottomNavigationState: BottomNavigationState? = null,
        values: (AniListUserScreen.Entry.Statistics) -> List<Value>,
        valueToKey: (Value) -> String,
        valueToText: (Value) -> String = valueToKey,
        valueToCount: (Value) -> Int,
        valueToMinutesWatched: (Value) -> Int,
        valueToChaptersRead: (Value) -> Int,
        valueToMeanScore: (Value) -> Double,
        valueToMediaIds: (Value) -> List<Int>,
        onValueClick: (Value) -> Unit,
        initialItemImage: ((Value) -> String?)? = null
    ) {
        val statistics = statistics()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 16.dp + (bottomNavigationState?.bottomNavBarPadding() ?: 0.dp),
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            if (statistics == null) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp)
                        )
                    }
                }
                return@LazyColumn
            }

            val values = values(statistics)
            if (values.isNotEmpty()) {
                items(values, key = valueToKey) {
                    StatsDetailCard(
                        value = it,
                        valueToText = valueToText,
                        valueToCount = valueToCount,
                        valueToMinutesWatched = valueToMinutesWatched,
                        valueToChaptersRead = valueToChaptersRead,
                        valueToMeanScore = valueToMeanScore,
                        valueToMediaIds = valueToMediaIds,
                        onValueClick = onValueClick,
                        initialItemImage = initialItemImage,
                        state = state,
                        isAnime = isAnime,
                        navigationCallback = navigationCallback,
                    )
                }
            }
        }
    }

    @Composable
    private fun <Value> StatsDetailCard(
        value: Value,
        valueToText: (Value) -> String,
        valueToCount: (Value) -> Int,
        valueToMinutesWatched: (Value) -> Int,
        valueToChaptersRead: (Value) -> Int,
        valueToMeanScore: (Value) -> Double,
        valueToMediaIds: (Value) -> List<Int>,
        onValueClick: (Value) -> Unit,
        initialItemImage: ((Value) -> String?)?,
        state: AniListUserViewModel.States.State<Value>,
        isAnime: Boolean,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        ElevatedCard(
            onClick = { onValueClick(value) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(IntrinsicSize.Min),
        ) {
            Text(
                text = valueToText(value),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
            )

            UserStatsBasicScreen.StatsRow(
                valueToCount(value).toString() to R.string.anime_user_statistics_count,
                if (isAnime) {
                    String.format(
                        "%.1f",
                        valueToMinutesWatched(value).minutes.toDouble(DurationUnit.DAYS)
                    ) to R.string.anime_user_statistics_anime_days_watched
                } else {
                    valueToChaptersRead(value).toString() to
                            R.string.anime_user_statistics_manga_chapters_read
                },
                String.format("%.1f", valueToMeanScore(value)) to
                        R.string.anime_user_statistics_mean_score,
            )

            val mediaIds = valueToMediaIds(value)
            if (mediaIds.isNotEmpty()) {
                val medias = state.getMedia(value)
                if (medias.isSuccess) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .height(180.dp)
                    ) {
                        val initialItemImage = initialItemImage?.invoke(value)
                        if (initialItemImage != null) {
                            item {
                                InnerCard(
                                    image = initialItemImage,
                                    loading = false,
                                    onClick = { onValueClick(value) }
                                )
                            }
                        }

                        items(mediaIds, key = { it }) {
                            val media = medias.getOrNull()?.get(it)
                            InnerCard(
                                image = media?.coverImage?.large,
                                loading = media == null,
                                onClick = {
                                    navigationCallback.onMediaClick(
                                        id = it.toString(),
                                        title = media?.title?.userPreferred,
                                        image = media?.coverImage?.large,
                                    )
                                }
                            )
                        }
                    }
                } else {
                    // TODO: Error handling with refresh
                    // TODO: Empty state
                }
            }
        }
    }

    @Composable
    private fun InnerCard(image: String?, loading: Boolean, onClick: () -> Unit) {
        Card(onClick = onClick) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.anime_media_cover_image),
                modifier = Modifier
                    .fillMaxHeight()
                    .size(width = 130.dp, height = 180.dp)
                    .placeholder(
                        visible = loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}
