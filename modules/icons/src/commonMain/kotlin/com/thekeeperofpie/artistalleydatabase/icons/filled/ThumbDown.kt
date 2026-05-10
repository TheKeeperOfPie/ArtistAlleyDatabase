@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ThumbDown: ImageVector
    get() {
        if (_thumbDown != null) {
            return _thumbDown!!
        }
        _thumbDown = materialIcon(name = "Filled.ThumbDown") {
            materialPath {
                moveTo(15.0f, 3.0f)
                lineTo(6.0f, 3.0f)
                curveToRelative(-0.83f, 0.0f, -1.54f, 0.5f, -1.84f, 1.22f)
                lineToRelative(-3.02f, 7.05f)
                curveToRelative(-0.09f, 0.23f, -0.14f, 0.47f, -0.14f, 0.73f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.31f)
                lineToRelative(-0.95f, 4.57f)
                lineToRelative(-0.03f, 0.32f)
                curveToRelative(0.0f, 0.41f, 0.17f, 0.79f, 0.44f, 1.06f)
                lineTo(9.83f, 23.0f)
                lineToRelative(6.59f, -6.59f)
                curveToRelative(0.36f, -0.36f, 0.58f, -0.86f, 0.58f, -1.41f)
                lineTo(17.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 3.0f)
                verticalLineToRelative(12.0f)
                horizontalLineToRelative(4.0f)
                lineTo(23.0f, 3.0f)
                horizontalLineToRelative(-4.0f)
                close()
            }
        }
        return _thumbDown!!
    }

private var _thumbDown: ImageVector? = null
