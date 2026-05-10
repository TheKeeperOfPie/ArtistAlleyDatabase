@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.MonetizationOn: ImageVector
    get() {
        if (_monetizationOn != null) {
            return _monetizationOn!!
        }
        _monetizationOn = materialIcon(name = "Filled.MonetizationOn") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(13.41f, 18.09f)
                lineTo(13.41f, 20.0f)
                horizontalLineToRelative(-2.67f)
                verticalLineToRelative(-1.93f)
                curveToRelative(-1.71f, -0.36f, -3.16f, -1.46f, -3.27f, -3.4f)
                horizontalLineToRelative(1.96f)
                curveToRelative(0.1f, 1.05f, 0.82f, 1.87f, 2.65f, 1.87f)
                curveToRelative(1.96f, 0.0f, 2.4f, -0.98f, 2.4f, -1.59f)
                curveToRelative(0.0f, -0.83f, -0.44f, -1.61f, -2.67f, -2.14f)
                curveToRelative(-2.48f, -0.6f, -4.18f, -1.62f, -4.18f, -3.67f)
                curveToRelative(0.0f, -1.72f, 1.39f, -2.84f, 3.11f, -3.21f)
                lineTo(10.74f, 4.0f)
                horizontalLineToRelative(2.67f)
                verticalLineToRelative(1.95f)
                curveToRelative(1.86f, 0.45f, 2.79f, 1.86f, 2.85f, 3.39f)
                lineTo(14.3f, 9.34f)
                curveToRelative(-0.05f, -1.11f, -0.64f, -1.87f, -2.22f, -1.87f)
                curveToRelative(-1.5f, 0.0f, -2.4f, 0.68f, -2.4f, 1.64f)
                curveToRelative(0.0f, 0.84f, 0.65f, 1.39f, 2.67f, 1.91f)
                reflectiveCurveToRelative(4.18f, 1.39f, 4.18f, 3.91f)
                curveToRelative(-0.01f, 1.83f, -1.38f, 2.83f, -3.12f, 3.16f)
                close()
            }
        }
        return _monetizationOn!!
    }

private var _monetizationOn: ImageVector? = null
