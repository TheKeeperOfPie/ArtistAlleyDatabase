@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Badge: ImageVector
    get() {
        if (_badge != null) {
            return _badge!!
        }
        _badge = materialIcon(name = "Filled.Badge") {
            materialPath {
                moveTo(20.0f, 7.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-2.0f)
                curveTo(9.9f, 2.0f, 9.0f, 2.9f, 9.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 7.0f, 2.0f, 7.9f, 2.0f, 9.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(9.0f)
                curveTo(22.0f, 7.9f, 21.1f, 7.0f, 20.0f, 7.0f)
                close()
                moveTo(9.0f, 12.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(9.83f, 15.0f, 9.0f, 15.0f)
                reflectiveCurveToRelative(-1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(8.17f, 12.0f, 9.0f, 12.0f)
                close()
                moveTo(12.0f, 18.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-0.75f)
                curveToRelative(0.0f, -1.0f, 2.0f, -1.5f, 3.0f, -1.5f)
                reflectiveCurveToRelative(3.0f, 0.5f, 3.0f, 1.5f)
                verticalLineTo(18.0f)
                close()
                moveTo(13.0f, 9.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(9.0f)
                close()
                moveTo(18.0f, 16.5f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(15.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(16.5f)
                close()
                moveTo(18.0f, 13.5f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(12.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(13.5f)
                close()
            }
        }
        return _badge!!
    }

private var _badge: ImageVector? = null
