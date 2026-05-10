@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.outlined

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Outlined.MusicNote: ImageVector
    get() {
        if (_musicNote != null) {
            return _musicNote!!
        }
        _musicNote = materialIcon(name = "Outlined.MusicNote") {
            materialPath {
                moveTo(12.0f, 3.0f)
                lineToRelative(0.01f, 10.55f)
                curveToRelative(-0.59f, -0.34f, -1.27f, -0.55f, -2.0f, -0.55f)
                curveTo(7.79f, 13.0f, 6.0f, 14.79f, 6.0f, 17.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.01f, 4.0f)
                reflectiveCurveTo(14.0f, 19.21f, 14.0f, 17.0f)
                lineTo(14.0f, 7.0f)
                horizontalLineToRelative(4.0f)
                lineTo(18.0f, 3.0f)
                horizontalLineToRelative(-6.0f)
                close()
                moveTo(10.01f, 19.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _musicNote!!
    }

private var _musicNote: ImageVector? = null
