@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@file:Suppress("NAME_SHADOWING")

package com.thekeeperofpie.artistalleydatabase.anime.ui

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.AccelerateEasing
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.VerticalDivider
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import de.charlex.compose.HtmlText

@Composable
internal fun CoverAndBannerHeader(
    coverImage: @Composable () -> String?,
    bannerImage: @Composable () -> String?,
    pinnedHeight: Dp,
    progress: Float = 0f,
    coverSize: Dp = 256.dp,
    coverImageWidthToHeightRatio: Float = 1f,
    color: () -> Color? = { null },
    onClickEnabled: Boolean = false,
    onClick: (() -> Unit)? = null,
    coverImageOnSuccess: (AsyncImagePainter.State.Success) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val elevation = lerp(0.dp, 16.dp, AccelerateEasing.transform(progress))

    Surface(
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = elevation,
        shadowElevation = elevation,
        modifier = Modifier.optionalClickable(onClick = onClick, enabled = onClickEnabled)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(bannerImage())
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.FillHeight,
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
                        if (color?.isSpecified == true) {
                            background(color)
                        } else this
                    }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = lerp(100.dp, 10.dp, progress), bottom = 10.dp)
                    .height(lerp(coverSize, coverSize - 76.dp, progress))
            ) {
                ElevatedCard {
                    var success by remember { mutableStateOf(false) }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(coverImage())
                            .crossfade(true)
                            .build(),
                        contentScale = ContentScale.FillHeight,
                        error = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        fallback = null,
                        onSuccess = {
                            success = true
                            coverImageOnSuccess(it)
                        },
                        contentDescription = stringResource(R.string.anime_media_cover_image),
                        modifier = Modifier
                            .conditionally(
                                coverImageWidthToHeightRatio == 1f,
                                Modifier::animateContentSize
                            )
                            .height(coverSize)
                            .run {
                                if (success) {
                                    wrapContentWidth()
                                } else if (coverImageWidthToHeightRatio != 1f) {
                                    width(coverSize * coverImageWidthToHeightRatio)
                                } else this
                            }
                            .widthIn(max = coverSize)
                    )
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
internal fun DetailsSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
    )
}

@Composable
internal fun DetailsSubsectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.surfaceTint,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
    )
}

internal fun LazyListScope.descriptionSection(
    @StringRes titleTextRes: Int,
    htmlText: String?,
    expanded: () -> Boolean,
    onExpandedChanged: (Boolean) -> Unit,
) {
    htmlText?.takeUnless(String::isEmpty) ?: return
    item {
        DetailsSectionHeader(
            stringResource(titleTextRes),
            modifier = Modifier.clickable { onExpandedChanged(!expanded()) }
        )
    }
    item {
        ElevatedCard(
            onClick = { onExpandedChanged(!expanded()) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .animateContentSize(),
        ) {
            val style = MaterialTheme.typography.bodyMedium
            val expanded = expanded()
            HtmlText(
                text = htmlText,
                style = style,
                color = style.color.takeOrElse { LocalContentColor.current },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .wrapContentHeight()
                    .heightIn(max = if (expanded) Dp.Unspecified else 80.dp)
                    .fadingEdgeBottom(show = !expanded)
            )
        }
    }
}


/**
 * @return True if anything shown
 */
@Composable
internal fun twoColumnInfoText(
    labelOne: String, bodyOne: String?, onClickOne: (() -> Unit)? = null,
    labelTwo: String, bodyTwo: String?, onClickTwo: (() -> Unit)? = null,
    showDividerAbove: Boolean = true
): Boolean {
    if (bodyOne != null && bodyTwo != null) {
        if (showDividerAbove) {
            Divider()
        }
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .optionalClickable(onClickOne)
            ) {
                InfoText(label = labelOne, body = bodyOne, showDividerAbove = false)
            }

            VerticalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .optionalClickable(onClickTwo)
            ) {
                InfoText(label = labelTwo, body = bodyTwo, showDividerAbove = false)
            }
        }
    } else if (bodyOne != null) {
        Column(modifier = Modifier.optionalClickable(onClickOne)) {
            InfoText(label = labelOne, body = bodyOne, showDividerAbove = showDividerAbove)
        }
    } else if (bodyTwo != null) {
        Column(modifier = Modifier.optionalClickable(onClickTwo)) {
            InfoText(label = labelTwo, body = bodyTwo, showDividerAbove = showDividerAbove)
        }
    } else {
        return false
    }

    return true
}

@Suppress("UnusedReceiverParameter")
@Composable
internal fun ColumnScope.InfoText(
    label: String,
    body: String,
    showDividerAbove: Boolean = true
) {
    if (showDividerAbove) {
        Divider()
    }

    DetailsSubsectionHeader(label)

    Text(
        text = body,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp)
    )
}

@Composable
internal fun <T> ExpandableListInfoText(
    @StringRes labelTextRes: Int,
    @StringRes contentDescriptionTextRes: Int,
    values: List<T>,
    valueToText: @Composable (T) -> String,
    onClick: ((T) -> Unit)? = null,
    showDividerAbove: Boolean = true,
) {
    if (values.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .heightIn(max = if (expanded) Dp.Unspecified else 120.dp)
                .clickable { expanded = !expanded }
                .fadingEdgeBottom(show = !expanded)
        ) {
            if (showDividerAbove) {
                Divider()
            }

            DetailsSubsectionHeader(stringResource(labelTextRes))

            values.forEachIndexed { index, value ->
                if (index != 0) {
                    Divider(modifier = Modifier.padding(start = 16.dp))
                }

                val bottomPadding = if (index == values.size - 1) {
                    12.dp
                } else {
                    8.dp
                }

                Text(
                    text = valueToText(value),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .optionalClickable(
                            onClick = onClick
                                ?.takeIf { expanded }
                                ?.let { { onClick(value) } }
                        )
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = bottomPadding,
                        )
                )
            }
        }

        TrailingDropdownIconButton(
            expanded = expanded,
            contentDescription = stringResource(contentDescriptionTextRes),
            onClick = { expanded = !expanded },
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}
