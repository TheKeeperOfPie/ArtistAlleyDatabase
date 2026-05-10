@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Check: ImageVector
    get() {
        if (_check != null) {
            return _check!!
        }
        _check = materialIcon(name = "Filled.Check") {
            materialPath {
                moveTo(9.0f, 16.17f)
                lineTo(4.83f, 12.0f)
                lineToRelative(-1.42f, 1.41f)
                lineTo(9.0f, 19.0f)
                lineTo(21.0f, 7.0f)
                lineToRelative(-1.41f, -1.41f)
                close()
            }
        }
        return _check!!
    }

private var _check: ImageVector? = null
