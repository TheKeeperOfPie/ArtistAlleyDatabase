package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val Kickstarter = ImageVector.Builder(
    name = "Kickstarter",
    defaultWidth = 24.dp,
    defaultHeight = (288.313 / 256.288 * 24).dp,
    viewportWidth = 67.81f,
    viewportHeight = 76.283f,
).apply {
    materialPath(
        fillAlpha = 1.0f,
        strokeAlpha = 1.0f,
        pathFillType = PathFillType.NonZero,
    ) {
        moveTo(52.995f, 82.115f)
        horizontalLineToRelative(0.264f)
        curveToRelative(9.8550f, 13.990f, 32.1890f, 7.880f, 33.8220f, -8.9960f)
        curveToRelative(0.8320f, -8.60f, -5.760f, -16.4960f, -12.390f, -21.1660f)
        verticalLineToRelative(-0.265f)
        curveToRelative(6.6370f, -4.6760f, 13.2240f, -12.5540f, 12.390f, -21.1670f)
        curveTo(85.450f, 13.6610f, 63.1050f, 7.550f, 53.260f, 21.5250f)
        curveToRelative(-2.4460f, -2.6550f, -4.7780f, -5.1060f, -8.2020f, -6.4940f)
        curveToRelative(-8.6530f, -3.5090f, -19.540f, -0.0680f, -23.6840f, 8.6110f)
        curveToRelative(-2.410f, 5.050f, -1.980f, 10.4350f, -1.980f, 15.8750f)
        verticalLineTo(63.86f)
        curveToRelative(00f, 5.3520f, -0.5220f, 10.8860f, 1.8230f, 15.8750f)
        curveToRelative(5.7240f, 12.1760f, 24.450f, 14.0970f, 31.7790f, 2.3810f)
        close()
    }
}.build()
