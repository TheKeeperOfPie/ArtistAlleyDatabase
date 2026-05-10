@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.Verified: ImageVector
    get() {
        if (_verified != null) {
            return _verified!!
        }
        _verified = materialIcon(name = "Filled.Verified") {
            materialPath {
                moveTo(23.0f, 12.0f)
                lineToRelative(-2.44f, -2.79f)
                lineToRelative(0.34f, -3.69f)
                lineToRelative(-3.61f, -0.82f)
                lineTo(15.4f, 1.5f)
                lineTo(12.0f, 2.96f)
                lineTo(8.6f, 1.5f)
                lineTo(6.71f, 4.69f)
                lineTo(3.1f, 5.5f)
                lineTo(3.44f, 9.2f)
                lineTo(1.0f, 12.0f)
                lineToRelative(2.44f, 2.79f)
                lineToRelative(-0.34f, 3.7f)
                lineToRelative(3.61f, 0.82f)
                lineTo(8.6f, 22.5f)
                lineToRelative(3.4f, -1.47f)
                lineToRelative(3.4f, 1.46f)
                lineToRelative(1.89f, -3.19f)
                lineToRelative(3.61f, -0.82f)
                lineToRelative(-0.34f, -3.69f)
                lineTo(23.0f, 12.0f)
                close()
                moveTo(10.09f, 16.72f)
                lineToRelative(-3.8f, -3.81f)
                lineToRelative(1.48f, -1.48f)
                lineToRelative(2.32f, 2.33f)
                lineToRelative(5.85f, -5.87f)
                lineToRelative(1.48f, 1.48f)
                lineTo(10.09f, 16.72f)
                close()
            }
        }
        return _verified!!
    }

private var _verified: ImageVector? = null
