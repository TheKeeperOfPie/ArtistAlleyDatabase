@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.AutoMirrored.Filled.LastPage: ImageVector
    get() {
        if (_lastPage != null) {
            return _lastPage!!
        }
        _lastPage = materialIcon(name = "AutoMirrored.Filled.LastPage", autoMirror = true) {
            materialPath {
                moveTo(5.59f, 7.41f)
                lineTo(10.18f, 12.0f)
                lineToRelative(-4.59f, 4.59f)
                lineTo(7.0f, 18.0f)
                lineToRelative(6.0f, -6.0f)
                lineToRelative(-6.0f, -6.0f)
                close()
                moveTo(16.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(12.0f)
                horizontalLineToRelative(-2.0f)
                close()
            }
        }
        return _lastPage!!
    }

private var _lastPage: ImageVector? = null
