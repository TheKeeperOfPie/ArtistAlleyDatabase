@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.TableChart: ImageVector
    get() {
        if (_tableChart != null) {
            return _tableChart!!
        }
        _tableChart = materialIcon(name = "Filled.TableChart") {
            materialPath {
                moveTo(10.0f, 10.02f)
                horizontalLineToRelative(5.0f)
                lineTo(15.0f, 21.0f)
                horizontalLineToRelative(-5.0f)
                close()
                moveTo(17.0f, 21.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-9.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(11.0f)
                close()
                moveTo(20.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(19.0f)
                lineTo(22.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                lineTo(8.0f, 10.0f)
                lineTo(3.0f, 10.0f)
                verticalLineToRelative(9.0f)
                close()
            }
        }
        return _tableChart!!
    }

private var _tableChart: ImageVector? = null
