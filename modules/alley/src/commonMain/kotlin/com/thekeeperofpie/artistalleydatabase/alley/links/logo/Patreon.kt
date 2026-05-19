package com.thekeeperofpie.artistalleydatabase.alley.links.logo

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

internal val Patreon = ImageVector.Builder(
    name = "Patreon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 1080f,
    viewportHeight = 1080f
).apply {
    materialPath(
        fillAlpha = 1.0f,
        strokeAlpha = 1.0f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(1033.05f, 324.45f)
        curveToRelative(-0.190f, -137.90f, -107.590f, -250.920f, -233.60f, -291.70f)
        curveToRelative(-156.480f, -50.640f, -362.860f, -43.30f, -512.280f, 27.20f)
        curveTo(106.070f, 145.410f, 49.180f, 332.610f, 47.060f, 519.310f)
        curveToRelative(-1.740f, 153.50f, 13.580f, 557.790f, 241.620f, 560.670f)
        curveToRelative(169.440f, 2.150f, 194.670f, -216.180f, 273.070f, -321.330f)
        curveToRelative(55.780f, -74.810f, 127.60f, -95.940f, 216.010f, -117.820f)
        curveTo(929.710f, 603.220f, 1033.270f, 483.30f, 1033.050f, 324.450f)
        close()
    }
}.build()
