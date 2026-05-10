@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.outlined

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Outlined.WrongLocation: ImageVector
    get() {
        if (_wrongLocation != null) {
            return _wrongLocation!!
        }
        _wrongLocation = materialIcon(name = "Outlined.WrongLocation") {
            materialPath {
                moveTo(18.0f, 11.0f)
                curveToRelative(0.0f, 0.07f, 0.0f, 0.13f, 0.0f, 0.2f)
                curveToRelative(0.0f, 2.34f, -1.95f, 5.44f, -6.0f, 9.14f)
                curveToRelative(-4.05f, -3.7f, -6.0f, -6.79f, -6.0f, -9.14f)
                curveTo(6.0f, 7.57f, 8.65f, 5.0f, 12.0f, 5.0f)
                curveToRelative(0.34f, 0.0f, 0.68f, 0.03f, 1.0f, 0.08f)
                verticalLineTo(3.06f)
                curveTo(12.67f, 3.02f, 12.34f, 3.0f, 12.0f, 3.0f)
                curveToRelative(-4.2f, 0.0f, -8.0f, 3.22f, -8.0f, 8.2f)
                curveToRelative(0.0f, 3.32f, 2.67f, 7.25f, 8.0f, 11.8f)
                curveToRelative(5.33f, -4.55f, 8.0f, -8.48f, 8.0f, -11.8f)
                curveToRelative(0.0f, -0.07f, 0.0f, -0.13f, 0.0f, -0.2f)
                horizontalLineTo(18.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 11.0f)
                moveToRelative(-2.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, 4.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, -4.0f, 0.0f)
            }
            materialPath {
                moveTo(22.54f, 2.88f)
                lineToRelative(-1.42f, -1.42f)
                lineToRelative(-2.12f, 2.13f)
                lineToRelative(-2.12f, -2.13f)
                lineToRelative(-1.42f, 1.42f)
                lineToRelative(2.13f, 2.12f)
                lineToRelative(-2.13f, 2.12f)
                lineToRelative(1.42f, 1.42f)
                lineToRelative(2.12f, -2.13f)
                lineToRelative(2.12f, 2.13f)
                lineToRelative(1.42f, -1.42f)
                lineToRelative(-2.13f, -2.12f)
                close()
            }
        }
        return _wrongLocation!!
    }

private var _wrongLocation: ImageVector? = null
