package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.request.crossfade
import coil3.size.Dimension
import com.anilist.fragment.MediaHeaderData
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ui.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.compose.CustomHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.LocalAppTheme
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.request
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class
)
object AnimeMediaLargeCard {

    private val HEIGHT = 200.dp
    private val DESCRIPTION_PLACEHOLDER =
        "Some really long placeholder description for a loading media large card, "
        .repeat(3)

    @Composable
    operator fun invoke(
        viewer: AniListViewer?,
        entry: Entry?,
        modifier: Modifier = Modifier,
        label: (@Composable () -> Unit)? = null,
        forceListEditIcon: Boolean = false,
        showQuickEdit: Boolean = true,
        shouldTransitionCoverImageIfUsed: Boolean = true,
    ) {
        val sharedTransitionKey = entry?.mediaId?.let { SharedTransitionKey.makeKeyForId(it) }
        val sharedContentState = rememberSharedContentState(
            sharedTransitionKey.takeIf { entry?.imageBanner != null || shouldTransitionCoverImageIfUsed },
            if (entry?.imageBanner != null) "media_banner_image" else "media_image",
        )
        ElevatedCard(
            modifier = modifier
                .conditionally(entry?.imageBanner != null || shouldTransitionCoverImageIfUsed) {
                    sharedElement(sharedContentState)
                }
                .fillMaxWidth()
                .heightIn(min = HEIGHT)
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
                .recomposeHighlighter()
        ) {
            val navigationCallback = LocalNavigationCallback.current
            val ignoreController = LocalIgnoreController.current
            val title = entry?.primaryTitle()
            val imageState =
                rememberCoilImageState(entry?.imageBanner ?: entry?.image, requestColors = true)
            val isBanner = entry?.imageBanner != null
            Box(
                modifier = Modifier.combinedClickable(
                    enabled = entry != null,
                    onClick = {
                        if (entry != null) {
                            navigationCallback.navigate(
                                AnimeDestination.MediaDetails(
                                    mediaId = entry.mediaId,
                                    title = title,
                                    coverImage = if (!isBanner) imageState.toImageState() else ImageState(
                                        entry.image
                                    ),
                                    sharedTransitionKey = sharedTransitionKey,
                                    headerParams = MediaHeaderParams(
                                        bannerImage = imageState.takeIf { isBanner }
                                            ?.toImageState(),
                                        coverImage = imageState.takeUnless { isBanner }
                                            ?.toImageState(),
                                        title = title,
                                        media = entry,
                                    ),
                                )
                            )
                        }
                    },
                    onLongClick = {
                        if (entry != null) {
                            ignoreController.toggle(
                                mediaId = entry.mediaId,
                                type = entry.type,
                                isAdult = entry.isAdult,
                                bannerImage = entry.imageBanner,
                                coverImage = entry.image,
                                titleRomaji = entry.titleRomaji,
                                titleEnglish = entry.titleEnglish,
                                titleNative = entry.titleNative,
                            )
                        }
                    }
                )
            ) {
                BannerImage(
                    entry = entry,
                    imageState = imageState,
                )

                val foregroundAlpha by LocalAnimatedVisibilityScope.current.transition
                    .animateFloat(transitionSpec = { tween() }, label = "Foreground text alpha") {
                        when (it) {
                            EnterExitState.PreEnter -> 0f
                            EnterExitState.Visible -> 1f
                            EnterExitState.PostExit -> 0f
                        }
                    }
                Column(
                    modifier = Modifier
                        .alpha(foregroundAlpha.takeIf { sharedContentState?.isMatchFound == true }
                            ?: 1f)
                        .fillMaxHeight()
                        .heightIn(min = HEIGHT)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            label?.invoke()
                            TitleText(entry, title)
                            SubtitleText(entry)
                        }

                        MediaRatingIconsSection(
                            rating = entry?.rating,
                            popularity = entry?.popularity,
                            loading = entry == null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .wrapContentWidth()
                        )
                    }

                    Description(entry = entry)

                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(modifier = Modifier.weight(1f)) {
                            val nextAiringAt = entry?.nextAiringEpisode?.airingAt
                            val nextAiringEpisode = entry?.nextAiringEpisode?.episode
                            if (nextAiringAt != null && nextAiringEpisode != null) {
                                MediaNextAiringSection(
                                    airingAtAniListTimestamp = nextAiringAt,
                                    episode = nextAiringEpisode,
                                    episodes = entry.episodes,
                                    format = entry.format,
                                )
                            }
                            val (containerColor, textColor) = imageState.colors
                            MediaTagRow(
                                loading = entry == null,
                                tags = entry?.tags ?: AnimeMediaTagEntry.PLACEHOLDERS,
                                onTagClick = { id, name ->
                                    if (entry != null) {
                                        navigationCallback.navigate(
                                            AnimeDestination.SearchMedia(
                                                title = name,
                                                tagId = id,
                                                mediaType = entry.type ?: MediaType.ANIME,
                                            )
                                        )
                                    }
                                },
                                tagContainerColor = containerColor,
                                tagTextColor = textColor,
                            )
                        }

                        if (viewer != null && entry != null && showQuickEdit) {
                            MediaQuickEdit(
                                viewer = viewer,
                                entry = entry,
                                nextAiringEpisode = entry.nextAiringEpisode?.episode,
                                forceListEditIcon = forceListEditIcon,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BannerImage(
        entry: Entry?,
        imageState: CoilImageState,
    ) {
        val foregroundColor = MaterialTheme.colorScheme.surface
        val appTheme = LocalAppTheme.current
        val isLightTheme = appTheme == AppThemeSetting.LIGHT
                || (appTheme == AppThemeSetting.AUTO && !isSystemInDarkTheme())
        val alpha by animateFloatAsState(
            if (imageState.success) 1f else 0f,
            label = "AnimeMediaLargeCard banner image alpha",
        )
        CoilImage(
            state = imageState,
            model = imageState.request()
                .crossfade(true)
                .size(
                    width = Dimension.Undefined,
                    height = Dimension.Pixels(
                        LocalDensity.current.run { HEIGHT.roundToPx() / 2 }
                    ),
                )
                .build(),
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(
                R.string.anime_media_banner_image_content_description
            ),
            modifier = Modifier
                .background(entry?.color ?: MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxWidth()
                .height(HEIGHT)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        foregroundColor,
                        alpha = if (isLightTheme) 0.8f else 0.6f,
                    )
                }
                // Clip to match card so that shared element animation keeps rounded corner
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .alpha(alpha)
                .blurForScreenshotMode()
        )
    }

    @Composable
    private fun TitleText(entry: Entry?, title: String?) {
        Text(
            text = if (entry == null) {
                "Placeholder media title..."
            } else {
                title.orEmpty()
            },
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(start = 12.dp, top = 10.dp, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun SubtitleText(entry: Entry?) {
        Text(
            text = if (entry == null) {
                "Placeholder subtitle text..."
            } else {
                MediaUtils.formatSubtitle(
                    format = entry.format,
                    status = entry.status,
                    season = entry.season,
                    seasonYear = entry.seasonYear,
                )
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 4.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun ColumnScope.Description(entry: Entry?) {
        val description = if (entry == null) {
            DESCRIPTION_PLACEHOLDER
        } else {
            entry.description
        }

        if (description == null) {
            Spacer(Modifier.weight(1f))
        } else {
            CustomHtmlText(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                fontWeight = FontWeight.Black,
                overflow = TextOverflow.Ellipsis,
                detectTaps = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .placeholder(
                        visible = entry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }

    @Composable
    private fun MediaQuickEdit(
        viewer: AniListViewer?,
        entry: Entry,
        nextAiringEpisode: Int?,
        forceListEditIcon: Boolean,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            val editViewModel = hiltViewModel<MediaEditViewModel>()
            MediaListQuickEditIconButton(
                viewer = viewer,
                mediaType = entry.type,
                media = entry,
                maxProgress = MediaUtils.maxProgress(
                    type = entry.type,
                    chapters = entry.chapters,
                    episodes = entry.episodes,
                    nextAiringEpisode = nextAiringEpisode,
                ),
                maxProgressVolumes = entry.volumes,
                forceListEditIcon = forceListEditIcon,
                onClick = {
                    editViewModel.initialize(
                        mediaId = entry.mediaId,
                        coverImage = entry.image,
                        type = entry.type,
                        titleRomaji = entry.titleRomaji,
                        titleEnglish = entry.titleEnglish,
                        titleNative = entry.titleNative,
                    )
                },
            )
        }
    }

    interface Entry : MediaStatusAware, MediaHeaderData {
        val mediaId: String
        val image: String?
        val imageBanner: String?
        val color: Color?
        val rating: Int?
        val tags: List<AnimeMediaTagEntry>
        val description: String?
        val titleRomaji: String?
        val titleEnglish: String?
        val titleNative: String?
        val chapters: Int?
        val volumes: Int?

        @Composable
        fun primaryTitle(): String?
    }
}
