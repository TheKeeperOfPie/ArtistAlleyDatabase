@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Store: ImageVector
    get() {
        if (_store != null) {
            return _store!!
        }
        _store = materialIcon(name = "Filled.Store") {
            materialPath {
                moveTo(20.0f, 4.0f)
                lineTo(4.0f, 4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(16.0f)
                lineTo(20.0f, 4.0f)
                close()
                moveTo(21.0f, 14.0f)
                verticalLineToRelative(-2.0f)
                lineToRelative(-1.0f, -5.0f)
                lineTo(4.0f, 7.0f)
                lineToRelative(-1.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(1.0f)
                close()
                moveTo(12.0f, 18.0f)
                lineTo(6.0f, 18.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        return _store!!
    }

private var _store: ImageVector? = null
