@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ArrowDropUp: ImageVector
    get() {
        if (_arrowDropUp != null) {
            return _arrowDropUp!!
        }
        _arrowDropUp = materialIcon(name = "Filled.ArrowDropUp") {
            materialPath {
                moveTo(7.0f, 14.0f)
                lineToRelative(5.0f, -5.0f)
                lineToRelative(5.0f, 5.0f)
                close()
            }
        }
        return _arrowDropUp!!
    }

private var _arrowDropUp: ImageVector? = null
