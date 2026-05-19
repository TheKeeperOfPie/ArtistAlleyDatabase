package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val ArtStation = ImageVector.Builder(
    name = "ArtStation",
    defaultWidth = 24.dp,
    defaultHeight = (93.099998 / 105.8 * 24).dp,
    viewportWidth = 105.8f,
    viewportHeight = 93.100001f
).apply {
    group(
        translationX = -51.4f,
        translationY = -51.5f,
    ) {
        materialPath(
            fillAlpha = 1.0f,
            strokeAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(51.4f, 123.3f)
            lineToRelative(8.9f, 15.4f)
            verticalLineToRelative(0f)
            curveToRelative(1.80f, 3.50f, 5.40f, 5.90f, 9.50f, 5.90f)
            verticalLineToRelative(0f)
            verticalLineToRelative(0f)
            horizontalLineToRelative(59.3f)
            lineToRelative(-12.3f, -21.3f)
            close()
        }
        materialPath(
            fillAlpha = 1.0f,
            strokeAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(157.2f, 123.4f)
            curveToRelative(00f, -2.10f, -0.60f, -4.10f, -1.70f, -5.80f)
            lineTo(120.7f, 57.2f)
            curveToRelative(-1.80f, -3.40f, -5.30f, -5.70f, -9.40f, -5.70f)
            horizontalLineTo(92.9f)
            lineToRelative(53.7f, 93f)
            lineToRelative(8.5f, -14.7f)
            curveToRelative(1.60f, -2.80f, 2.10f, -40f, 2.10f, -6.40f)
            close()
        }
        materialPath(
            fillAlpha = 1.0f,
            strokeAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(60.2f, 108.1f)
            lineTo(108.1f, 108.1f)
            lineTo(84.2f, 66.6f)
            close()
        }
    }
}.build()
