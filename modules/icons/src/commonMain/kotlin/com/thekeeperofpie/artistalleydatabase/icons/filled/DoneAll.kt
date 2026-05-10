@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.DoneAll: ImageVector
    get() {
        if (_doneAll != null) {
            return _doneAll!!
        }
        _doneAll = materialIcon(name = "Filled.DoneAll") {
            materialPath {
                moveTo(18.0f, 7.0f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(-6.34f, 6.34f)
                lineToRelative(1.41f, 1.41f)
                lineTo(18.0f, 7.0f)
                close()
                moveTo(22.24f, 5.59f)
                lineTo(11.66f, 16.17f)
                lineTo(7.48f, 12.0f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(11.66f, 19.0f)
                lineToRelative(12.0f, -12.0f)
                lineToRelative(-1.42f, -1.41f)
                close()
                moveTo(0.41f, 13.41f)
                lineTo(6.0f, 19.0f)
                lineToRelative(1.41f, -1.41f)
                lineTo(1.83f, 12.0f)
                lineTo(0.41f, 13.41f)
                close()
            }
        }
        return _doneAll!!
    }

private var _doneAll: ImageVector? = null
