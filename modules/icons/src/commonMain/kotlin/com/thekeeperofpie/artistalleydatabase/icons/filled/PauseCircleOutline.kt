@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.PauseCircleOutline: ImageVector
    get() {
        if (_pauseCircleOutline != null) {
            return _pauseCircleOutline!!
        }
        _pauseCircleOutline = materialIcon(name = "Filled.PauseCircleOutline") {
            materialPath {
                moveTo(9.0f, 16.0f)
                horizontalLineToRelative(2.0f)
                lineTo(11.0f, 8.0f)
                lineTo(9.0f, 8.0f)
                verticalLineToRelative(8.0f)
                close()
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.59f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(13.0f, 16.0f)
                horizontalLineToRelative(2.0f)
                lineTo(15.0f, 8.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(8.0f)
                close()
            }
        }
        return _pauseCircleOutline!!
    }

private var _pauseCircleOutline: ImageVector? = null
