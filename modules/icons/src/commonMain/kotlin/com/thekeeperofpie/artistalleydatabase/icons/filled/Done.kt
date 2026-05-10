@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Done: ImageVector
    get() {
        if (_done != null) {
            return _done!!
        }
        _done = materialIcon(name = "Filled.Done") {
            materialPath {
                moveTo(9.0f, 16.2f)
                lineTo(4.8f, 12.0f)
                lineToRelative(-1.4f, 1.4f)
                lineTo(9.0f, 19.0f)
                lineTo(21.0f, 7.0f)
                lineToRelative(-1.4f, -1.4f)
                lineTo(9.0f, 16.2f)
                close()
            }
        }
        return _done!!
    }

private var _done: ImageVector? = null
