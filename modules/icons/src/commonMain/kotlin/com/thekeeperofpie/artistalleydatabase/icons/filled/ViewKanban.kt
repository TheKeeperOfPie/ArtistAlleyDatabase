@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ViewKanban: ImageVector
    get() {
        if (_viewKanban != null) {
            return _viewKanban!!
        }
        _viewKanban = materialIcon(name = "Filled.ViewKanban") {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(21.0f, 3.9f, 20.1f, 3.0f, 19.0f, 3.0f)
                close()
                moveTo(9.0f, 17.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(13.0f, 12.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(12.0f)
                close()
                moveTo(17.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
            }
        }
        return _viewKanban!!
    }

private var _viewKanban: ImageVector? = null
