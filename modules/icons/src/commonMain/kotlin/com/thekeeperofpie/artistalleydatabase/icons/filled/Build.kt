@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Build: ImageVector
    get() {
        if (_build != null) {
            return _build!!
        }
        _build = materialIcon(name = "Filled.Build") {
            materialPath {
                moveTo(22.7f, 19.0f)
                lineToRelative(-9.1f, -9.1f)
                curveToRelative(0.9f, -2.3f, 0.4f, -5.0f, -1.5f, -6.9f)
                curveToRelative(-2.0f, -2.0f, -5.0f, -2.4f, -7.4f, -1.3f)
                lineTo(9.0f, 6.0f)
                lineTo(6.0f, 9.0f)
                lineTo(1.6f, 4.7f)
                curveTo(0.4f, 7.1f, 0.9f, 10.1f, 2.9f, 12.1f)
                curveToRelative(1.9f, 1.9f, 4.6f, 2.4f, 6.9f, 1.5f)
                lineToRelative(9.1f, 9.1f)
                curveToRelative(0.4f, 0.4f, 1.0f, 0.4f, 1.4f, 0.0f)
                lineToRelative(2.3f, -2.3f)
                curveToRelative(0.5f, -0.4f, 0.5f, -1.1f, 0.1f, -1.4f)
                close()
            }
        }
        return _build!!
    }

private var _build: ImageVector? = null
