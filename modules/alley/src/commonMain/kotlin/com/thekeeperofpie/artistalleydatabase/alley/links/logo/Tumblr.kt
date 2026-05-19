package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val Tumblr = ImageVector.Builder(
    name = "Tumblr",
    defaultWidth = (289.999f / 512.184f * 24f).dp,
    defaultHeight = 24.dp,
    viewportWidth = 289.999f,
    viewportHeight = 512.184f
).apply {
    materialPath(
        fillAlpha = 1.0f,
        strokeAlpha = 1.0f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(204.435f, 512.184f)
        curveToRelative(-77.020f, 00f, -134.4270f, -39.6290f, -134.4270f, -134.4380f)
        verticalLineTo(225.914f)
        horizontalLineTo(0f)
        verticalLineTo(143.7f)
        curveTo(77.0480f, 123.6990f, 109.260f, 57.4080f, 112.9830f, 00f)
        horizontalLineToRelative(79.974f)
        verticalLineToRelative(130.361f)
        horizontalLineToRelative(93.314f)
        verticalLineToRelative(95.553f)
        horizontalLineToRelative(-93.314f)
        verticalLineToRelative(132.21f)
        curveToRelative(00f, 39.6290f, 19.9950f, 53.3230f, 51.8520f, 53.3230f)
        horizontalLineToRelative(45.19f)
        verticalLineToRelative(100.737f)
        horizontalLineToRelative(-85.564f)
        close()
    }
}.build()
