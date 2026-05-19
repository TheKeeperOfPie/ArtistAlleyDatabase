package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val Substack = ImageVector.Builder(
    name = "Substack",
    defaultWidth = 24.dp,
    defaultHeight = (211.66664 / 185.91388 * 24).dp,
    viewportWidth = 185.91388f,
    viewportHeight = 211.66664f
).apply {
    group(
        translationX = -10.338551f,
        translationY = -10.004978f,
    ) {
        materialPath(
            fillAlpha = 1.0f,
            strokeAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(10.338551f, 10.004978f)
            verticalLineToRelative(25.04722f)
            horizontalLineTo(196.25243f)
            verticalLineTo(10.004978f)
            horizontalLineTo(10.338551f)
            moveToRelative(0f, 47.977777f)
            verticalLineTo(82.677199f)
            horizontalLineTo(196.25243f)
            verticalLineTo(57.982755f)
            horizontalLineTo(10.338551f)
            moveToRelative(0f, 47.625005f)
            verticalLineToRelative(116.06388f)
            curveToRelative(
                7.84910f,
                -2.48380f,
                15.75080f,
                -8.49370f,
                22.93060f,
                -12.55030f
            )
            curveToRelative(
                15.35690f,
                -8.67680f,
                30.71830f,
                -17.39470f,
                46.21390f,
                -25.82110f
            )
            curveToRelative(5.67490f, -3.08590f, 11.32640f, -6.25020f, 16.93330f, -9.45780f)
            curveToRelative(1.72360f, -0.9860f, 4.87630f, -3.6240f, 6.90580f, -3.51280f)
            curveToRelative(2.22530f, 0.12190f, 5.29270f, 2.67890f, 7.20530f, 3.770f)
            curveToRelative(5.73530f, 3.27190f, 11.53730f, 6.42690f, 17.28610f, 9.6750f)
            curveToRelative(15.47560f, 8.74380f, 30.98670f, 17.45390f, 46.56670f, 26.01030f)
            curveToRelative(6.88260f, 3.77980f, 14.34380f, 9.67210f, 21.87220f, 11.88660f)
            verticalLineTo(105.60776f)
            close()
        }
    }
}.build()
