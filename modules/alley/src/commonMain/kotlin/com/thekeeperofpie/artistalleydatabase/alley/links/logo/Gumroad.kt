package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val Gumroad = ImageVector.Builder(
    name = "Gumroad",
    defaultWidth = 24.dp,
    defaultHeight = (80.186 / 80.777 * 24).dp,
    viewportWidth = 21.372f,
    viewportHeight = 21.216f
).apply {
    group(scaleX = 0.26458f, scaleY = 0.26458f) {
        materialPath(
            fillAlpha = 0f,
            strokeAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(38.605f, 76.433f)
            curveToRelative(20.8920f, 00f, 37.8280f, -16.9360f, 37.8280f, -37.8280f)
            curveToRelative(00f, -20.890f, -16.9360f, -37.8270f, -37.8280f, -37.8270f)
            curveTo(17.7150f, 0.7780f, 0.7790f, 17.7140f, 0.7790f, 38.6060f)
            curveToRelative(00f, 20.890f, 16.9360f, 37.8270f, 37.8280f, 37.8270f)
            close()
        }
    }
    materialPath(
        fillAlpha = 1.0f,
        strokeAlpha = 1.0f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(9.364f, 15.153f)
        curveToRelative(-2.870f, 00f, -4.5590f, -2.3020f, -4.5590f, -5.1660f)
        curveToRelative(00f, -2.9760f, 1.8580f, -5.390f, 5.4030f, -5.390f)
        curveToRelative(3.6590f, 00f, 4.8970f, 2.470f, 4.9530f, 3.8740f)
        horizontalLineToRelative(-2.645f)
        curveToRelative(-0.0560f, -0.7860f, -0.7320f, -1.9660f, -2.3640f, -1.9660f)
        curveToRelative(-1.7450f, 00f, -2.870f, 1.5170f, -2.870f, 3.370f)
        curveToRelative(00f, 1.8530f, 1.1250f, 3.370f, 2.870f, 3.370f)
        curveToRelative(1.5760f, 00f, 2.2520f, -1.2360f, 2.5330f, -2.4720f)
        horizontalLineToRelative(-2.533f)
        verticalLineToRelative(-1.01f)
        horizontalLineToRelative(5.315f)
        verticalLineToRelative(5.166f)
        horizontalLineToRelative(-2.332f)
        verticalLineToRelative(-3.257f)
        curveToRelative(-0.1690f, 1.180f, -0.90f, 3.4820f, -3.7710f, 3.4820f)
        close()
    }
    group(
        translationX = -32.42f,
        translationY = -9.51f,
    ) {
        materialPath(
            fillAlpha = 1.0f,
            strokeAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(50.664f, 13.746f)
            arcToRelative(
                9.963f,
                9.963f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                1.974f,
                5.968f
            )
            curveToRelative(00f, 5.5280f, -4.480f, 10.0090f, -10.0080f, 10.0090f)
            arcToRelative(
                9.96f,
                9.96f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                -4.955f,
                -1.314f
            )
            arcToRelative(
                9.758f,
                9.758f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = false,
                6.313f,
                2.307f
            )
            arcToRelative(
                9.8f,
                9.8f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = false,
                9.8f,
                -9.8f
            )
            arcToRelative(
                9.771f,
                9.771f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = false,
                -3.124f,
                -7.17f
            )
            close()
        }
    }
}.build()
