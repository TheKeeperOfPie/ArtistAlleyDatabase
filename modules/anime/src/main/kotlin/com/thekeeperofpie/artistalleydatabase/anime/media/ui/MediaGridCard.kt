package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.AuthedUserQuery
import com.anilist.fragment.MediaNavigationData
import com.anilist.type.MediaType
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object MediaGridCard {

    @Composable
    operator fun invoke(
        screenKey: String,
        entry: Entry?,
        viewer: AuthedUserQuery.Data.Viewer?,
        onClickListEdit: (Entry) -> Unit,
        onLongClick: (MediaNavigationData) -> Unit,
        onLongPressImage: (entry: Entry) -> Unit,
        colorCalculationState: ColorCalculationState,
        modifier: Modifier = Modifier,
    ) {
        val colors = colorCalculationState.colorMap[entry?.media?.id?.toString()]
        val animationProgress by animateIntAsState(
            if (colors == null) 0 else 255,
            label = "Media grid card color fade in",
        )

        val surfaceColor = entry?.color ?: MaterialTheme.colorScheme.surface
        val containerColor = when {
            colors == null || animationProgress == 0 -> surfaceColor
            animationProgress == 255 -> colors.first
            else -> Color(
                ColorUtils.compositeColors(
                    ColorUtils.setAlphaComponent(
                        colors.first.toArgb(),
                        animationProgress
                    ),
                    surfaceColor.toArgb()
                )
            )
        }

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
        ) {
            var imageWidthToHeightRatio by remember { MutableSingle(1f) }
            val navigationCallback = LocalNavigationCallback.current
            Box(
                modifier = Modifier.combinedClickable(
                    enabled = entry != null,
                    onClick = {
                        if (entry != null) {
                            navigationCallback?.onMediaClick(entry.media, imageWidthToHeightRatio)
                        }
                    },
                    onLongClick = { if (entry?.media != null) onLongClick(entry.media) }
                )
            ) {
                Column {
                    Box {
                        CoverImage(
                            screenKey = screenKey,
                            entry = entry,
                            viewer = viewer,
                            onClick = {
                                if (entry != null) {
                                    navigationCallback?.onMediaClick(
                                        entry.media,
                                        imageWidthToHeightRatio
                                    )
                                }
                            },
                            onClickListEdit = onClickListEdit,
                            onLongPressImage = onLongPressImage,
                            colorCalculationState = colorCalculationState,
                            onRatioAvailable = { imageWidthToHeightRatio = it },
                        )

                        MediaRatingIconsSection(
                            rating = entry?.averageScore,
                            popularity = null,
                            loading = entry == null,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(bottomStart = 12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f))
                                .padding(start = 8.dp, top = 2.dp, bottom = 4.dp)
                        )
                    }

                    Text(
                        text = entry?.media?.title?.primaryTitle() ?: "Placeholder",
                        style = MaterialTheme.typography.labelSmall,
                        color = ComposeColorUtils.bestTextColor(containerColor)
                            ?: Color.Unspecified,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        minLines = 2,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .placeholder(
                                visible = entry == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
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
        onLongPressImage: (entry: Entry) -> Unit,
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
                            width = Dimension.Pixels(LocalDensity.current.run { 120.dp.roundToPx() }),
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
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .fillMaxSize()
                        .aspectRatio(0.66f)
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
                        mediaType = entry.type,
                        listStatus = entry.mediaListStatus,
                        progress = entry.progress,
                        progressVolumes = entry.progressVolumes,
                        maxProgress = entry.maxProgress,
                        maxProgressVolumes = entry.maxProgressVolumes,
                        onClick = { onClickListEdit(entry) },
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }
    }

    interface Entry : MediaStatusAware {
        val media: MediaNavigationData
        val type: MediaType?
        val color: Color?
        val maxProgress: Int?
        val maxProgressVolumes: Int?
        val averageScore: Int?
    }
}
