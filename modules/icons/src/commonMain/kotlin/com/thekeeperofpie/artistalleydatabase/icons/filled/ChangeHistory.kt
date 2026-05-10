@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ChangeHistory: ImageVector
    get() {
        if (_changeHistory != null) {
            return _changeHistory!!
        }
        _changeHistory = materialIcon(name = "Filled.ChangeHistory") {
            materialPath {
                moveTo(12.0f, 7.77f)
                lineTo(18.39f, 18.0f)
                horizontalLineTo(5.61f)
                lineTo(12.0f, 7.77f)
                moveTo(12.0f, 4.0f)
                lineTo(2.0f, 20.0f)
                horizontalLineToRelative(20.0f)
                lineTo(12.0f, 4.0f)
                close()
            }
        }
        return _changeHistory!!
    }

private var _changeHistory: ImageVector? = null
