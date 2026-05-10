@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.AcUnit: ImageVector
    get() {
        if (_acUnit != null) {
            return _acUnit!!
        }
        _acUnit = materialIcon(name = "Filled.AcUnit") {
            materialPath {
                moveTo(22.0f, 11.0f)
                horizontalLineToRelative(-4.17f)
                lineToRelative(3.24f, -3.24f)
                lineToRelative(-1.41f, -1.42f)
                lineTo(15.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(9.0f)
                lineToRelative(4.66f, -4.66f)
                lineToRelative(-1.42f, -1.41f)
                lineTo(13.0f, 6.17f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(4.17f)
                lineTo(7.76f, 2.93f)
                lineTo(6.34f, 4.34f)
                lineTo(11.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(9.0f)
                lineTo(4.34f, 6.34f)
                lineTo(2.93f, 7.76f)
                lineTo(6.17f, 11.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.17f)
                lineToRelative(-3.24f, 3.24f)
                lineToRelative(1.41f, 1.42f)
                lineTo(9.0f, 13.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(-4.66f, 4.66f)
                lineToRelative(1.42f, 1.41f)
                lineTo(11.0f, 17.83f)
                verticalLineTo(22.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-4.17f)
                lineToRelative(3.24f, 3.24f)
                lineToRelative(1.42f, -1.41f)
                lineTo(13.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(4.66f, 4.66f)
                lineToRelative(1.41f, -1.42f)
                lineTo(17.83f, 13.0f)
                horizontalLineTo(22.0f)
                close()
            }
        }
        return _acUnit!!
    }

private var _acUnit: ImageVector? = null
