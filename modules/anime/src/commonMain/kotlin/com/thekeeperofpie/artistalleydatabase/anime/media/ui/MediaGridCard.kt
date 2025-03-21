package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_long_press_preview
import coil3.annotation.ExperimentalCoilApi
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaWithListStatus
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toIconContentDescription
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import org.jetbrains.compose.resources.stringResource

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
        val sharedTransitionKey =
            entry?.media?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
        val coverImageState = rememberCoilImageState(entry?.media?.coverImage?.extraLarge)
        val colors = coverImageState.colors
        val animationProgress by animateFloatAsState(
            if (colors.containerColor.isUnspecified) 0f else 1f,
            label = "Media grid card color fade in",
        )

        val surfaceColor = entry?.color ?: MaterialTheme.colorScheme.surface
        val containerColor = when {
            colors.containerColor.isUnspecified || animationProgress == 0f -> surfaceColor
            animationProgress == 1f -> colors.containerColor
            else -> colors.containerColor.copy(alpha = animationProgress)
                .compositeOver(surfaceColor)
        }

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
        ) {
            val navigationController = LocalNavigationController.current
            val ignoreController = LocalIgnoreController.current
            val title = entry?.media?.title?.primaryTitle()
            Box(
                modifier = Modifier.combinedClickable(
                    enabled = entry != null,
                    onClick = {
                        if (entry != null) {
                            navigationController.navigate(
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
                                    navigationController.navigate(
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
                image = imageState.request().build(),
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
                            Res.string.anime_media_cover_image_long_press_preview
                        ),
                    ),
                contentScale = ContentScale.Crop,
            )

            if (viewer != null && entry != null && showQuickEdit) {
                MediaListQuickEditIconButton(
                    viewer = viewer,
                    media = entry,
                    onClick = { onClickListEdit(entry.media) },
                    forceListEditIcon = forceListEditIcon,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }

    interface Entry : MediaListQuickEditButtonData {
        val media: MediaWithListStatus
        val color: Color?
        val averageScore: Int?
        val ignored: Boolean
    }
}
