package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val Cara = ImageVector.Builder(
    name = "Cara",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 216f,
    viewportHeight = 216f
).apply {
    path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 8f
    ) {
        moveTo(212f, 108f)
        arcTo(104f, 104f, 0f, false, true, 108f, 212f)
        arcTo(104f, 104f, 0f, false, true, 4f, 108f)
        arcTo(104f, 104f, 0f, false, true, 212f, 108f)
        close()
    }
    materialPath(
        fillAlpha = 1.0f,
        strokeAlpha = 1.0f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(139.544f, 88.537f)
        lineTo(156.435f, 65.8124f)
        curveTo(129.61f, 44.5268f, 88.6368f, 45.8605f, 65.0496f, 72.4297f)
        curveTo(57.2323f, 81.2353f, 52.2065f, 92.7229f, 51.6263f, 106.658f)
        curveTo(50.8191f, 126.046f, 59.5276f, 144f, 73.1033f, 153.638f)
        curveTo(98.7823f, 171.867f, 133.354f, 167.468f, 157.997f, 150.187f)
        lineTo(142.9f, 126.121f)
        curveTo(124.779f, 140.215f, 91.5073f, 138.872f, 89.208f, 110.685f)
        curveTo(86.9088f, 82.4968f, 118.067f, 74.4431f, 139.544f, 88.537f)
        close()
    }
}.build()
