@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.DeviceUnknown: ImageVector
    get() {
        if (_deviceUnknown != null) {
            return _deviceUnknown!!
        }
        _deviceUnknown = materialIcon(name = "Filled.DeviceUnknown") {
            materialPath {
                moveTo(17.0f, 1.0f)
                lineTo(7.0f, 1.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(19.0f, 3.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(17.0f, 19.0f)
                lineTo(7.0f, 19.0f)
                lineTo(7.0f, 5.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(14.0f)
                close()
                moveTo(12.0f, 6.72f)
                curveToRelative(-1.96f, 0.0f, -3.5f, 1.52f, -3.5f, 3.47f)
                horizontalLineToRelative(1.75f)
                curveToRelative(0.0f, -0.93f, 0.82f, -1.75f, 1.75f, -1.75f)
                reflectiveCurveToRelative(1.75f, 0.82f, 1.75f, 1.75f)
                curveToRelative(0.0f, 1.75f, -2.63f, 1.57f, -2.63f, 4.45f)
                horizontalLineToRelative(1.76f)
                curveToRelative(0.0f, -1.96f, 2.62f, -2.19f, 2.62f, -4.45f)
                curveToRelative(0.0f, -1.96f, -1.54f, -3.47f, -3.5f, -3.47f)
                close()
                moveTo(11.12f, 15.52f)
                horizontalLineToRelative(1.76f)
                verticalLineToRelative(1.76f)
                horizontalLineToRelative(-1.76f)
                close()
            }
        }
        return _deviceUnknown!!
    }

private var _deviceUnknown: ImageVector? = null
