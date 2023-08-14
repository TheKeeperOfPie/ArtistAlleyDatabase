package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.AuthedUserQuery
import com.anilist.fragment.MediaPreview
import com.anilist.type.RecommendationRating
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object AnimeMediaListRow {

    @Composable
    operator fun invoke(
        screenKey: String,
        entry: Entry?,
        viewer: AuthedUserQuery.Data.Viewer?,
        modifier: Modifier = Modifier,
        label: (@Composable () -> Unit)? = null,
        onClickListEdit: (Entry) -> Unit,
        onLongClick: (Entry) -> Unit,
        colorCalculationState: ColorCalculationState,
        nextAiringEpisode: MediaPreview.NextAiringEpisode? = entry?.media?.nextAiringEpisode,
        showDate: Boolean = true,
        recommendation: RecommendationData? = null,
        onUserRecommendationRating: (
            recommendation: RecommendationData,
            newRating: RecommendationRating,
        ) -> Unit = { _, _ -> },
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        val navigationCallback = LocalNavigationCallback.current
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
                .padding(bottom = 2.dp)
        ) {
            Row(modifier = Modifier
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    enabled = entry != null,
                    onClick = {
                        navigationCallback.onMediaClick(
                            entry!!,
                            null,
                            imageWidthToHeightRatio
                        )
                    },
                    onLongClick = { if (entry != null) onLongClick(entry) }
                )
            ) {
                CoverImage(
                    screenKey = screenKey,
                    entry = entry,
                    viewer = viewer,
                    onClick = {
                        if (entry != null) {
                            navigationCallback.onMediaClick(entry, null, imageWidthToHeightRatio)
                        }
                    },
                    onClickListEdit = onClickListEdit,
                    colorCalculationState = colorCalculationState,
                    onRatioAvailable = { imageWidthToHeightRatio = it },
                    recommendation = recommendation,
                    onUserRecommendationRating = onUserRecommendationRating,
                )

                Column(modifier = Modifier.heightIn(min = 180.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            label?.invoke()
                            TitleText(entry, paddingTop = if (label == null) 10.dp else 4.dp)
                            SubtitleText(entry)
                        }

                        MediaRatingIconsSection(
                            rating = entry?.media?.averageScore,
                            popularity = entry?.media?.popularity,
                            loading = entry == null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .wrapContentWidth()
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    nextAiringEpisode?.let { MediaNextAiringSection(it, showDate = showDate) }

                    val (containerColor, textColor) =
                        colorCalculationState.getColors(entry?.media?.id?.toString())
                    val tags = entry?.tags.orEmpty()
                    if (tags.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                    } else {
                        MediaTagRow(
                            tags = tags,
                            onTagClick = { id, name ->
                                if (entry != null) {
                                    navigationCallback.onTagClick(
                                        entry.media.type ?: com.anilist.type.MediaType.ANIME,
                                        id,
                                        name
                                    )
                                }
                            },
                            tagContainerColor = containerColor,
                            tagTextColor = textColor,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CoverImage(
        screenKey: String,
        entry: Entry?,
        viewer: AuthedUserQuery.Data.Viewer?,
        onClick: (Entry) -> Unit = {},
        onClickListEdit: (Entry) -> Unit,
        colorCalculationState: ColorCalculationState,
        onRatioAvailable: (Float) -> Unit,
        recommendation: RecommendationData?,
        onUserRecommendationRating: (
            recommendation: RecommendationData,
            newRating: RecommendationRating,
        ) -> Unit,
    ) {
        Box {
            val fullscreenImageHandler = LocalFullscreenImageHandler.current
            MediaCoverImage(
                screenKey = screenKey,
                mediaId = entry?.media?.id?.toString(),
                image = ImageRequest.Builder(LocalContext.current)
                    .data(entry?.media?.coverImage?.extraLarge)
                    .crossfade(true)
                    .allowHardware(colorCalculationState.hasColor(entry?.media?.id?.toString()))
                    .size(
                        width = Dimension.Pixels(LocalDensity.current.run { 130.dp.roundToPx() }),
                        height = Dimension.Undefined
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                onSuccess = {
                    onRatioAvailable(it.widthToHeightRatio())
                    entry?.media?.id?.let { mediaId ->
                        ComposeColorUtils.calculatePalette(
                            mediaId.toString(),
                            it,
                            colorCalculationState,
                        )
                    }
                },
                modifier = Modifier
                    // Clip to match card so that shared element animation keeps rounded corner
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxHeight()
                    .heightIn(min = 180.dp)
                    .width(130.dp)
                    .placeholder(
                        visible = entry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .combinedClickable(
                        onClick = { if (entry != null) onClick(entry) },
                        onLongClick = {
                            entry?.media?.coverImage?.extraLarge
                                ?.let(fullscreenImageHandler::openImage)
                        },
                        onLongClickLabel = stringResource(
                            R.string.anime_media_cover_image_long_press_preview
                        ),
                    )
            )

            if (viewer != null && recommendation != null) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(130.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f))
                        .align(Alignment.TopStart)
                ) {
                    val userRating = recommendation.userRating
                    IconButton(
                        onClick = {
                            val newRating = if (userRating == RecommendationRating.RATE_DOWN) {
                                RecommendationRating.NO_RATING
                            } else {
                                RecommendationRating.RATE_DOWN
                            }
                            onUserRecommendationRating(recommendation, newRating)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (userRating == RecommendationRating.RATE_DOWN) {
                                Icons.Filled.ThumbDown
                            } else {
                                Icons.Outlined.ThumbDown
                            },
                            contentDescription = stringResource(
                                if (userRating == RecommendationRating.RATE_DOWN) {
                                    R.string.anime_media_recommendation_rate_down_filled_content_description
                                } else {
                                    R.string.anime_media_recommendation_rate_down_empty_content_description
                                }
                            ),
                        )
                    }

                    Text(
                        text = recommendation.rating.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = {
                            val newRating = if (userRating == RecommendationRating.RATE_UP) {
                                RecommendationRating.NO_RATING
                            } else {
                                RecommendationRating.RATE_UP
                            }
                            onUserRecommendationRating(recommendation, newRating)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (userRating == RecommendationRating.RATE_UP) {
                                Icons.Filled.ThumbUp
                            } else {
                                Icons.Outlined.ThumbUp
                            },
                            contentDescription = stringResource(
                                if (userRating == RecommendationRating.RATE_UP) {
                                    R.string.anime_media_recommendation_rate_up_filled_content_description
                                } else {
                                    R.string.anime_media_recommendation_rate_up_empty_content_description
                                }
                            ),
                        )
                    }
                }
            }

            if (viewer != null && entry != null) {
                MediaListQuickEditIconButton(
                    viewer = viewer,
                    mediaType = entry.media.type,
                    media = entry,
                    maxProgress = MediaUtils.maxProgress(entry.media),
                    maxProgressVolumes = entry.media.volumes,
                    onClick = { onClickListEdit(entry) },
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }

    @Composable
    private fun TitleText(entry: Entry?, paddingTop: Dp) {
        Text(
            text = entry?.media?.title?.primaryTitle() ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(start = 12.dp, top = paddingTop, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun SubtitleText(entry: Entry?) {
        val media = entry?.media
        Text(
            text = MediaUtils.formatSubtitle(
                format = media?.format,
                status = media?.status,
                season = media?.season,
                seasonYear = media?.seasonYear,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 10.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    interface Entry : MediaStatusAware {
        val media: MediaPreview
        val color: Color?
        val tags: List<AnimeMediaTagEntry>
    }
}
