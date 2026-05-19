package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val DeviantArt = ImageVector.Builder(
    name = "DeviantArt",
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
        moveTo(24.403f, 2f)
        horizontalLineToRelative(-4.82f)
        lineToRelative(-0.514f, 0.511f)
        lineToRelative(-2.452f, 4.68f)
        lineToRelative(-0.716f, 0.413f)
        horizontalLineTo(7.597f)
        verticalLineToRelative(6.995f)
        horizontalLineToRelative(4.437f)
        lineToRelative(0.462f, 0.462f)
        lineToRelative(-4.899f, 9.351f)
        verticalLineToRelative(5.588f)
        horizontalLineToRelative(0f)
        lineToRelative(4.823f, -0.002f)
        lineToRelative(0.516f, -0.513f)
        lineToRelative(2.457f, -4.682f)
        lineToRelative(0.701f, -0.405f)
        horizontalLineToRelative(8.309f)
        verticalLineToRelative(-6.991f)
        horizontalLineToRelative(-4.45f)
        lineToRelative(-0.45f, -0.45f)
        lineToRelative(4.901f, -9.356f)
    }
}.build()
