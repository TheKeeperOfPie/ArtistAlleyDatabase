package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.size.Dimension
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MediaPreview
import com.anilist.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.compose.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.request
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.animateSharedTransitionWithOtherState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElement

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalCoilApi::class
)
object AnimeMediaListRow {

    @Composable
    operator fun invoke(
        entry: Entry?,
        viewer: AniListViewer?,
        modifier: Modifier = Modifier,
        label: @Composable (() -> Unit)? = null,
        onClickListEdit: (MediaNavigationData) -> Unit,
        forceListEditIcon: Boolean = false,
        showQuickEdit: Boolean = true,
        nextAiringEpisode: MediaPreview.NextAiringEpisode? = entry?.media?.nextAiringEpisode,
        showDate: Boolean = true,
        recommendation: RecommendationData? = null,
        onUserRecommendationRating: (recommendation: RecommendationData, newRating: RecommendationRating) -> Unit = { _, _ -> },
    ) {
        val sharedTransitionKey = SharedTransitionKey.makeKeyForId(entry?.media?.id.toString())
        val coverImageState = rememberCoilImageState(entry?.media?.coverImage?.extraLarge)
        val navigationCallback = LocalNavigationCallback.current
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
                .padding(bottom = 2.dp)
                .recomposeHighlighter()
        ) {
            val ignoreController = LocalIgnoreController.current
            val title = entry?.media?.title?.primaryTitle()
            Row(modifier = Modifier
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    enabled = entry != null,
                    onClick = {
                        if (entry != null) {
                            val media = entry.media
                            navigationCallback.navigate(
                                AnimeDestinations.MediaDetails(
                                    mediaId = media.id.toString(),
                                    title = title,
                                    coverImage = coverImageState.toImageState(),
                                    sharedTransitionKey = sharedTransitionKey,
                                    headerParams = MediaHeaderParams(
                                        coverImage = coverImageState.toImageState(),
                                        title = title,
                                        media = media,
                                    ),
                                )
                            )
                        }
                    },
                    onLongClick = {
                        if (entry != null) {
                            ignoreController.toggle(entry.media)
                        }
                    }
                )
            ) {
                CoverImage(
                    entry = entry,
                    sharedTransitionKey = sharedTransitionKey,
                    imageState = coverImageState,
                    viewer = viewer,
                    onClickListEdit = onClickListEdit,
                    recommendation = recommendation,
                    onUserRecommendationRating = onUserRecommendationRating,
                    forceListEditIcon = forceListEditIcon,
                    showQuickEdit = showQuickEdit,
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

                    nextAiringEpisode?.let {
                        MediaNextAiringSection(
                            nextAiringEpisode = nextAiringEpisode,
                            episodes = entry?.media?.episodes,
                            format = entry?.media?.format,
                            showDate = showDate,
                        )
                    }

                    val colorCalculationState = LocalColorCalculationState.current
                    val (containerColor, textColor) =
                        colorCalculationState.getColors(entry?.media?.id?.toString())
                    val tags = entry?.tags ?: AnimeMediaTagEntry.PLACEHOLDERS
                    if (tags.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                    } else {
                        MediaTagRow(
                            loading = entry == null,
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
        entry: Entry?,
        sharedTransitionKey: SharedTransitionKey?,
        imageState: CoilImageState,
        viewer: AniListViewer?,
        onClick: ((Entry) -> Unit)? = null,
        onClickListEdit: (MediaNavigationData) -> Unit,
        recommendation: RecommendationData?,
        onUserRecommendationRating: (
            recommendation: RecommendationData,
            newRating: RecommendationRating,
        ) -> Unit,
        forceListEditIcon: Boolean,
        showQuickEdit: Boolean,
    ) {
        Box {
            val fullscreenImageHandler = LocalFullscreenImageHandler.current
            val colorCalculationState = LocalColorCalculationState.current
            val shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
            val sharedContentState = rememberSharedContentState(sharedTransitionKey, "media_image")
            MediaCoverImage(
                imageState = imageState,
                image = imageState.request()
                    .crossfade(true)
                    .allowHardware(colorCalculationState.allowHardware(entry?.media?.id?.toString()))
                    .size(
                        width = Dimension.Pixels(LocalDensity.current.run { 130.dp.roundToPx() }),
                        height = Dimension.Undefined
                    )
                    .listener(onSuccess = { _, result ->
                        entry?.media?.id?.let { mediaId ->
                            ComposeColorUtils.calculatePalette(
                                id = mediaId.toString(),
                                image = result.image,
                                colorCalculationState = colorCalculationState,
                            )
                        }
                    })
                    .build(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .sharedElement(sharedContentState)
                    // Clip to match card so that shared element animation keeps rounded corner
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxHeight()
                    .heightIn(min = 180.dp)
                    .width(130.dp)
                    .placeholder(
                        visible = entry == null,
                        shape = shape,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .combinedClickable(
                        onClick = { if (entry != null) onClick?.invoke(entry) },
                        onLongClick = {
                            imageState.uri?.let(fullscreenImageHandler::openImage)
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

            if (viewer != null && entry != null && showQuickEdit) {
                MediaListQuickEditIconButton(
                    viewer = viewer,
                    mediaType = entry.media.type,
                    media = entry,
                    maxProgress = MediaUtils.maxProgress(entry.media),
                    maxProgressVolumes = entry.media.volumes,
                    onClick = { onClickListEdit(entry.media) },
                    forceListEditIcon = forceListEditIcon,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .animateSharedTransitionWithOtherState(sharedContentState)
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
            text = if (entry == null) {
                "Placeholder subtitle"
            } else {
                MediaUtils.formatSubtitle(
                    format = media?.format,
                    status = media?.status,
                    season = media?.season,
                    seasonYear = media?.seasonYear,
                )
            },
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
