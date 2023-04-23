package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.fadingEdge(
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

fun Modifier.optionalClickable(onClick: (() -> Unit)?) =
    if (onClick == null) this else clickable(onClick = onClick)
