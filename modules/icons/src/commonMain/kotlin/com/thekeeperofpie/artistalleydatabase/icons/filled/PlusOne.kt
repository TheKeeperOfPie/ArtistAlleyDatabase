@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.PlusOne: ImageVector
    get() {
        if (_plusOne != null) {
            return _plusOne!!
        }
        _plusOne = materialIcon(name = "Filled.PlusOne") {
            materialPath {
                moveTo(10.0f, 8.0f)
                lineTo(8.0f, 8.0f)
                verticalLineToRelative(4.0f)
                lineTo(4.0f, 12.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-4.0f)
                close()
                moveTo(14.5f, 6.08f)
                lineTo(14.5f, 7.9f)
                lineToRelative(2.5f, -0.5f)
                lineTo(17.0f, 18.0f)
                horizontalLineToRelative(2.0f)
                lineTo(19.0f, 5.0f)
                close()
            }
        }
        return _plusOne!!
    }

private var _plusOne: ImageVector? = null
