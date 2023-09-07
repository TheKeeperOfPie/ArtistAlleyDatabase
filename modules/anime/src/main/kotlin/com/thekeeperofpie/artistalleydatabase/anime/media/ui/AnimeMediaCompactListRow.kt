package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.type.MediaType
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object AnimeMediaCompactListRow {

    private val DEFAULT_IMAGE_HEIGHT = 100.dp
    private val DEFAULT_IMAGE_WIDTH = 72.dp

    @Composable
    operator fun invoke(
        screenKey: String,
        viewer: AniListViewer?,
        entry: Entry?,
        modifier: Modifier = Modifier,
        onClickListEdit: (Entry) -> Unit,
        label: (@Composable () -> Unit)? = null,
        forceListEditIcon: Boolean = false,
        showQuickEdit: Boolean = true,
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        SharedElement(key = "anime_media_compact_row_${entry?.media?.id}", screenKey = screenKey) {
            OutlinedCard(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = DEFAULT_IMAGE_HEIGHT)
                    .alpha(if (entry?.ignored == true) 0.38f else 1f)
            ) {
                val navigationCallback = LocalNavigationCallback.current
                val ignoreController = LocalIgnoreController.current
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .combinedClickable(
                            enabled = entry != null,
                            onClick = {
                                if (entry != null) {
                                    navigationCallback.onMediaClick(
                                        entry.media,
                                        imageWidthToHeightRatio
                                    )
                                }
                            },
                            onLongClick = {
                                if (entry != null) {
                                    ignoreController.toggle(entry.media)
                                }
                            },
                        )
                ) {
                    CoverImage(
                        screenKey = screenKey,
                        viewer = viewer,
                        entry = entry,
                        onClick = {
                            if (entry != null) {
                                navigationCallback.onMediaClick(
                                    entry.media,
                                    imageWidthToHeightRatio
                                )
                            }
                        },
                        onClickListEdit = onClickListEdit,
                        onRatioAvailable = { imageWidthToHeightRatio = it },
                        forceListEditIcon = forceListEditIcon,
                        showQuickEdit = showQuickEdit,
                    )

                    Column(
                        modifier = Modifier
                            .heightIn(min = DEFAULT_IMAGE_HEIGHT)
                            .fillMaxHeight()
                    ) {
                        Row(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                label?.invoke()
                                TitleText(entry)
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

                        val colorCalculationState = LocalColorCalculationState.current
                        val (containerColor, textColor) =
                            colorCalculationState.getColors(entry?.media?.id?.toString())
                        MediaTagRow(
                            loading = entry == null,
                            tags = entry?.tags ?: AnimeMediaTagEntry.PLACEHOLDERS,
                            onTagClick = { id, name ->
                                if (entry != null) {
                                    navigationCallback.onTagClick(
                                        entry.media.type ?: MediaType.ANIME,
                                        id,
                                        name
                                    )
                                }
                            },
                            tagContainerColor = containerColor,
                            tagTextColor = textColor,
                            tagTextStyle = MaterialTheme.typography.bodySmall,
                            height = 20.dp,
                            startPadding = 8.dp,
                            bottomPadding = 8.dp,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CoverImage(
        screenKey: String,
        viewer: AniListViewer?,
        entry: Entry?,
        onClick: (Entry) -> Unit = {},
        onClickListEdit: (Entry) -> Unit,
        onRatioAvailable: (Float) -> Unit,
        forceListEditIcon: Boolean,
        showQuickEdit: Boolean,
    ) {
        Box {
            val fullscreenImageHandler = LocalFullscreenImageHandler.current
            val colorCalculationState = LocalColorCalculationState.current
            val shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
            MediaCoverImage(
                screenKey = screenKey,
                mediaId = entry?.media?.id?.toString(),
                image = ImageRequest.Builder(LocalContext.current)
                    .data(entry?.media?.coverImage?.extraLarge)
                    .crossfade(true)
                    .allowHardware(colorCalculationState.hasColor(entry?.media?.id?.toString()))
                    .size(
                        width = Dimension.Pixels(
                            LocalDensity.current.run { DEFAULT_IMAGE_WIDTH.roundToPx() }
                        ),
                        height = Dimension.Undefined
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                onSuccess = {
                    onRatioAvailable(it.widthToHeightRatio())
                    ComposeColorUtils.calculatePalette(
                        entry?.media?.id.toString(),
                        it,
                        colorCalculationState,
                    )
                },
                modifier = Modifier
                    // Clip to match card so that shared element animation keeps rounded corner
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .width(DEFAULT_IMAGE_WIDTH)
                    .heightIn(min = DEFAULT_IMAGE_HEIGHT)
                    .placeholder(
                        visible = entry == null,
                        shape = shape,
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

            if (viewer != null && entry != null && showQuickEdit) {
                MediaListQuickEditIconButton(
                    viewer = viewer,
                    mediaType = entry.media.type,
                    media = entry,
                    maxProgress = MediaUtils.maxProgress(entry.media),
                    maxProgressVolumes = entry.media.volumes,
                    onClick = { onClickListEdit(entry) },
                    padding = 6.dp,
                    forceListEditIcon = forceListEditIcon,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .widthIn(max = DEFAULT_IMAGE_WIDTH)
                )
            }
        }
    }

    @Composable
    private fun TitleText(entry: Entry?) {
        Text(
            text = if (entry == null) {
                "Some placeholder media title..."
            } else {
                entry.media.title?.primaryTitle().orEmpty()
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            minLines = 2,
            maxLines = 2,
            modifier = Modifier
                .wrapContentHeight(Alignment.Top)
                .padding(start = 8.dp, top = 8.dp, end = 16.dp)
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
                    status = null,
                    season = media?.season,
                    seasonYear = media?.seasonYear,
                )
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.typography.labelSmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 8.dp, top = 4.dp, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    interface Entry : MediaStatusAware {
        val media: MediaCompactWithTags
        val tags: List<AnimeMediaTagEntry>
    }
}
