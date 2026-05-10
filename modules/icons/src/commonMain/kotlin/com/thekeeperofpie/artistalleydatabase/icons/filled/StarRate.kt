@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.StarRate: ImageVector
    get() {
        if (_starRate != null) {
            return _starRate!!
        }
        _starRate = materialIcon(name = "Filled.StarRate") {
            materialPath {
                moveTo(14.43f, 10.0f)
                lineToRelative(-2.43f, -8.0f)
                lineToRelative(-2.43f, 8.0f)
                lineToRelative(-7.57f, 0.0f)
                lineToRelative(6.18f, 4.41f)
                lineToRelative(-2.35f, 7.59f)
                lineToRelative(6.17f, -4.69f)
                lineToRelative(6.18f, 4.69f)
                lineToRelative(-2.35f, -7.59f)
                lineToRelative(6.17f, -4.41f)
                close()
            }
        }
        return _starRate!!
    }

private var _starRate: ImageVector? = null
