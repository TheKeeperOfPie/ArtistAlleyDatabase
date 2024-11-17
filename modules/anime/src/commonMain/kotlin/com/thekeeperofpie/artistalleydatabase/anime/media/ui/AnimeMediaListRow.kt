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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_long_press_preview
import artistalleydatabase.modules.anime.generated.resources.anime_media_recommendation_rate_down_empty_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_recommendation_rate_down_filled_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_recommendation_rate_up_empty_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_recommendation_rate_up_filled_content_description
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.type.MediaType
import com.anilist.data.type.RecommendationRating
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.data.NextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateSharedTransitionWithOtherState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class
)
object AnimeMediaListRow {

    private val DEFAULT_IMAGE_WIDTH = 130.dp

    @Composable
    operator fun invoke(
        entry: Entry?,
        viewer: AniListViewer?,
        modifier: Modifier = Modifier,
        label: @Composable (() -> Unit)? = null,
        onClickListEdit: (MediaNavigationData) -> Unit,
        forceListEditIcon: Boolean = false,
        showQuickEdit: Boolean = true,
        nextAiringEpisode: NextAiringEpisode? = entry?.media?.nextAiringEpisode?.let {
            NextAiringEpisode(
                episode = it.episode,
                airingAt = Instant.fromEpochSeconds(it.airingAt.toLong()),
            )
        },
        showDate: Boolean = true,
        recommendation: RecommendationData? = null,
        onUserRecommendationRating: (recommendation: RecommendationData, newRating: RecommendationRating) -> Unit = { _, _ -> },
    ) {
        val sharedTransitionKey = entry?.media?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        val coverImageState =
            rememberCoilImageState(entry?.media?.coverImage?.extraLarge, requestColors = true)
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
            val onClickEntry = {
                if (entry != null) {
                    val media = entry.media
                    navigationCallback.navigate(
                        AnimeDestination.MediaDetails(
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
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .combinedClickable(
                        enabled = entry != null,
                        onClick = onClickEntry,
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
                    onClick = onClickEntry,
                    onClickListEdit = onClickListEdit,
                    recommendation = recommendation,
                    onUserRecommendationRating = onUserRecommendationRating,
                    forceListEditIcon = forceListEditIcon,
                    showQuickEdit = showQuickEdit,
                )
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.heightIn(min = 180.dp)
                ) {
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

                    Column {
                        nextAiringEpisode?.let {
                            MediaNextAiringSection(
                                nextAiringEpisode = nextAiringEpisode,
                                episodes = entry?.media?.episodes,
                                format = entry?.media?.format,
                                showDate = showDate,
                            )
                        }

                        val (containerColor, textColor) = coverImageState.colors
                        val tags = entry?.tags ?: AnimeMediaTagEntry.PLACEHOLDERS
                        if (tags.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                        } else {
                            MediaTagRow(
                                loading = entry == null,
                                tags = tags,
                                onTagClick = { id, name ->
                                    if (entry != null) {
                                        navigationCallback.navigate(
                                            AnimeDestination.SearchMedia(
                                                title =
                                                    AnimeDestination.SearchMedia.Title.Custom(name),
                                                tagId = id,
                                                mediaType = entry.media.type ?: MediaType.ANIME,
                                            )
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
    }

    @Composable
    private fun CoverImage(
        entry: Entry?,
        sharedTransitionKey: SharedTransitionKey?,
        imageState: CoilImageState,
        viewer: AniListViewer?,
        onClick: () -> Unit,
        onClickListEdit: (MediaNavigationData) -> Unit,
        recommendation: RecommendationData?,
        onUserRecommendationRating: (
            recommendation: RecommendationData,
            newRating: RecommendationRating,
        ) -> Unit,
        forceListEditIcon: Boolean,
        showQuickEdit: Boolean,
    ) {
        Box(
            Modifier.width(DEFAULT_IMAGE_WIDTH)
                .fillMaxHeight()
        ) {
            val fullscreenImageHandler = LocalFullscreenImageHandler.current
            val shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
            val sharedContentState = rememberSharedContentState(sharedTransitionKey, "media_image")
            MediaCoverImage(
                imageState = imageState,
                image = imageState.request().build(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .placeholder(
                        visible = entry == null,
                        shape = shape,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .sharedElement(sharedContentState)
                    // Clip to match card so that shared element animation keeps rounded corner
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { imageState.uri?.let(fullscreenImageHandler::openImage) },
                        onLongClickLabel = stringResource(
                            Res.string.anime_media_cover_image_long_press_preview
                        ),
                    )
            )

            if (viewer != null && recommendation != null) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
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
                                    Res.string.anime_media_recommendation_rate_down_filled_content_description
                                } else {
                                    Res.string.anime_media_recommendation_rate_down_empty_content_description
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
                                    Res.string.anime_media_recommendation_rate_up_filled_content_description
                                } else {
                                    Res.string.anime_media_recommendation_rate_up_empty_content_description
                                }
                            ),
                        )
                    }
                }
            }

            if (viewer != null && entry != null && showQuickEdit) {
                MediaListQuickEditIconButton(
                    viewer = viewer,
                    media = entry,
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
                    startDate = media?.startDate,
                ).orEmpty()
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

    interface Entry : MediaListQuickEditButtonData {
        val media: MediaPreview
        val color: Color?
        val tags: List<AnimeMediaTagEntry>
        val ignored: Boolean
    }
}
