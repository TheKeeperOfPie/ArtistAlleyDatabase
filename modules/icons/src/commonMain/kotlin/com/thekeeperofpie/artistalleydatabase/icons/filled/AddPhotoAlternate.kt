@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.AddPhotoAlternate: ImageVector
    get() {
        if (_addPhotoAlternate != null) {
            return _addPhotoAlternate!!
        }
        _addPhotoAlternate = materialIcon(name = "Filled.AddPhotoAlternate") {
            materialPath {
                moveTo(19.0f, 7.0f)
                verticalLineToRelative(2.99f)
                reflectiveCurveToRelative(-1.99f, 0.01f, -2.0f, 0.0f)
                lineTo(17.0f, 7.0f)
                horizontalLineToRelative(-3.0f)
                reflectiveCurveToRelative(0.01f, -1.99f, 0.0f, -2.0f)
                horizontalLineToRelative(3.0f)
                lineTo(17.0f, 2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-3.0f)
                close()
                moveTo(16.0f, 11.0f)
                lineTo(16.0f, 8.0f)
                horizontalLineToRelative(-3.0f)
                lineTo(13.0f, 5.0f)
                lineTo(5.0f, 5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(-3.0f)
                close()
                moveTo(5.0f, 19.0f)
                lineToRelative(3.0f, -4.0f)
                lineToRelative(2.0f, 3.0f)
                lineToRelative(3.0f, -4.0f)
                lineToRelative(4.0f, 5.0f)
                lineTo(5.0f, 19.0f)
                close()
            }
        }
        return _addPhotoAlternate!!
    }

private var _addPhotoAlternate: ImageVector? = null
