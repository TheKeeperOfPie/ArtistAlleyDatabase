@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.FirstPage: ImageVector
    get() {
        if (_firstPage != null) {
            return _firstPage!!
        }
        _firstPage = materialIcon(name = "Filled.FirstPage") {
            materialPath {
                moveTo(18.41f, 16.59f)
                lineTo(13.82f, 12.0f)
                lineToRelative(4.59f, -4.59f)
                lineTo(17.0f, 6.0f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(6.0f, 6.0f)
                close()
                moveTo(6.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(12.0f)
                horizontalLineTo(6.0f)
                close()
            }
        }
        return _firstPage!!
    }

private var _firstPage: ImageVector? = null
