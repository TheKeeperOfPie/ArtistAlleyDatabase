@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.PeopleAlt: ImageVector
    get() {
        if (_peopleAlt != null) {
            return _peopleAlt!!
        }
        _peopleAlt = materialIcon(name = "Filled.PeopleAlt") {
            materialPath(pathFillType = PathFillType.EvenOdd) {
                moveTo(16.67f, 13.13f)
                curveTo(18.04f, 14.06f, 19.0f, 15.32f, 19.0f, 17.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-3.0f)
                curveTo(23.0f, 14.82f, 19.43f, 13.53f, 16.67f, 13.13f)
                close()
            }
            materialPath(pathFillType = PathFillType.EvenOdd) {
                moveTo(9.0f, 8.0f)
                moveToRelative(-4.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, 8.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, -8.0f, 0.0f)
            }
            materialPath(pathFillType = PathFillType.EvenOdd) {
                moveTo(15.0f, 12.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                curveToRelative(-0.47f, 0.0f, -0.91f, 0.1f, -1.33f, 0.24f)
                curveTo(14.5f, 5.27f, 15.0f, 6.58f, 15.0f, 8.0f)
                reflectiveCurveToRelative(-0.5f, 2.73f, -1.33f, 3.76f)
                curveTo(14.09f, 11.9f, 14.53f, 12.0f, 15.0f, 12.0f)
                close()
            }
            materialPath(pathFillType = PathFillType.EvenOdd) {
                moveTo(9.0f, 13.0f)
                curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(-3.0f)
                curveTo(17.0f, 14.34f, 11.67f, 13.0f, 9.0f, 13.0f)
                close()
            }
        }
        return _peopleAlt!!
    }

private var _peopleAlt: ImageVector? = null
