@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.outlined

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Outlined.PeopleAlt: ImageVector
    get() {
        if (_peopleAlt != null) {
            return _peopleAlt!!
        }
        _peopleAlt = materialIcon(name = "Outlined.PeopleAlt") {
            materialPath {
                moveTo(16.67f, 13.13f)
                curveTo(18.04f, 14.06f, 19.0f, 15.32f, 19.0f, 17.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-3.0f)
                curveTo(23.0f, 14.82f, 19.43f, 13.53f, 16.67f, 13.13f)
                close()
            }
            materialPath {
                moveTo(15.0f, 12.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                curveToRelative(-0.47f, 0.0f, -0.91f, 0.1f, -1.33f, 0.24f)
                curveTo(14.5f, 5.27f, 15.0f, 6.58f, 15.0f, 8.0f)
                reflectiveCurveToRelative(-0.5f, 2.73f, -1.33f, 3.76f)
                curveTo(14.09f, 11.9f, 14.53f, 12.0f, 15.0f, 12.0f)
                close()
            }
            materialPath {
                moveTo(9.0f, 12.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveTo(5.0f, 5.79f, 5.0f, 8.0f)
                curveTo(5.0f, 10.21f, 6.79f, 12.0f, 9.0f, 12.0f)
                close()
                moveTo(9.0f, 6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveTo(7.0f, 9.1f, 7.0f, 8.0f)
                curveTo(7.0f, 6.9f, 7.9f, 6.0f, 9.0f, 6.0f)
                close()
            }
            materialPath {
                moveTo(9.0f, 13.0f)
                curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(-3.0f)
                curveTo(17.0f, 14.34f, 11.67f, 13.0f, 9.0f, 13.0f)
                close()
                moveTo(15.0f, 18.0f)
                horizontalLineTo(3.0f)
                lineToRelative(0.0f, -0.99f)
                curveTo(3.2f, 16.29f, 6.3f, 15.0f, 9.0f, 15.0f)
                reflectiveCurveToRelative(5.8f, 1.29f, 6.0f, 2.0f)
                verticalLineTo(18.0f)
                close()
            }
        }
        return _peopleAlt!!
    }

private var _peopleAlt: ImageVector? = null
