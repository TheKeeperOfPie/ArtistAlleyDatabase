package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.fadingEdgeEnd(
    startTransparent: Dp = 4.dp,
    startOpaque: Dp = 16.dp,
    endOpaque: Dp = 16.dp,
    endTransparent: Dp = 4.dp,
) = composed {
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val startTransparentStop = startTransparent / size.width.toDp()
            val startOpaqueStop = startOpaque / size.width.toDp()
            val endOpaqueStop = endOpaque / size.width.toDp()
            val endTransparentStop = endTransparent / size.width.toDp()
            val brush = Brush.horizontalGradient(
                startTransparentStop to Color.Transparent,
                startTransparentStop + startOpaqueStop to Color.Black,
                (1f - endOpaqueStop - endTransparentStop) to Color.Black,
                (1f - endTransparentStop) to Color.Transparent,
            )
            onDrawWithContent {
                drawContent()
                drawRect(
                    brush = brush,
                    blendMode = BlendMode.DstIn
                )
            }
        }
}

fun Modifier.fadingEdgeBottom(show: Boolean = true, firstStop: Float = 0.8f) =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val brush = Brush.verticalGradient(
                firstStop to Color.Black,
                1f to Color.Transparent,
            )
            onDrawWithContent {
                drawContent()
                if (show) {
                    drawRect(brush, blendMode = BlendMode.DstIn)
                }
            }
        }

fun Modifier.optionalClickable(
    onClick: (() -> Unit)?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null
) = if (onClick == null) this else clickable(
    enabled = enabled,
    onClick = onClick,
    onClickLabel = onClickLabel,
    role = role,
)

@Composable
fun Modifier.conditionally(apply: Boolean, block: @Composable Modifier.() -> Modifier) =
    if (apply) block() else this

@Composable
fun <T> Modifier.conditionallyNonNull(target: T?, block: @Composable Modifier.(T) -> Modifier) =
    if (target != null) block(target) else this

fun Modifier.topBorder(color: Color, width: Dp = Dp.Hairline): Modifier = border(
    width,
    color,
    startOffsetX = { width.value * density },
    startOffsetY = { 0f },
    endOffsetX = { size.width - (width.value * density / 2) },
    endOffsetY = { 0f }
)

fun Modifier.bottomBorder(color: Color, width: Dp = Dp.Hairline): Modifier = border(
    width,
    color,
    startOffsetX = { 0f },
    startOffsetY = { size.height - (width.value / 2 * density) },
    endOffsetX = { size.width },
    endOffsetY = { size.height - (width.value / 2 * density) }
)

@Suppress("UnnecessaryComposedModifier")
fun Modifier.border(
    width: Dp = Dp.Hairline,
    color: Color,
    startOffsetX: ContentDrawScope.() -> Float,
    startOffsetY: ContentDrawScope.() -> Float,
    endOffsetX: ContentDrawScope.() -> Float,
    endOffsetY: ContentDrawScope.() -> Float,
): Modifier = composed(
    factory = {
        this.then(
            Modifier.drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawLine(
                        color = color,
                        start = Offset(startOffsetX(), startOffsetY()),
                        end = Offset(endOffsetX(), endOffsetY()),
                        strokeWidth = width.value * density,
                    )
                }
            }
        )
    },
    inspectorInfo = debugInspectorInfo {
        name = "border"
        properties["width"] = width
        properties["color"] = color.value
        value = color
        properties["shape"] = RectangleShape
    }
)
