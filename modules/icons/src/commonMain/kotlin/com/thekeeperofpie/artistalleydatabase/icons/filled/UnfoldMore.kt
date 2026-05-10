@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.UnfoldMore: ImageVector
    get() {
        if (_unfoldMore != null) {
            return _unfoldMore!!
        }
        _unfoldMore = materialIcon(name = "Filled.UnfoldMore") {
            materialPath {
                moveTo(12.0f, 5.83f)
                lineTo(15.17f, 9.0f)
                lineToRelative(1.41f, -1.41f)
                lineTo(12.0f, 3.0f)
                lineTo(7.41f, 7.59f)
                lineTo(8.83f, 9.0f)
                lineTo(12.0f, 5.83f)
                close()
                moveTo(12.0f, 18.17f)
                lineTo(8.83f, 15.0f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(12.0f, 21.0f)
                lineToRelative(4.59f, -4.59f)
                lineTo(15.17f, 15.0f)
                lineTo(12.0f, 18.17f)
                close()
            }
        }
        return _unfoldMore!!
    }

private var _unfoldMore: ImageVector? = null
