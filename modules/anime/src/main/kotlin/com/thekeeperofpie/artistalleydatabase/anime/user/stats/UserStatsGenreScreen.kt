package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anilist.fragment.UserMediaStatistics
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("NAME_SHADOWING")
object UserStatsGenreScreen {

    @Composable
    operator fun invoke(
        statistics: @Composable () -> AniListUserScreen.Entry.Statistics?,
        state: UserStatsGenreState,
        callback: AniListUserScreen.Callback,
        bottomNavBarPadding: @Composable () -> Dp = { 0.dp },
    ) {
        val statistics = statistics()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp + bottomNavBarPadding()),
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

            val genres = statistics.statistics.genres?.filterNotNull().orEmpty()
            if (genres.isNotEmpty()) {
                items(genres, key = { it.genre.orEmpty() }) {
                    GenreCard(genre = it, state = state, callback = callback)
                }
            }
        }
    }

    @Composable
    private fun GenreCard(
        genre: UserMediaStatistics.Genre,
        state: UserStatsGenreState,
        callback: AniListUserScreen.Callback,
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(IntrinsicSize.Min),
        ) {
            Text(
                text = genre.genre.orEmpty(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
            )

            UserStatsBasicScreen.StatsRow(
                genre.count.toString() to R.string.anime_user_statistics_count,
                if (state.isAnime) {
                    String.format(
                        "%.1f",
                        genre.minutesWatched.minutes.toDouble(DurationUnit.DAYS)
                    ) to R.string.anime_user_statistics_anime_days_watched
                } else {
                    genre.chaptersRead.toString() to
                            R.string.anime_user_statistics_manga_chapters_read
                },
                String.format("%.1f", genre.meanScore) to
                        R.string.anime_user_statistics_mean_score,
            )

            if (genre.mediaIds.isNotEmpty()) {
                val medias = state.getMedia(genre)
                if (medias.isSuccess) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.height(180.dp)
                            .padding(vertical = 16.dp),
                    ) {
                        items(genre.mediaIds.filterNotNull(), key = { it }) {
                            val media = medias.getOrNull()?.get(it)
                            Card(onClick = {
                                callback.onMediaClick(
                                    id = it.toString(),
                                    title = media?.title?.userPreferred,
                                    image = media?.coverImage?.large,
                                )
                            }) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(media?.coverImage?.large)
                                        .build(),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = stringResource(R.string.anime_media_cover_image),
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .widthIn(min = 120.dp)
                                        .placeholder(
                                            visible = media == null,
                                            highlight = PlaceholderHighlight.shimmer(),
                                        )
                                )
                            }
                        }
                    }
                } else {
                    // TODO: Error handling with refresh
                }
            }
        }
    }
}
