package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.utils.BuildVariant
import com.thekeeperofpie.artistalleydatabase.utils.isDebug
import kotlinx.coroutines.delay
import kotlin.math.min

@Suppress("MayBeConstant")
private val FORCE_ENABLE = false

/**
 * A [Modifier] that draws a border around elements that are recomposing. The border increases in
 * size and interpolates from red to green as more recompositions occur before a timeout.
 */
@Stable
fun Modifier.recomposeHighlighter(trackTimeout: Boolean = false) =
    if (!BuildVariant.isDebug() && !FORCE_ENABLE) this else this.then(
        Modifier.composed(
            inspectorInfo = debugInspectorInfo { name = "recomposeHighlighter" },
        ) {
            // The total number of compositions that have occurred. We're not using a State<> here be
            // able to read/write the value without invalidating (which would cause infinite
            // recomposition).
            val totalCompositions = remember { arrayOf(0L) }
            totalCompositions[0]++

            // The value of totalCompositions at the last timeout.
            var totalCompositionsAtLastTimeout by remember { mutableLongStateOf(0L) }

            // Start the timeout, and reset everytime there's a recomposition. (Using totalCompositions
            // as the key is really just to cause the timer to restart every composition).
            if (trackTimeout) {
                LaunchedEffect(totalCompositions[0]) {
                    delay(3000)
                    totalCompositionsAtLastTimeout = totalCompositions[0]
                }
            }

            Modifier.drawWithCache {
                onDrawWithContent {
                    // Draw actual content.
                    drawContent()

                    // Below is to draw the highlight, if necessary. A lot of the logic is copied from
                    // Modifier.border
                    val numCompositionsSinceTimeout =
                        totalCompositions[0] - totalCompositionsAtLastTimeout

                    val hasValidBorderParams = size.minDimension > 0f
                    if (!hasValidBorderParams || numCompositionsSinceTimeout <= 0) {
                        return@onDrawWithContent
                    }

                    val (color, strokeWidthPx) =
                        when (numCompositionsSinceTimeout) {
                            // We need at least one composition to draw, so draw the smallest border
                            // color in blue.
                            1L -> Color.Blue to 1f
                            // 2 compositions is _probably_ okay.
                            2L -> Color.Green to 2.dp.toPx()
                            // 3 or more compositions before timeout may indicate an issue. lerp the
                            // color from yellow to red, and continually increase the border size.
                            else -> {
                                lerp(
                                    Color.Yellow.copy(alpha = 0.8f),
                                    Color.Red.copy(alpha = 0.5f),
                                    min(1f, (numCompositionsSinceTimeout - 1).toFloat() / 100f)
                                ) to numCompositionsSinceTimeout.toInt().dp.toPx()
                            }
                        }

                    val halfStroke = strokeWidthPx / 2
                    val topLeft = Offset(halfStroke, halfStroke)
                    val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)

                    val fillArea = (strokeWidthPx * 2) > size.minDimension
                    val rectTopLeft = if (fillArea) Offset.Zero else topLeft
                    val size = if (fillArea) size else borderSize
                    val style = if (fillArea) Fill else Stroke(strokeWidthPx)

                    drawRect(
                        brush = SolidColor(color),
                        topLeft = rectTopLeft,
                        size = size,
                        style = style
                    )
                }
            }
        }
    )

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

fun Modifier.fadingEdgeBottom(show: Boolean = true, firstStop: Float = 0.8f, lastStop: Float = 1f) =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val brush = Brush.verticalGradient(
                firstStop to Color.Black,
                lastStop to Color.Transparent,
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
inline fun Modifier.conditionally(apply: Boolean, block: @Composable Modifier.() -> Modifier) =
    if (apply) block() else this

@Composable
inline fun <T> Modifier.conditionallyNonNull(
    target: T?,
    block: @Composable Modifier.(T) -> Modifier,
) = if (target != null) block(target) else this

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

@Suppress("UnnecessaryComposedModifier")
fun Modifier.border(
    width: Dp = Dp.Hairline,
    color: Color,
    start: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false,
): Modifier = composed(
    factory = {
        this.then(
            Modifier.drawWithCache {
                onDrawWithContent {
                    drawContent()
                    if (start) {
                        drawLine(
                            color = color,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height)
                        )
                    }
                    if (end) {
                        drawLine(
                            color = color,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height)
                        )
                    }
                    if (bottom) {
                        drawLine(
                            color = color,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height)
                        )
                    }
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
