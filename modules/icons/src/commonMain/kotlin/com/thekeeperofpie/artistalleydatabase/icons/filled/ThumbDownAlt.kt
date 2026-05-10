@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ThumbDownAlt: ImageVector
    get() {
        if (_thumbDownAlt != null) {
            return _thumbDownAlt!!
        }
        _thumbDownAlt = materialIcon(name = "Filled.ThumbDownAlt") {
            materialPath {
                moveTo(22.0f, 4.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(9.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(4.0f)
                close()
                moveTo(2.17f, 11.12f)
                curveToRelative(-0.11f, 0.25f, -0.17f, 0.52f, -0.17f, 0.8f)
                verticalLineTo(13.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(5.5f)
                lineToRelative(-0.92f, 4.65f)
                curveToRelative(-0.05f, 0.22f, -0.02f, 0.46f, 0.08f, 0.66f)
                curveToRelative(0.23f, 0.45f, 0.52f, 0.86f, 0.88f, 1.22f)
                lineTo(10.0f, 22.0f)
                lineToRelative(6.41f, -6.41f)
                curveToRelative(0.38f, -0.38f, 0.59f, -0.89f, 0.59f, -1.42f)
                verticalLineTo(6.34f)
                curveTo(17.0f, 5.05f, 15.95f, 4.0f, 14.66f, 4.0f)
                horizontalLineToRelative(-8.1f)
                curveToRelative(-0.71f, 0.0f, -1.36f, 0.37f, -1.72f, 0.97f)
                lineToRelative(-2.67f, 6.15f)
                close()
            }
        }
        return _thumbDownAlt!!
    }

private var _thumbDownAlt: ImageVector? = null
