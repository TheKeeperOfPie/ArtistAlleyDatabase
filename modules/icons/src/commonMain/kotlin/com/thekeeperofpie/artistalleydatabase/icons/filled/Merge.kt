@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Merge: ImageVector
    get() {
        if (_merge != null) {
            return _merge!!
        }
        _merge = materialIcon(name = "Filled.Merge") {
            materialPath {
                moveTo(6.41f, 21.0f)
                lineTo(5.0f, 19.59f)
                lineToRelative(4.83f, -4.83f)
                curveToRelative(0.75f, -0.75f, 1.17f, -1.77f, 1.17f, -2.83f)
                verticalLineToRelative(-5.1f)
                lineTo(9.41f, 8.41f)
                lineTo(8.0f, 7.0f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(4.0f, 4.0f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(13.0f, 6.83f)
                verticalLineToRelative(5.1f)
                curveToRelative(0.0f, 1.06f, 0.42f, 2.08f, 1.17f, 2.83f)
                lineTo(19.0f, 19.59f)
                lineTo(17.59f, 21.0f)
                lineTo(12.0f, 15.41f)
                lineTo(6.41f, 21.0f)
                close()
            }
        }
        return _merge!!
    }

private var _merge: ImageVector? = null
