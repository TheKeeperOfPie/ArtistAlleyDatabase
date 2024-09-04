package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.request.crossfade
import coil3.size.Dimension
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.MediaNavigationData
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
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateSharedTransitionWithOtherState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
object AnimeMediaCompactListRow {

    private val DEFAULT_IMAGE_HEIGHT = 100.dp
    private val DEFAULT_IMAGE_WIDTH = 72.dp

    @Composable
    operator fun invoke(
        viewer: AniListViewer?,
        entry: Entry?,
        modifier: Modifier = Modifier,
        onClickListEdit: (MediaNavigationData) -> Unit,
        label: @Composable() (() -> Unit)? = null,
        forceListEditIcon: Boolean = false,
        showQuickEdit: Boolean = true,
        coverImageState: CoilImageState = rememberCoilImageState(entry?.media?.coverImage?.extraLarge),
    ) {
        val sharedTransitionKey = entry?.media?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
        // Read colors here to request palette calculation
        val (containerColor, textColor) = coverImageState.colors
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = DEFAULT_IMAGE_HEIGHT)
                // Used to animate persistence of this view across screens
                .sharedElement(sharedTransitionKey, "media_compact_list_row")
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
        ) {
            val navigationCallback = LocalNavigationCallback.current
            val ignoreController = LocalIgnoreController.current
            val title = entry?.media?.title?.primaryTitle().orEmpty()
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .combinedClickable(
                        enabled = entry != null,
                        onClick = {
                            if (entry != null) {
                                navigationCallback.navigate(
                                    AnimeDestination.MediaDetails(
                                        mediaId = entry.media.id.toString(),
                                        title = title,
                                        coverImage = coverImageState.toImageState(),
                                        sharedTransitionKey = sharedTransitionKey,
                                        headerParams = MediaHeaderParams(
                                            coverImage = coverImageState.toImageState(),
                                            title = title,
                                            mediaCompactWithTags = entry.media,
                                        )
                                    )
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
                    viewer = viewer,
                    entry = entry,
                    imageState = coverImageState,
                    sharedTransitionKey = sharedTransitionKey,
                    onClick = {
                        if (entry != null) {
                            navigationCallback.navigate(
                                AnimeDestination.MediaDetails(
                                    mediaId = entry.media.id.toString(),
                                    title = title,
                                    coverImage = coverImageState.toImageState(),
                                    sharedTransitionKey = sharedTransitionKey,
                                    headerParams = MediaHeaderParams(
                                        coverImage = coverImageState.toImageState(),
                                        title = title,
                                        mediaCompactWithTags = entry.media,
                                    )
                                )
                            )
                        }
                    },
                    onClickListEdit = onClickListEdit,
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

                    MediaTagRow(
                        loading = entry == null,
                        tags = entry?.tags ?: AnimeMediaTagEntry.PLACEHOLDERS,
                        onTagClick = { id, name ->
                            if (entry != null) {
                                navigationCallback.navigate(
                                    AnimeDestination.SearchMedia(
                                        title = name,
                                        tagId = id,
                                        mediaType = entry.media.type ?: MediaType.ANIME,
                                    )
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

    @Composable
    private fun CoverImage(
        viewer: AniListViewer?,
        entry: Entry?,
        imageState: CoilImageState,
        sharedTransitionKey: SharedTransitionKey?,
        onClick: (Entry) -> Unit = {},
        onClickListEdit: (MediaNavigationData) -> Unit,
        forceListEditIcon: Boolean,
        showQuickEdit: Boolean,
    ) {
        Box {
            val fullscreenImageHandler = LocalFullscreenImageHandler.current
            val shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
            val sharedContentState = rememberSharedContentState(sharedTransitionKey, "media_image")
            MediaCoverImage(
                imageState = imageState,
                image = imageState.request()
                    .crossfade(true)
                    .size(
                        width = Dimension.Pixels(
                            LocalDensity.current.run { DEFAULT_IMAGE_WIDTH.roundToPx() }
                        ),
                        height = Dimension.Undefined
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    // Pad inside to offset the 1.dp border from the OutlinedCard
                    .padding(start = 1.dp, top = 1.dp, bottom = 1.dp)
                    .width(DEFAULT_IMAGE_WIDTH)
                    .heightIn(min = DEFAULT_IMAGE_HEIGHT)
                    .sharedElement(sharedContentState)
                    // Clip to match card so that shared element animation keeps rounded corner
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .placeholder(
                        visible = entry == null,
                        shape = shape,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .combinedClickable(
                        onClick = { if (entry != null) onClick(entry) },
                        onLongClick = { imageState.uri?.let(fullscreenImageHandler::openImage) },
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
                    onClick = { onClickListEdit(entry.media) },
                    padding = 6.dp,
                    forceListEditIcon = forceListEditIcon,
                    modifier = Modifier
                        .animateSharedTransitionWithOtherState(sharedContentState)
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

    @Stable
    interface Entry : MediaStatusAware {
        val media: MediaCompactWithTags
        val tags: List<AnimeMediaTagEntry>
    }
}
