package com.thekeeperofpie.artistalleydatabase.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.fadingEdgeEnd(
    startTransparent: Dp = 4.dp,
    startOpaque: Dp = 16.dp,
    endOpaque: Dp = 16.dp,
    endTransparent: Dp = 4.dp,
) = composed {
    val localConfiguration = LocalConfiguration.current
    val startTransparentStop = remember { (startTransparent / localConfiguration.screenWidthDp.dp) }
    val startOpaqueStop = remember { (startOpaque / localConfiguration.screenWidthDp.dp) }
    val endOpaqueStop = remember { (endOpaque / localConfiguration.screenWidthDp.dp) }
    val endTransparentStop = remember { (endTransparent / localConfiguration.screenWidthDp.dp) }
    val fadingEdgeBrush = remember {
        Brush.horizontalGradient(
            startTransparentStop to Color.Transparent,
            startTransparentStop + startOpaqueStop to Color.Black,
            (1f - endOpaqueStop - endTransparentStop) to Color.Black,
            (1f - endTransparentStop) to Color.Transparent,
        )
    }

    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            onDrawWithContent {
                drawContent()
                drawRect(
                    brush = fadingEdgeBrush,
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

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.conditionally(apply: Boolean, block: @Composable Modifier.() -> Modifier) =
    composed { if (apply) block() else this }
