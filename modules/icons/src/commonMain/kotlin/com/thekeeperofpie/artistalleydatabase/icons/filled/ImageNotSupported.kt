@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ImageNotSupported: ImageVector
    get() {
        if (_imageNotSupported != null) {
            return _imageNotSupported!!
        }
        _imageNotSupported = materialIcon(name = "Filled.ImageNotSupported") {
            materialPath {
                moveTo(21.9f, 21.9f)
                lineToRelative(-8.49f, -8.49f)
                lineToRelative(0.0f, 0.0f)
                lineTo(3.59f, 3.59f)
                lineToRelative(0.0f, 0.0f)
                lineTo(2.1f, 2.1f)
                lineTo(0.69f, 3.51f)
                lineTo(3.0f, 5.83f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(13.17f)
                lineToRelative(2.31f, 2.31f)
                lineTo(21.9f, 21.9f)
                close()
                moveTo(5.0f, 18.0f)
                lineToRelative(3.5f, -4.5f)
                lineToRelative(2.5f, 3.01f)
                lineTo(12.17f, 15.0f)
                lineToRelative(3.0f, 3.0f)
                horizontalLineTo(5.0f)
                close()
                moveTo(21.0f, 18.17f)
                lineTo(5.83f, 3.0f)
                horizontalLineTo(19.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineTo(18.17f)
                close()
            }
        }
        return _imageNotSupported!!
    }

private var _imageNotSupported: ImageVector? = null
