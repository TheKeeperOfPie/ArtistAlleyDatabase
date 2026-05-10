@file:Suppress("UnusedReceiverParameter")

package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialIcon
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.ProductionQuantityLimits: ImageVector
    get() {
        if (_productionQuantityLimits != null) {
            return _productionQuantityLimits!!
        }
        _productionQuantityLimits = materialIcon(name = "Filled.ProductionQuantityLimits") {
            materialPath {
                moveTo(13.0f, 10.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(10.0f)
                close()
                moveTo(13.0f, 6.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(7.0f, 18.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                reflectiveCurveTo(5.9f, 22.0f, 7.0f, 22.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveTo(8.1f, 18.0f, 7.0f, 18.0f)
                close()
                moveTo(17.0f, 18.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                reflectiveCurveToRelative(0.89f, 2.0f, 1.99f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveTo(18.1f, 18.0f, 17.0f, 18.0f)
                close()
                moveTo(8.1f, 13.0f)
                horizontalLineToRelative(7.45f)
                curveToRelative(0.75f, 0.0f, 1.41f, -0.41f, 1.75f, -1.03f)
                lineTo(21.0f, 4.96f)
                lineTo(19.25f, 4.0f)
                lineToRelative(-3.7f, 7.0f)
                horizontalLineTo(8.53f)
                lineTo(4.27f, 2.0f)
                horizontalLineTo(1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(3.6f, 7.59f)
                lineToRelative(-1.35f, 2.44f)
                curveTo(4.52f, 15.37f, 5.48f, 17.0f, 7.0f, 17.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(7.0f)
                lineTo(8.1f, 13.0f)
                close()
            }
        }
        return _productionQuantityLimits!!
    }

private var _productionQuantityLimits: ImageVector? = null
