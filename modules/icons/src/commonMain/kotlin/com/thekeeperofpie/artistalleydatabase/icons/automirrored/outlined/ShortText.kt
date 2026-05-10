@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.automirrored.outlined

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.AutoMirrored.Outlined.ShortText: ImageVector
    get() {
        if (_shortText != null) {
            return _shortText!!
        }
        _shortText = materialIcon(name = "AutoMirrored.Outlined.ShortText", autoMirror = true) {
            materialPath {
                moveTo(4.0f, 9.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 11.0f)
                lineTo(4.0f, 9.0f)
                close()
                moveTo(4.0f, 13.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                close()
            }
        }
        return _shortText!!
    }

private var _shortText: ImageVector? = null
