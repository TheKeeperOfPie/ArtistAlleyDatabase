package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil3.annotation.ExperimentalCoilApi
import coil3.request.crossfade
import coil3.size.Dimension
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MediaWithListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toIconContentDescription
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request

@OptIn(ExperimentalFoundationApi::class, ExperimentalCoilApi::class)
object MediaGridCard {

    @Composable
    operator fun invoke(
        entry: Entry?,
        viewer: AniListViewer?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        modifier: Modifier = Modifier,
        forceListEditIcon: Boolean = false,
        showQuickEdit: Boolean = true,
        showTypeIcon: Boolean = false,
        label: @Composable() (ColumnScope.(textColor: Color) -> Unit) = {},
    ) {
        val sharedTransitionKey = entry?.media?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
        val coverImageState = rememberCoilImageState(entry?.media?.coverImage?.extraLarge)
        val colors = coverImageState.colors
        val animationProgress by animateIntAsState(
            if (colors.containerColor.isUnspecified) 0 else 255,
            label = "Media grid card color fade in",
        )

        val surfaceColor = entry?.color ?: MaterialTheme.colorScheme.surface
        val containerColor = when {
            colors.containerColor.isUnspecified || animationProgress == 0 -> surfaceColor
            animationProgress == 255 -> colors.containerColor
            else -> Color(
                ColorUtils.compositeColors(
                    ColorUtils.setAlphaComponent(
                        colors.containerColor.toArgb(),
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
            val navigationCallback = LocalNavigationCallback.current
            val ignoreController = LocalIgnoreController.current
            val title = entry?.media?.title?.primaryTitle()
            Box(
                modifier = Modifier.combinedClickable(
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
                                        mediaWithListStatus = entry.media,
                                    )
                                )
                            )
                        }
                    },
                    onLongClick = {
                        if (entry?.media != null) {
                            ignoreController.toggle(entry.media)
                        }
                    }
                )
            ) {
                Column {
                    val title = entry?.media?.title?.primaryTitle()
                    Box {
                        CoverImage(
                            entry = entry,
                            sharedTransitionKey = sharedTransitionKey,
                            imageState = coverImageState,
                            viewer = viewer,
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
                                                mediaWithListStatus = entry.media,
                                            )
                                        )
                                    )
                                }
                            },
                            onClickListEdit = onClickListEdit,
                            forceListEditIcon = forceListEditIcon,
                            showQuickEdit = showQuickEdit,
                        )

                        if (showTypeIcon) {
                            Icon(
                                imageVector = entry?.type.toIcon(),
                                contentDescription = entry?.type.toIconContentDescription(),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .clip(RoundedCornerShape(bottomEnd = 6.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f))
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }

                        MediaRatingIconsSection(
                            rating = entry?.averageScore,
                            popularity = null,
                            showPopularity = false,
                            loading = entry == null,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(bottomStart = 6.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f))
                                .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                        )
                    }

                    val textColor = (ComposeColorUtils.bestTextColor(containerColor)
                        ?: Color.Unspecified)

                    label(textColor)

                    Text(
                        text = title ?: "Placeholder",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
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
        entry: Entry?,
        sharedTransitionKey: SharedTransitionKey?,
        imageState: CoilImageState,
        viewer: AniListViewer?,
        onClick: (Entry) -> Unit = {},
        onClickListEdit: (MediaNavigationData) -> Unit,
        forceListEditIcon: Boolean,
        showQuickEdit: Boolean,
    ) {
        Box {
            val fullscreenImageHandler = LocalFullscreenImageHandler.current
            MediaCoverImage(
                imageState = imageState,
                image = imageState.request()
                    .crossfade(true)
                    .size(
                        width = Dimension.Pixels(LocalDensity.current.run { 120.dp.roundToPx() }),
                        height = Dimension.Undefined
                    )
                    .build(),
                modifier = Modifier
                    .sharedElement(sharedTransitionKey, "media_image")
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
                        onLongClick = {
                            entry?.media?.coverImage?.extraLarge
                                ?.let(fullscreenImageHandler::openImage)
                        },
                        onLongClickLabel = stringResource(
                            R.string.anime_media_cover_image_long_press_preview
                        ),
                    ),
                contentScale = ContentScale.Crop,
            )

            if (viewer != null && entry != null && showQuickEdit) {
                MediaListQuickEditIconButton(
                    viewer = viewer,
                    mediaType = entry.type,
                    media = entry,
                    maxProgress = entry.maxProgress,
                    maxProgressVolumes = entry.maxProgressVolumes,
                    onClick = { onClickListEdit(entry.media) },
                    forceListEditIcon = forceListEditIcon,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }

    interface Entry : MediaStatusAware {
        val media: MediaWithListStatus
        val type: MediaType?
        val color: Color?
        val maxProgress: Int?
        val maxProgressVolumes: Int?
        val averageScore: Int?
    }
}
