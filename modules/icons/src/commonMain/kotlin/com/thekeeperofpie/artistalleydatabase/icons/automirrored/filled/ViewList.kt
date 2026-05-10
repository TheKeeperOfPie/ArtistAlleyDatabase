@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.AutoMirrored.Filled.ViewList: ImageVector
    get() {
        if (_viewList != null) {
            return _viewList!!
        }
        _viewList = materialIcon(name = "AutoMirrored.Filled.ViewList", autoMirror = true) {
            materialPath {
                moveTo(3.0f, 14.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(3.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(3.0f, 19.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(3.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(3.0f, 9.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(3.0f)
                verticalLineTo(9.0f)
                close()
                moveTo(8.0f, 14.0f)
                horizontalLineToRelative(13.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(8.0f, 19.0f)
                horizontalLineToRelative(13.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(8.0f, 5.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(13.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(8.0f)
                close()
            }
        }
        return _viewList!!
    }

private var _viewList: ImageVector? = null
