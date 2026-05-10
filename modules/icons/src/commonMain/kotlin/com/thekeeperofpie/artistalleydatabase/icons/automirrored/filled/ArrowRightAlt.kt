@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.AutoMirrored.Filled.ArrowRightAlt: ImageVector
    get() {
        if (_arrowRightAlt != null) {
            return _arrowRightAlt!!
        }
        _arrowRightAlt = materialIcon(
            name = "AutoMirrored.Filled.ArrowRightAlt", autoMirror =
                true
        ) {
            materialPath {
                moveTo(16.01f, 11.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(12.01f)
                verticalLineToRelative(3.0f)
                lineTo(20.0f, 12.0f)
                lineToRelative(-3.99f, -4.0f)
                close()
            }
        }
        return _arrowRightAlt!!
    }

private var _arrowRightAlt: ImageVector? = null
