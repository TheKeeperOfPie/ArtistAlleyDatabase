package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val Linktree = ImageVector.Builder(
    name = "Linktree",
    defaultWidth = 24.dp,
    defaultHeight = (512.238 / 417 * 24).dp,
    viewportWidth = 417f,
    viewportHeight = 512.238f
).apply {
    materialPath(
        fillAlpha = 1.0f,
        strokeAlpha = 1.0f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(171.274f, 344.942f)
        horizontalLineToRelative(74.09f)
        verticalLineToRelative(167.296f)
        horizontalLineToRelative(-74.09f)
        verticalLineTo(344.942f)
        close()
        moveTo(0f, 173.468f)
        horizontalLineToRelative(126.068f)
        lineToRelative(-89.622f, -85.44f)
        lineToRelative(49.591f, -50.985f)
        lineToRelative(85.439f, 87.829f)
        verticalLineTo(0f)
        horizontalLineToRelative(74.086f)
        verticalLineToRelative(124.872f)
        lineTo(331f, 37.243f)
        lineToRelative(49.552f, 50.785f)
        lineToRelative(-89.58f, 85.24f)
        horizontalLineTo(417f)
        verticalLineToRelative(70.502f)
        horizontalLineTo(290.252f)
        lineToRelative(90.183f, 87.629f)
        lineTo(331f, 381.192f)
        lineTo(208.519f, 258.11f)
        lineTo(86.037f, 381.192f)
        lineToRelative(-49.591f, -49.591f)
        lineToRelative(90.218f, -87.631f)
        horizontalLineTo(0f)
        verticalLineToRelative(-70.502f)
        close()
    }
}.build()
