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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.compose.AutoSharedElement
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.currentLocale
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("NAME_SHADOWING")
object UserStatsDetailScreen {

    @Composable
    operator fun <Value> invoke(
        screenKey: String,
        statistics: @Composable () -> AniListUserScreen.Entry.Statistics?,
        state: AniListUserViewModel.States.State<Value>,
        isAnime: Boolean,
        bottomNavigationState: BottomNavigationState? = null,
        values: (AniListUserScreen.Entry.Statistics) -> List<Value>,
        valueToKey: (Value) -> String,
        valueToText: @Composable (Value) -> String,
        valueToCount: (Value) -> Int,
        valueToMinutesWatched: (Value) -> Int,
        valueToChaptersRead: (Value) -> Int,
        valueToMeanScore: (Value) -> Double,
        valueToMediaIds: (Value) -> List<Int>,
        onValueClick: (Value, imageWidthToHeightRatio: Float) -> Unit,
        initialItemId: ((Value) -> String)? = null,
        initialItemImage: ((Value) -> String?)? = null,
        initialItemSharedElementKey: ((Value) -> String)? = null,
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
                        screenKey = screenKey,
                        value = it,
                        valueToText = valueToText,
                        valueToCount = valueToCount,
                        valueToMinutesWatched = valueToMinutesWatched,
                        valueToChaptersRead = valueToChaptersRead,
                        valueToMeanScore = valueToMeanScore,
                        valueToMediaIds = valueToMediaIds,
                        onValueClick = onValueClick,
                        initialItemId = initialItemId,
                        initialItemImage = initialItemImage,
                        initialItemSharedElementKey = initialItemSharedElementKey,
                        state = state,
                        isAnime = isAnime,
                    )
                }
            }
        }
    }

    @Composable
    private fun <Value> StatsDetailCard(
        screenKey: String,
        value: Value,
        valueToText: @Composable (Value) -> String,
        valueToCount: (Value) -> Int,
        valueToMinutesWatched: (Value) -> Int,
        valueToChaptersRead: (Value) -> Int,
        valueToMeanScore: (Value) -> Double,
        valueToMediaIds: (Value) -> List<Int>,
        onValueClick: (Value, imageWidthToHeightRatio: Float) -> Unit,
        initialItemId: ((Value) -> String)?,
        initialItemImage: ((Value) -> String?)?,
        initialItemSharedElementKey: ((Value) -> String)?,
        state: AniListUserViewModel.States.State<Value>,
        isAnime: Boolean,
    ) {
        var firstItemImageWidthToHeightRatio by remember { MutableSingle(1f) }
        ElevatedCard(
            onClick = { onValueClick(value, firstItemImageWidthToHeightRatio) },
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
                        LocalConfiguration.currentLocale,
                        "%.1f",
                        valueToMinutesWatched(value).minutes.toDouble(DurationUnit.DAYS)
                    ) to R.string.anime_user_statistics_anime_days_watched
                } else {
                    valueToChaptersRead(value).toString() to
                            R.string.anime_user_statistics_manga_chapters_read
                },
                String.format(LocalConfiguration.currentLocale, "%.1f", valueToMeanScore(value)) to
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
                        if (initialItemSharedElementKey != null) {
                            if (initialItemImage != null) {
                                val initialItemImage = initialItemImage.invoke(value)
                                item {
                                    InnerCard(
                                        screenKey = screenKey,
                                        sharedElementKey = initialItemSharedElementKey(value),
                                        id = initialItemId!!.invoke(value),
                                        image = initialItemImage,
                                        loading = false,
                                        onClick = {
                                            onValueClick(value, firstItemImageWidthToHeightRatio)
                                        },
                                        onImageRatioCalculated = {
                                            firstItemImageWidthToHeightRatio = it
                                        },
                                    )
                                }
                            }
                        }

                        items(mediaIds, key = { it }) {
                            val media = medias.getOrNull()?.get(it)
                            val navigationCallback = LocalNavigationCallback.current
                            InnerCard(
                                screenKey = screenKey,
                                sharedElementKey = "anime_media_${media?.id}_image",
                                id = media?.id.toString(),
                                image = media?.coverImage?.extraLarge,
                                loading = media == null,
                                onClick = { ratio ->
                                    if (media != null) {
                                        navigationCallback.onMediaClick(
                                            media = media,
                                            imageWidthToHeightRatio = ratio,
                                        )
                                    }
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

    // TODO: For media types, show quick edit and stats
    @Composable
    private fun InnerCard(
        screenKey: String,
        sharedElementKey: String,
        id: String,
        image: String?,
        loading: Boolean,
        onClick: () -> Unit,
        onImageRatioCalculated: (Float) -> Unit,
    ) {
        Card(onClick = onClick) {
            val density = LocalDensity.current
            AutoSharedElement(
                key = sharedElementKey,
                screenKey = screenKey,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .size(
                            width = density.run { 130.dp.roundToPx() },
                            height = density.run { 180.dp.roundToPx() },
                        )
                        .build(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { onImageRatioCalculated(it.widthToHeightRatio()) },
                    contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                    modifier = Modifier
                        .fillMaxHeight()
                        .size(width = 130.dp, height = 180.dp)
                        .placeholder(
                            visible = loading,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }

    @Composable
    private fun InnerCard(
        screenKey: String,
        sharedElementKey: String,
        id: String,
        image: String?,
        loading: Boolean,
        onClick: (widthToHeightRatio: Float) -> Unit,
    ) {
        var widthToHeightRatio by remember { MutableSingle(1f) }
        InnerCard(
            screenKey = screenKey,
            sharedElementKey = sharedElementKey,
            id = id,
            image = image,
            loading = loading,
            onClick = { onClick(widthToHeightRatio) },
            onImageRatioCalculated = { widthToHeightRatio = it },
        )
    }
}
