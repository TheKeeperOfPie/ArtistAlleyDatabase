@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.SwapVert: ImageVector
    get() {
        if (_swapVert != null) {
            return _swapVert!!
        }
        _swapVert = materialIcon(name = "Filled.SwapVert") {
            materialPath {
                moveTo(16.0f, 17.01f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(7.01f)
                horizontalLineToRelative(-3.0f)
                lineTo(15.0f, 21.0f)
                lineToRelative(4.0f, -3.99f)
                horizontalLineToRelative(-3.0f)
                close()
                moveTo(9.0f, 3.0f)
                lineTo(5.0f, 6.99f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(14.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(6.99f)
                horizontalLineToRelative(3.0f)
                lineTo(9.0f, 3.0f)
                close()
            }
        }
        return _swapVert!!
    }

private var _swapVert: ImageVector? = null
