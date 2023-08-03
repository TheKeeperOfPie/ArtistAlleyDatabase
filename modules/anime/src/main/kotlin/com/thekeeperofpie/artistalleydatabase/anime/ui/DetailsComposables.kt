@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalFoundationApi::class
)
@file:Suppress("NAME_SHADOWING")

package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.size.Dimension
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.AccelerateEasing
import com.thekeeperofpie.artistalleydatabase.compose.CustomHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
internal fun CoverAndBannerHeader(
    screenKey: String,
    upIconOption: UpIconOption?,
    entryId: EntryId?,
    coverImage: @Composable () -> String?,
    coverImageAllowHardware: Boolean,
    bannerImage: @Composable () -> String? = { null },
    pinnedHeight: Dp = 120.dp,
    progress: Float = 0f,
    coverSize: Dp = 256.dp,
    coverImageWidthToHeightRatio: Float = 1f,
    color: () -> Color? = { null },
    onClickEnabled: Boolean = false,
    onClick: (() -> Unit)? = null,
    coverImageOnSuccess: (AsyncImagePainter.State.Success) -> Unit = {},
    menuContent: (@Composable RowScope.() -> Unit)? = null,
    fadeOutMenu: Boolean = true,
    reserveMenuWidth: Boolean = !fadeOutMenu,
    onCoverImageSharedElementFractionChanged: ((Float) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val elevation = lerp(0.dp, 16.dp, AccelerateEasing.transform(progress))
    val bottomCornerDp = lerp(0.dp, 12.dp, progress)

    Surface(
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = elevation,
        shadowElevation = elevation,
        modifier = Modifier
            .clip(RoundedCornerShape(bottomStart = bottomCornerDp, bottomEnd = bottomCornerDp))
            .optionalClickable(onClick = onClick, enabled = onClickEnabled)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(bannerImage())
                    .crossfade(true)
                    .size(
                        width = Dimension.Undefined,
                        height = Dimension.Pixels(LocalDensity.current.run { 180.dp.roundToPx() }),
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.anime_media_banner_image),
                modifier = Modifier
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
                        val color = color()
                        val hasColor = color?.isSpecified == true
                        val alpha by animateFloatAsState(
                            if (hasColor) 1f else 0f,
                            label = "Banner image background alpha fade",
                        )
                        if (hasColor && alpha > 0f) {
                            background(color!!.copy(alpha = alpha))
                        } else this
                    }
            )

            if (upIconOption != null && progress != 1f) {
                UpIconButton(
                    option = upIconOption, modifier = Modifier
                        .align(Alignment.TopStart)
                        .alpha(1f - progress)
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
                    SharedElement(
                        key = "${entryId?.scopedId}_image",
                        screenKey = screenKey,
                        onFractionChanged = onCoverImageSharedElementFractionChanged,
                    ) {
                        ElevatedCard {
                            val imageHeight = rowHeight - 20.dp
                            var success by remember { mutableStateOf(false) }
                            val maxWidth = LocalConfiguration.current.screenWidthDp.dp * 0.4f
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(coverImage())
                                    .crossfade(true)
                                    .allowHardware(coverImageAllowHardware)
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
                                onSuccess = {
                                    success = true
                                    coverImageOnSuccess(it)
                                },
                                contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                                modifier = Modifier
                                    .height(imageHeight)
                                    .run {
                                        if (coverImageWidthToHeightRatio != 1f) {
                                            width(
                                                (imageHeight * coverImageWidthToHeightRatio)
                                                    .coerceAtMost(maxWidth)
                                            )
                                        } else if (success) {
                                            wrapContentWidth()
                                        } else this
                                    }
                                    .widthIn(max = maxWidth)
                            )
                        }
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
    errorResource: @Composable () -> Pair<Int, Throwable?>?,
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
    errorResource: @Composable () -> Pair<Int, Exception?>?,
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
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

internal fun LazyListScope.descriptionSection(
    markdownText: String?,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    imagesSupported: Boolean = false,
) {
    markdownText?.takeUnless(String::isEmpty) ?: return
    item("descriptionSection") {
        ElevatedCard(
            onClick = { onExpandedChange(!expanded()) },
            modifier = Modifier
                .animateContentSize()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 2.dp)
                .fillMaxWidth()
        ) {
            val style = MaterialTheme.typography.bodyMedium
            val expanded = expanded()

            if (imagesSupported) {
                ImageHtmlText(
                    text = markdownText.replaceSpoilers(),
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    color = style.color.takeOrElse { LocalContentColor.current },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .wrapContentHeight()
                )
            } else {
                CustomHtmlText(
                    text = markdownText.replaceSpoilers(),
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    style = style,
                    color = style.color.takeOrElse { LocalContentColor.current },
                    overflow = TextOverflow.Ellipsis,
                    onFallbackClick = { onExpandedChange(!expanded()) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .wrapContentHeight()
                )
            }
        }
    }
}

// TODO: Find a way to show spoilers on click
@Composable
private fun String.replaceSpoilers(): String {
    val spoilerRegex = Regex("(?<=<span class='markdown_spoiler'><span>).+?(?=</span></span>)")
    return replace(spoilerRegex, stringResource(R.string.anime_description_spoiler))
}
