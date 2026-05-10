@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.AutoMirrored.Filled.Reply: ImageVector
    get() {
        if (_reply != null) {
            return _reply!!
        }
        _reply = materialIcon(name = "AutoMirrored.Filled.Reply", autoMirror = true) {
            materialPath {
                moveTo(10.0f, 9.0f)
                verticalLineTo(5.0f)
                lineToRelative(-7.0f, 7.0f)
                lineToRelative(7.0f, 7.0f)
                verticalLineToRelative(-4.1f)
                curveToRelative(5.0f, 0.0f, 8.5f, 1.6f, 11.0f, 5.1f)
                curveToRelative(-1.0f, -5.0f, -4.0f, -10.0f, -11.0f, -11.0f)
                close()
            }
        }
        return _reply!!
    }

private var _reply: ImageVector? = null
