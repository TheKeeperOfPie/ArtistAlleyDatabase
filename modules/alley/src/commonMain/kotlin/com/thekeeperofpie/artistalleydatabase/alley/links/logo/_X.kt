package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val _X = ImageVector.Builder(
    name = "X",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 16f,
    viewportHeight = 16f
).apply {
    materialPath(
        fillAlpha = 1.0f,
        strokeAlpha = 1.0f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(12.6f, 0.75f)
        horizontalLineToRelative(2.454f)
        lineToRelative(-5.36f, 6.142f)
        lineTo(16f, 15.25f)
        horizontalLineToRelative(-4.937f)
        lineToRelative(-3.867f, -5.07f)
        lineToRelative(-4.425f, 5.07f)
        horizontalLineTo(0.316f)
        lineToRelative(5.733f, -6.57f)
        lineTo(0f, 0.75f)
        horizontalLineToRelative(5.063f)
        lineToRelative(3.495f, 4.633f)
        lineTo(12.601f, 0.75f)
        close()
        moveToRelative(-0.86f, 13.028f)
        horizontalLineToRelative(1.36f)
        lineTo(4.323f, 2.145f)
        horizontalLineTo(2.865f)
        close()
    }
}.build()
