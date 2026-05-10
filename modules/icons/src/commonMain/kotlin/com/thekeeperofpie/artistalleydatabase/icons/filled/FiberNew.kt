@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.FiberNew: ImageVector
    get() {
        if (_fiberNew != null) {
            return _fiberNew!!
        }
        _fiberNew = materialIcon(name = "Filled.FiberNew") {
            materialPath {
                moveTo(20.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveTo(2.89f, 4.0f, 2.01f, 4.89f, 2.01f, 6.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveTo(22.0f, 4.89f, 21.11f, 4.0f, 20.0f, 4.0f)
                close()
                moveTo(8.5f, 15.0f)
                horizontalLineTo(7.3f)
                lineToRelative(-2.55f, -3.5f)
                verticalLineTo(15.0f)
                horizontalLineTo(3.5f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(1.25f)
                lineToRelative(2.5f, 3.5f)
                verticalLineTo(9.0f)
                horizontalLineTo(8.5f)
                verticalLineTo(15.0f)
                close()
                moveTo(13.5f, 10.26f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(1.12f)
                horizontalLineToRelative(2.5f)
                verticalLineToRelative(1.26f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(1.11f)
                horizontalLineToRelative(2.5f)
                verticalLineTo(15.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(10.26f)
                close()
                moveTo(20.5f, 14.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(1.25f)
                verticalLineToRelative(4.51f)
                horizontalLineToRelative(1.13f)
                verticalLineTo(9.99f)
                horizontalLineToRelative(1.25f)
                verticalLineToRelative(3.51f)
                horizontalLineToRelative(1.12f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(1.25f)
                verticalLineTo(14.0f)
                close()
            }
        }
        return _fiberNew!!
    }

private var _fiberNew: ImageVector? = null
