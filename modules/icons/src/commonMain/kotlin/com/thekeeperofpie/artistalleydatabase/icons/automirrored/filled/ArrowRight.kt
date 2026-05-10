@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.AutoMirrored.Filled.ArrowRight: ImageVector
    get() {
        if (_arrowRight != null) {
            return _arrowRight!!
        }
        _arrowRight = materialIcon(name = "AutoMirrored.Filled.ArrowRight", autoMirror = true) {
            materialPath {
                moveTo(10.0f, 17.0f)
                lineToRelative(5.0f, -5.0f)
                lineToRelative(-5.0f, -5.0f)
                verticalLineToRelative(10.0f)
                close()
            }
        }
        return _arrowRight!!
    }

private var _arrowRight: ImageVector? = null
