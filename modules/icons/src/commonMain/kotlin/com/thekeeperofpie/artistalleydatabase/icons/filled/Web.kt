@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Web: ImageVector
    get() {
        if (_web != null) {
            return _web!!
        }
        _web = materialIcon(name = "Filled.Web") {
            materialPath {
                moveTo(20.0f, 4.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(15.0f, 18.0f)
                lineTo(4.0f, 18.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(11.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(15.0f, 13.0f)
                lineTo(4.0f, 13.0f)
                lineTo(4.0f, 9.0f)
                horizontalLineToRelative(11.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(20.0f, 18.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(16.0f, 9.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(9.0f)
                close()
            }
        }
        return _web!!
    }

private var _web: ImageVector? = null
