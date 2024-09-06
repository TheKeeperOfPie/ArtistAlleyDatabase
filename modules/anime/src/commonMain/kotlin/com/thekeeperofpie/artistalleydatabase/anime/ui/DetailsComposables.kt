@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalFoundationApi::class
)
@file:Suppress("NAME_SHADOWING")

package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_banner_image
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_long_press_preview
import artistalleydatabase.modules.anime.generated.resources.anime_unfold_less_text
import coil3.request.crossfade
import coil3.size.Dimension
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.AccelerateEasing
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CoverAndBannerHeader(
    upIconOption: UpIconOption?,
    headerValues: DetailsHeaderValues,
    modifier: Modifier = Modifier,
    sharedTransitionKey: SharedTransitionKey?,
    coverImageSharedTransitionIdentifier: String,
    bannerImageSharedTransitionIdentifier: String,
    pinnedHeight: Dp = 120.dp,
    progress: Float = 0f,
    coverSize: Dp = 256.dp,
    onClickEnabled: Boolean = false,
    onClick: (() -> Unit)? = null,
    coverImageState: CoilImageState? = rememberCoilImageState(headerValues.coverImage),
    menuContent: @Composable() (RowScope.() -> Unit)? = null,
    fadeOutMenu: Boolean = true,
    reserveMenuWidth: Boolean = !fadeOutMenu,
    onCoverImageClick: (() -> Unit)? = null,
    content: @Composable() (ColumnScope.() -> Unit),
) {
    val elevation = lerp(0.dp, 16.dp, AccelerateEasing.transform(progress))
    val bottomCornerDp = lerp(0.dp, 12.dp, progress)

    val fullscreenImageHandler = LocalFullscreenImageHandler.current
    Surface(
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = elevation,
        shadowElevation = elevation,
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = bottomCornerDp, bottomEnd = bottomCornerDp))
            .combinedClickable(
                enabled = onClickEnabled,
                onClick = onClick ?: {},
                onLongClick = { headerValues.bannerImage?.uri?.let(fullscreenImageHandler::openImage) },
                onLongClickLabel = stringResource(
                    Res.string.anime_media_cover_image_long_press_preview
                ),
            )
    ) {
        Box {
            val bannerImageState = rememberCoilImageState(headerValues.bannerImage)
            CoilImage(
                state = bannerImageState,
                model = bannerImageState.request()
                    .crossfade(true)
                    .size(
                        width = Dimension.Undefined,
                        height = Dimension.Pixels(LocalDensity.current.run { 180.dp.roundToPx() }),
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(Res.string.anime_media_banner_image),
                modifier = Modifier
                    .conditionally(headerValues.bannerImage != null) {
                        sharedElement(sharedTransitionKey, bannerImageSharedTransitionIdentifier)
                    }
                    .fillMaxWidth()
                    .height(lerp(180.dp, pinnedHeight, progress))
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithCache {
                        val brush = Brush.verticalGradient(
                            AnimationUtils.lerp(0.5f, 0f, progress) to
                                    Color.Black.copy(
                                        alpha = AnimationUtils.lerp(1f, 0.25f, progress)
                                    ),
                            1f to Color.Transparent,
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.DstIn)
                        }
                    }
                    .run {
                        val color = bannerImageState.colors.containerColor.takeOrElse {
                            coverImageState?.colors?.containerColor?.takeOrElse {
                                headerValues.defaultColor
                            } ?: Color.Unspecified
                        }
                        val hasColor = color.isSpecified
                        val alpha by animateFloatAsState(
                            if (hasColor) 1f else 0f,
                            label = "Banner image background alpha fade",
                        )
                        if (hasColor && alpha > 0f) {
                            background(color.copy(alpha = alpha))
                        } else this
                    }
                    .blurForScreenshotMode()
            )

            val maxAlpha by LocalAnimatedVisibilityScope.current.transition
                .animateFloat(label = "CoverAndBannerHeader upIconOption transition") {
                    when (it) {
                        EnterExitState.PreEnter -> 0f
                        EnterExitState.Visible -> 1f
                        EnterExitState.PostExit -> 0f
                    }
                }

            if (upIconOption != null) {
                UpIconButton(
                    option = upIconOption,
                    modifier = Modifier
                        .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                        .align(Alignment.TopStart)
                        .alpha((1f - progress).coerceAtMost(maxAlpha))
                        .clip(RoundedCornerShape(bottomEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f))
                )
            }

            if (menuContent != null && (!fadeOutMenu || progress != 1f)) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .conditionally(fadeOutMenu) { alpha(1f - progress) }
                        .clip(RoundedCornerShape(bottomStart = 12.dp))
                        .background(
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = if (fadeOutMenu) 0.66f else (0.66f * (1f - progress))
                            )
                        )
                ) {
                    menuContent()
                }
            }

            val rowHeight = lerp(coverSize, pinnedHeight, progress)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = lerp(100.dp, 0.dp, progress),
                        // Reserve some space if the menu should be retained,
                        // currently only supports 1 option (40dp)
                        end = if (reserveMenuWidth) lerp(0.dp, 40.dp, progress) else 0.dp
                    )
                    .height(rowHeight)
            ) {
                Box(modifier = Modifier.padding(vertical = 10.dp)) {
                    ElevatedCard(
                        modifier = Modifier
                            .sharedElement(sharedTransitionKey, coverImageSharedTransitionIdentifier)
                    ) {
                        val imageHeight = rowHeight - 20.dp
                        val maxWidth = LocalConfiguration.current.screenWidthDp.dp * 0.4f
                        CoilImage(
                            state = coverImageState,
                            model = coverImageState.request()
                                .crossfade(true)
                                .size(
                                    width = Dimension.Undefined,
                                    height = Dimension.Pixels(
                                        LocalDensity.current.run { imageHeight.roundToPx() }
                                    ),
                                )
                                .build(),
                            contentScale = ContentScale.FillHeight,
                            error = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            fallback = null,
                            contentDescription = stringResource(Res.string.anime_media_cover_image_content_description),
                            modifier = Modifier
                                .height(imageHeight)
                                .wrapContentWidth()
                                .widthIn(max = maxWidth)
                                .combinedClickable(
                                    onClick = { onCoverImageClick?.invoke() },
                                    onLongClick = {
                                        coverImageState?.uri?.let(fullscreenImageHandler::openImage)
                                    }
                                )
                                .blurForScreenshotMode()
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(top = lerp(32.dp, 0.dp, progress))
                        .widthIn(min = 120.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun DetailsLoadingOrError(
    loading: Boolean,
    errorResource: @Composable () -> Pair<StringResource, Throwable?>?,
) {
    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }
    } else {
        val errorResource = errorResource()
        AnimeMediaListScreen.Error(
            errorTextRes = errorResource?.first,
            exception = errorResource?.second,
        )
    }
}

internal fun LazyListScope.detailsLoadingOrError(
    loading: Boolean,
    errorResource: @Composable () -> Pair<StringResource, Exception?>?,
) {
    if (loading) {
        item("loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            }
        }
    } else {
        item("error") {
            val errorResource = errorResource()
            AnimeMediaListScreen.Error(
                errorTextRes = errorResource?.first,
                exception = errorResource?.second,
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
internal fun DescriptionSection(
    markdownText: MarkdownText?,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    var showExpand by rememberSaveable { mutableStateOf(false) }
    val expanded = expanded()
    ElevatedCard(
        onClick = {
            if (showExpand) {
                onExpandedChange(!expanded)
            }
        },
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 2.dp)
            .fillMaxWidth()
            .recomposeHighlighter()
    ) {
        val style = MaterialTheme.typography.bodyMedium

        Box(modifier = Modifier.height(IntrinsicSize.Min)) {
            MarkdownText(
                markdownText = markdownText,
                textColor = style.color.takeOrElse { LocalContentColor.current },
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                onOverflowChange = { showExpand = it },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .conditionally(!expanded && showExpand) { fadingEdgeBottom() }
                    .fillMaxWidth()
                    .animateContentSize()
            )

            if (!expanded && showExpand) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { onExpandedChange(true) }
                )
            }
        }

        if (expanded) {
            HorizontalDivider()
            TextButton(
                onClick = { onExpandedChange(false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.anime_unfold_less_text))
            }
        }
    }
}
