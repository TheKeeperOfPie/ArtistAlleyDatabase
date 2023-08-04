package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.AuthedUserQuery
import com.anilist.fragment.MediaHeaderData
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object AnimeMediaListRow {

    @Composable
    operator fun <MediaType : MediaPreview> invoke(
        screenKey: String,
        entry: Entry<MediaType>?,
        viewer: AuthedUserQuery.Data.Viewer?,
        modifier: Modifier = Modifier,
        label: (@Composable () -> Unit)? = null,
        onClickListEdit: (Entry<MediaType>) -> Unit,
        onLongClick: (Entry<MediaType>) -> Unit,
        onLongPressImage: (entry: Entry<MediaType>) -> Unit = {},
        nextAiringEpisode: MediaPreview.NextAiringEpisode? = entry?.media?.nextAiringEpisode,
        colorCalculationState: ColorCalculationState = ColorCalculationState(),
        navigationCallback: AnimeNavigator.NavigationCallback? = null,
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
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
                        navigationCallback?.onMediaClick(
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
                            navigationCallback?.onMediaClick(entry, null, imageWidthToHeightRatio)
                        }
                    },
                    onClickListEdit = onClickListEdit,
                    onLongPressImage = onLongPressImage,
                    colorCalculationState = colorCalculationState,
                    onRatioAvailable = { imageWidthToHeightRatio = it },
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

                    nextAiringEpisode?.let { MediaNextAiringSection(it) }

                    val (containerColor, textColor) =
                        colorCalculationState.getColors(entry?.media?.id?.toString())
                    MediaTagRow(
                        tags = entry?.tags.orEmpty(),
                        onTagClick = { id, name ->
                            if (entry != null) {
                                navigationCallback?.onTagClick(
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

    @Composable
    private fun <MediaType : MediaPreview> CoverImage(
        screenKey: String,
        entry: Entry<MediaType>?,
        viewer: AuthedUserQuery.Data.Viewer?,
        onClick: (Entry<MediaType>) -> Unit = {},
        onClickListEdit: (Entry<MediaType>) -> Unit,
        onLongPressImage: (entry: Entry<MediaType>) -> Unit,
        colorCalculationState: ColorCalculationState,
        onRatioAvailable: (Float) -> Unit,
    ) {
        SharedElement(
            key = "anime_media_${entry?.media?.id}_image",
            screenKey = screenKey,
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(entry?.media?.coverImage?.extraLarge)
                        .crossfade(true)
                        .allowHardware(colorCalculationState.hasColor(entry?.media?.id?.toString()))
                        .size(
                            width = Dimension.Pixels(LocalDensity.current.run { 130.dp.roundToPx() }),
                            height = Dimension.Undefined
                        )
                        .build(),
                    contentScale = ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
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
                    contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
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
                            onLongClick = { if (entry != null) onLongPressImage(entry) },
                            onLongClickLabel = stringResource(
                                R.string.anime_media_cover_image_long_press_preview
                            ),
                        )
                )

                if (viewer != null && entry != null) {
                    MediaListQuickEditIconButton(
                        mediaType = entry.media.type,
                        listStatus = entry.mediaListStatus,
                        progress = entry.progress,
                        progressVolumes = entry.progressVolumes,
                        maxProgress = MediaUtils.maxProgress(entry.media),
                        maxProgressVolumes = entry.media.volumes,
                        onClick = { onClickListEdit(entry) },
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }
    }

    @Composable
    private fun <MediaType : MediaPreview> TitleText(
        entry: Entry<MediaType>?,
        paddingTop: Dp,
    ) {
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
    private fun <MediaType : MediaPreview> SubtitleText(entry: Entry<MediaType>?) {
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

    open class Entry<MediaType>(
        val media: MediaType,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = null,
        override val progressVolumes: Int? = null,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : MediaStatusAware where MediaType : MediaPreview, MediaType : MediaHeaderData {
        open val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
        val tags = media.tags?.asSequence()
            ?.filterNotNull()
            ?.filter {
                showLessImportantTags
                        || it.category !in MediaUtils.LESS_IMPORTANT_MEDIA_TAG_CATEGORIES
            }
            ?.filter {
                showSpoilerTags || (it.isGeneralSpoiler != true && it.isMediaSpoiler != true)
            }
            ?.map(::AnimeMediaTagEntry)
            ?.distinctBy { it.id }
            ?.toList()
            .orEmpty()
    }
}
