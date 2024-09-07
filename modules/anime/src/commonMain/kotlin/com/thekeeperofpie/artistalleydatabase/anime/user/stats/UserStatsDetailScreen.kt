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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_statistics_anime_days_watched
import artistalleydatabase.modules.anime.generated.resources.anime_user_statistics_count
import artistalleydatabase.modules.anime.generated.resources.anime_user_statistics_manga_chapters_read
import artistalleydatabase.modules.anime.generated.resources.anime_user_statistics_mean_score
import coil3.request.crossfade
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

@Suppress("NAME_SHADOWING")
object UserStatsDetailScreen {

    @Composable
    operator fun <Value> invoke(
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
        onValueClick: (Value, CoilImageState, SharedTransitionKey?) -> Unit,
        initialItemId: ((Value) -> String)? = null,
        initialItemImage: ((Value) -> String?)? = null,
        initialItemSharedTransitionKey: @Composable() ((Value) -> SharedTransitionKey?)? = null,
        initialItemSharedTransitionIdentifier: ((Value) -> String)? = null,
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
                    SharedTransitionKeyScope("user_stats_details", valueToKey(it)) {
                        StatsDetailCard(
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
                            initialItemSharedTransitionKey = initialItemSharedTransitionKey,
                            initialItemSharedTransitionIdentifier = initialItemSharedTransitionIdentifier,
                            state = state,
                            isAnime = isAnime,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun <Value> StatsDetailCard(
        value: Value,
        valueToText: @Composable (Value) -> String,
        valueToCount: (Value) -> Int,
        valueToMinutesWatched: (Value) -> Int,
        valueToChaptersRead: (Value) -> Int,
        valueToMeanScore: (Value) -> Double,
        valueToMediaIds: (Value) -> List<Int>,
        onValueClick: (Value, CoilImageState, SharedTransitionKey?) -> Unit,
        initialItemId: ((Value) -> String)?,
        initialItemImage: ((Value) -> String?)?,
        initialItemSharedTransitionKey: @Composable() ((Value) -> SharedTransitionKey?)?,
        initialItemSharedTransitionIdentifier: ((Value) -> String)?,
        state: AniListUserViewModel.States.State<Value>,
        isAnime: Boolean,
    ) {
        val firstItemImageState = rememberCoilImageState(initialItemImage?.invoke(value))
        val sharedTransitionKey = initialItemSharedTransitionKey?.invoke(value)
        ElevatedCard(
            onClick = { onValueClick(value, firstItemImageState, sharedTransitionKey) },
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
                valueToCount(value).toString() to Res.string.anime_user_statistics_count,
                if (isAnime) {
                    valueToMinutesWatched(value)
                        .minutes
                        .toDouble(DurationUnit.DAYS)
                        .let(BigDecimal::fromDouble)
                        .roundToDigitPositionAfterDecimalPoint(1, RoundingMode.FLOOR)
                        .toStringExpanded() to Res.string.anime_user_statistics_anime_days_watched
                } else {
                    valueToChaptersRead(value).toString() to
                            Res.string.anime_user_statistics_manga_chapters_read
                },
                valueToMeanScore(value)
                    .let(BigDecimal::fromDouble)
                    .roundToDigitPositionAfterDecimalPoint(1, RoundingMode.FLOOR)
                    .toStringExpanded() to Res.string.anime_user_statistics_mean_score,
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
                        if (initialItemId != null
                            && initialItemImage != null
                            && initialItemSharedTransitionKey != null
                            && initialItemSharedTransitionIdentifier != null
                        ) {
                            val initialItemImage = initialItemImage.invoke(value)
                            item(key = initialItemId(value)) {
                                InnerCard(
                                    sharedTransitionKey = sharedTransitionKey,
                                    sharedTransitionIdentifier = initialItemSharedTransitionIdentifier(
                                        value
                                    ),
                                    imageState = firstItemImageState,
                                    loading = false,
                                    onClick = {
                                        onValueClick(
                                            value,
                                            firstItemImageState,
                                            sharedTransitionKey,
                                        )
                                    },
                                )
                            }
                        }

                        items(mediaIds, key = { it }) {
                            val media = medias.getOrNull()?.get(it)
                            val navigationCallback = LocalNavigationCallback.current
                            val languageOptionMedia = LocalLanguageOptionMedia.current
                            val sharedTransitionKey = media?.id?.toString()
                                ?.let { SharedTransitionKey.makeKeyForId(it) }
                            InnerCard(
                                sharedTransitionKey = sharedTransitionKey,
                                sharedTransitionIdentifier = "media_image",
                                image = media?.coverImage?.extraLarge,
                                loading = media == null,
                                onClick = { imageState ->
                                    if (media != null) {
                                        navigationCallback.navigate(
                                            AnimeDestination.MediaDetails(
                                                mediaNavigationData = media,
                                                coverImage = imageState.toImageState(),
                                                languageOptionMedia = languageOptionMedia,
                                                sharedTransitionKey = sharedTransitionKey,
                                            )
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
        sharedTransitionKey: SharedTransitionKey?,
        sharedTransitionIdentifier: String,
        imageState: CoilImageState,
        loading: Boolean,
        onClick: () -> Unit,
    ) {
        Card(onClick = onClick) {
            val density = LocalDensity.current
            CoilImage(
                state = imageState,
                model = imageState.request()
                    .crossfade(true)
                    .size(
                        width = density.run { 130.dp.roundToPx() },
                        height = density.run { 180.dp.roundToPx() },
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(Res.string.anime_media_cover_image_content_description),
                modifier = Modifier
                    .sharedElement(sharedTransitionKey, sharedTransitionIdentifier)
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

    @Composable
    private fun InnerCard(
        sharedTransitionKey: SharedTransitionKey?,
        sharedTransitionIdentifier: String,
        image: String?,
        loading: Boolean,
        onClick: (CoilImageState) -> Unit,
    ) {
        val imageState = rememberCoilImageState(image)
        InnerCard(
            sharedTransitionKey = sharedTransitionKey,
            sharedTransitionIdentifier = sharedTransitionIdentifier,
            imageState = imageState,
            loading = loading,
            onClick = { onClick(imageState) },
        )
    }
}
