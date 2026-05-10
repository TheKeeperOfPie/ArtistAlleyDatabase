@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Games: ImageVector
    get() {
        if (_games != null) {
            return _games!!
        }
        _games = materialIcon(name = "Filled.Games") {
            materialPath {
                moveTo(15.0f, 7.5f)
                verticalLineTo(2.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(5.5f)
                lineToRelative(3.0f, 3.0f)
                lineToRelative(3.0f, -3.0f)
                close()
                moveTo(7.5f, 9.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(5.5f)
                lineToRelative(3.0f, -3.0f)
                lineToRelative(-3.0f, -3.0f)
                close()
                moveTo(9.0f, 16.5f)
                verticalLineTo(22.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-5.5f)
                lineToRelative(-3.0f, -3.0f)
                lineToRelative(-3.0f, 3.0f)
                close()
                moveTo(16.5f, 9.0f)
                lineToRelative(-3.0f, 3.0f)
                lineToRelative(3.0f, 3.0f)
                horizontalLineTo(22.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(-5.5f)
                close()
            }
        }
        return _games!!
    }

private var _games: ImageVector? = null
