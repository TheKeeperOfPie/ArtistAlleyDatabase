@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ThumbUpAlt: ImageVector
    get() {
        if (_thumbUpAlt != null) {
            return _thumbUpAlt!!
        }
        _thumbUpAlt = materialIcon(name = "Filled.ThumbUpAlt") {
            materialPath {
                moveTo(2.0f, 20.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-9.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(2.0f, 9.0f)
                verticalLineToRelative(11.0f)
                close()
                moveTo(21.83f, 12.88f)
                curveToRelative(0.11f, -0.25f, 0.17f, -0.52f, 0.17f, -0.8f)
                lineTo(22.0f, 11.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-5.5f)
                lineToRelative(0.92f, -4.65f)
                curveToRelative(0.05f, -0.22f, 0.02f, -0.46f, -0.08f, -0.66f)
                curveToRelative(-0.23f, -0.45f, -0.52f, -0.86f, -0.88f, -1.22f)
                lineTo(14.0f, 2.0f)
                lineTo(7.59f, 8.41f)
                curveTo(7.21f, 8.79f, 7.0f, 9.3f, 7.0f, 9.83f)
                verticalLineToRelative(7.84f)
                curveTo(7.0f, 18.95f, 8.05f, 20.0f, 9.34f, 20.0f)
                horizontalLineToRelative(8.11f)
                curveToRelative(0.7f, 0.0f, 1.36f, -0.37f, 1.72f, -0.97f)
                lineToRelative(2.66f, -6.15f)
                close()
            }
        }
        return _thumbUpAlt!!
    }

private var _thumbUpAlt: ImageVector? = null
