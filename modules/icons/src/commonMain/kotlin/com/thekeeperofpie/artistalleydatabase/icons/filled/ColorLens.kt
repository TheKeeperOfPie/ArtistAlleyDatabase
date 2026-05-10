@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ColorLens: ImageVector
    get() {
        if (_colorLens != null) {
            return _colorLens!!
        }
        _colorLens = materialIcon(name = "Filled.ColorLens") {
            materialPath {
                moveTo(12.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                reflectiveCurveToRelative(4.03f, 9.0f, 9.0f, 9.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                curveToRelative(0.0f, -0.39f, -0.15f, -0.74f, -0.39f, -1.01f)
                curveToRelative(-0.23f, -0.26f, -0.38f, -0.61f, -0.38f, -0.99f)
                curveToRelative(0.0f, -0.83f, 0.67f, -1.5f, 1.5f, -1.5f)
                lineTo(16.0f, 16.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -4.42f, -4.03f, -8.0f, -9.0f, -8.0f)
                close()
                moveTo(6.5f, 12.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(5.67f, 9.0f, 6.5f, 9.0f)
                reflectiveCurveTo(8.0f, 9.67f, 8.0f, 10.5f)
                reflectiveCurveTo(7.33f, 12.0f, 6.5f, 12.0f)
                close()
                moveTo(9.5f, 8.0f)
                curveTo(8.67f, 8.0f, 8.0f, 7.33f, 8.0f, 6.5f)
                reflectiveCurveTo(8.67f, 5.0f, 9.5f, 5.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(10.33f, 8.0f, 9.5f, 8.0f)
                close()
                moveTo(14.5f, 8.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(13.67f, 5.0f, 14.5f, 5.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(15.33f, 8.0f, 14.5f, 8.0f)
                close()
                moveTo(17.5f, 12.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(16.67f, 9.0f, 17.5f, 9.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
            }
        }
        return _colorLens!!
    }

private var _colorLens: ImageVector? = null
