@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.UnfoldLess: ImageVector
    get() {
        if (_unfoldLess != null) {
            return _unfoldLess!!
        }
        _unfoldLess = materialIcon(name = "Filled.UnfoldLess") {
            materialPath {
                moveTo(7.41f, 18.59f)
                lineTo(8.83f, 20.0f)
                lineTo(12.0f, 16.83f)
                lineTo(15.17f, 20.0f)
                lineToRelative(1.41f, -1.41f)
                lineTo(12.0f, 14.0f)
                lineToRelative(-4.59f, 4.59f)
                close()
                moveTo(16.59f, 5.41f)
                lineTo(15.17f, 4.0f)
                lineTo(12.0f, 7.17f)
                lineTo(8.83f, 4.0f)
                lineTo(7.41f, 5.41f)
                lineTo(12.0f, 10.0f)
                lineToRelative(4.59f, -4.59f)
                close()
            }
        }
        return _unfoldLess!!
    }

private var _unfoldLess: ImageVector? = null
