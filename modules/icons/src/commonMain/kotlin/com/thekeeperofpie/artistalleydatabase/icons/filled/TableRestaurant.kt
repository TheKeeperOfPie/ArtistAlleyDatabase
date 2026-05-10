@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.TableRestaurant: ImageVector
    get() {
        if (_tableRestaurant != null) {
            return _tableRestaurant!!
        }
        _tableRestaurant = materialIcon(name = "Filled.TableRestaurant") {
            materialPath {
                moveTo(21.96f, 9.73f)
                lineToRelative(-1.43f, -5.0f)
                curveTo(20.41f, 4.3f, 20.02f, 4.0f, 19.57f, 4.0f)
                horizontalLineTo(4.43f)
                curveTo(3.98f, 4.0f, 3.59f, 4.3f, 3.47f, 4.73f)
                lineToRelative(-1.43f, 5.0f)
                curveTo(1.86f, 10.36f, 2.34f, 11.0f, 3.0f, 11.0f)
                horizontalLineToRelative(2.2f)
                lineTo(4.0f, 20.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(0.67f, -5.0f)
                horizontalLineToRelative(10.67f)
                lineTo(18.0f, 20.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(-1.2f, -9.0f)
                horizontalLineTo(21.0f)
                curveTo(21.66f, 11.0f, 22.14f, 10.36f, 21.96f, 9.73f)
                close()
                moveTo(6.93f, 13.0f)
                lineToRelative(0.27f, -2.0f)
                horizontalLineToRelative(9.6f)
                lineToRelative(0.27f, 2.0f)
                horizontalLineTo(6.93f)
                close()
            }
        }
        return _tableRestaurant!!
    }

private var _tableRestaurant: ImageVector? = null
