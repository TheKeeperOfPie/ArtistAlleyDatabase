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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import coil.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.AccelerateEasing
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import de.charlex.compose.HtmlText

@Composable
fun CoverAndBannerHeader(
    coverImage: @Composable () -> String?,
    bannerImage: @Composable () -> String?,
    pinnedHeight: Dp,
    progress: Float = 0f,
    coverSize: Dp = 256.dp,
    color: () -> Color? = { null },
    onClickEnabled: Boolean = false,
    onClick: (() -> Unit)? = null,
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
                model = bannerImage(),
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
                    AsyncImage(
                        model = coverImage(),
                        contentScale = ContentScale.FillHeight,
                        error = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        fallback = null,
                        contentDescription = stringResource(R.string.anime_media_cover_image),
                        modifier = Modifier
                            .height(coverSize)
                            .widthIn(max = coverSize)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(top = lerp(32.dp, 0.dp, progress))
                        .animateContentSize()
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun DetailsSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
    )
}

fun LazyListScope.descriptionSection(
    @StringRes titleTextRes: Int,
    htmlText: String?,
    expanded: () -> Boolean,
    onExpandedChanged: (Boolean) -> Unit,
) {
    htmlText ?: return
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
