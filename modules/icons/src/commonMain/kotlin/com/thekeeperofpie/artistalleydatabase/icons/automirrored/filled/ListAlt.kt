@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.AutoMirrored.Filled.ListAlt: ImageVector
    get() {
        if (_listAlt != null) {
            return _listAlt!!
        }
        _listAlt = materialIcon(name = "AutoMirrored.Filled.ListAlt", autoMirror = true) {
            materialPath {
                moveTo(19.0f, 5.0f)
                verticalLineToRelative(14.0f)
                lineTo(5.0f, 19.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(14.0f)
                moveToRelative(1.1f, -2.0f)
                lineTo(3.9f, 3.0f)
                curveToRelative(-0.5f, 0.0f, -0.9f, 0.4f, -0.9f, 0.9f)
                verticalLineToRelative(16.2f)
                curveToRelative(0.0f, 0.4f, 0.4f, 0.9f, 0.9f, 0.9f)
                horizontalLineToRelative(16.2f)
                curveToRelative(0.4f, 0.0f, 0.9f, -0.5f, 0.9f, -0.9f)
                lineTo(21.0f, 3.9f)
                curveToRelative(0.0f, -0.5f, -0.5f, -0.9f, -0.9f, -0.9f)
                close()
                moveTo(11.0f, 7.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-6.0f)
                lineTo(11.0f, 7.0f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-6.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(11.0f, 15.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-6.0f)
                close()
                moveTo(7.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.0f, 9.0f)
                close()
                moveTo(7.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.0f, 13.0f)
                close()
                moveTo(7.0f, 15.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.0f, 17.0f)
                close()
            }
        }
        return _listAlt!!
    }

private var _listAlt: ImageVector? = null
