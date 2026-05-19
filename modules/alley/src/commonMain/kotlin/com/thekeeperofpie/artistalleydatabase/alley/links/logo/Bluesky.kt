package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val Bluesky = ImageVector.Builder(
    name = "Bluesky",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 32f,
    viewportHeight = 32f
).apply {
    materialPath(
        fillAlpha = 1.0f,
        strokeAlpha = 1.0f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(23.931f, 5.298f)
        curveToRelative(-3.210f, 2.4180f, -6.6630f, 7.320f, -7.9310f, 9.9510f)
        curveToRelative(-1.2670f, -2.6310f, -4.7210f, -7.5330f, -7.9310f, -9.9510f)
        curveToRelative(-2.3160f, -1.7440f, -6.0690f, -3.0940f, -6.0690f, 1.2010f)
        curveToRelative(00f, 0.8570f, 0.490f, 7.2060f, 0.7780f, 8.2370f)
        curveToRelative(0.9990f, 3.5830f, 4.6410f, 4.4970f, 7.8810f, 3.9440f)
        curveToRelative(-5.6630f, 0.9670f, -7.1030f, 4.1690f, -3.9920f, 7.3720f)
        curveToRelative(5.9080f, 6.0830f, 8.4920f, -1.5260f, 9.1540f, -3.4760f)
        curveToRelative(0.1230f, -0.360f, 0.1790f, -0.5270f, 0.1790f, -0.3790f)
        curveToRelative(00f, -0.1480f, 0.0570f, 0.0190f, 0.1790f, 0.3790f)
        curveToRelative(0.6620f, 1.9490f, 3.2450f, 9.5580f, 9.1540f, 3.4760f)
        curveToRelative(3.1110f, -3.2030f, 1.6710f, -6.4050f, -3.9920f, -7.3720f)
        curveToRelative(3.240f, 0.5530f, 6.8820f, -0.3610f, 7.8810f, -3.9440f)
        curveToRelative(0.2880f, -1.0310f, 0.7780f, -7.380f, 0.7780f, -8.2370f)
        curveToRelative(00f, -4.2950f, -3.7530f, -2.9450f, -6.0690f, -1.2010f)
        close()
    }
}.build()
