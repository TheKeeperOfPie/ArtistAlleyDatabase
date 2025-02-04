package com.thekeeperofpie.artistalleydatabase.alley.links

import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

enum class Logo(val icon: ImageVector) {
    // https://www.artstation.com/about/logo
    ART_STATION(
        ImageVector.Builder(
            name = "ArtStation",
            defaultWidth = 16.dp,
            defaultHeight = (93.099998 / 105.8 * 16).dp,
            viewportWidth = 105.8f,
            viewportHeight = 93.100001f
        ).apply {
            group(
                translationX = -51.4f,
                translationY = -51.5f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.Companion.NonZero
                ) {
                    moveTo(51.4f, 123.3f)
                    lineToRelative(8.9f, 15.4f)
                    verticalLineToRelative(0f)
                    curveToRelative(1.80f, 3.50f, 5.40f, 5.90f, 9.50f, 5.90f)
                    verticalLineToRelative(0f)
                    verticalLineToRelative(0f)
                    horizontalLineToRelative(59.3f)
                    lineToRelative(-12.3f, -21.3f)
                    close()
                }
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.Companion.NonZero
                ) {
                    moveTo(157.2f, 123.4f)
                    curveToRelative(00f, -2.10f, -0.60f, -4.10f, -1.70f, -5.80f)
                    lineTo(120.7f, 57.2f)
                    curveToRelative(-1.80f, -3.40f, -5.30f, -5.70f, -9.40f, -5.70f)
                    horizontalLineTo(92.9f)
                    lineToRelative(53.7f, 93f)
                    lineToRelative(8.5f, -14.7f)
                    curveToRelative(1.60f, -2.80f, 2.10f, -40f, 2.10f, -6.40f)
                    close()
                }
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.Companion.NonZero
                ) {
                    moveTo(60.2f, 108.1f)
                    lineTo(108.1f, 108.1f)
                    lineTo(84.2f, 66.6f)
                    close()
                }
            }
        }.build()
    ),

    // Converted using Inkscape from https://www.bigcartel.com/resources/help/article/brand-guide
    BIG_CARTEL(
        ImageVector.Builder(
            name = "Big Cartel",
            defaultWidth = 16.dp,
            defaultHeight = (190.228 / 149.995 * 16).dp,
            viewportWidth = 39.686f,
            viewportHeight = 50.331f
        ).apply {
            group(
                translationX = -96.371f,
                translationY = -53.662f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.Companion.NonZero
                ) {
                    moveTo(109.897f, 100.133f)
                    curveToRelative(-3.2750f, -2.1110f, -6.4750f, -4.2650f, -7.1120f, -4.7870f)
                    curveToRelative(-2.3720f, -1.9410f, -4.7970f, -5.9930f, -5.7660f, -9.6330f)
                    curveToRelative(-0.3870f, -1.4540f, -0.4590f, -3.6780f, -0.5470f, -16.8630f)
                    lineToRelative(-0.1f, -15.188f)
                    lineToRelative(0.636f, 0.417f)
                    curveToRelative(4.4830f, 2.9370f, 19.0520f, 12.10f, 19.240f, 12.10f)
                    curveToRelative(0.1370f, 00f, 4.6280f, -2.7890f, 9.980f, -6.1970f)
                    lineToRelative(9.73f, -6.197f)
                    lineToRelative(0.08f, 4.279f)
                    curveToRelative(0.0890f, 4.730f, -0.1020f, 5.8190f, -1.1990f, 6.8270f)
                    curveToRelative(-0.4030f, 0.3710f, -4.3640f, 2.9720f, -8.8030f, 5.7790f)
                    curveToRelative(-4.4380f, 2.8070f, -8.340f, 5.3490f, -8.670f, 5.6480f)
                    curveToRelative(-0.7570f, 0.6880f, -1.1850f, 2.1030f, -1.0730f, 3.5530f)
                    lineToRelative(0.086f, 1.125f)
                    lineToRelative(9.79f, -6.229f)
                    lineToRelative(9.789f, -6.23f)
                    lineToRelative(0.07f, 6.681f)
                    curveToRelative(0.0810f, 7.5960f, -0.2120f, 10.1240f, -1.5490f, 13.3340f)
                    curveToRelative(-10f, 2.4020f, -3.0180f, 5.3120f, -4.6080f, 6.6470f)
                    curveToRelative(-1.5250f, 1.280f, -13.280f, 8.8220f, -13.7050f, 8.7940f)
                    curveToRelative(-0.1740f, -0.0120f, -2.9950f, -1.750f, -6.270f, -3.860f)
                    close()
                }
            }
        }.build()
    ),

    // https://nucleoapp.com/social-media-icons
    BLUESKY(
        ImageVector.Builder(
            name = "Bluesky",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.Companion.NonZero
            ) {
                moveTo(23.931f, 5.298f)
                curveToRelative(-3.210f, 2.4180f, -6.6630f, 7.320f, -7.9310f, 9.9510f)
                curveToRelative(-1.2670f, -2.6310f, -4.7210f, -7.5330f, -7.9310f, -9.9510f)
                curveToRelative(-2.3160f, -1.7440f, -6.0690f, -3.0940f, -6.0690f, 1.2010f)
                curveToRelative(00f, 0.8570f, 0.490f, 7.2060f, 0.7780f, 8.2370f)
                curveToRelative(0.9990f, 3.5830f, 4.6410f, 4.4970f, 7.8810f, 3.9440f)
                curveToRelative(-5.6630f, 0.9670f, -7.1030f, 4.1690f, -3.9920f, 7.3720f)
                curveToRelative(5.9080f, 6.0830f, 8.4920f, -1.5260f, 9.1540f, -3.4760f)
                curveToRelative(0.1230f, -0.360f, 0.1790f, -0.5270f, 0.1790f, -0.3790f)
                curveToRelative(00f, -0.1480f, 0.0570f, 0.0190f, 0.1790f, 0.3790f)
                curveToRelative(0.6620f, 1.9490f, 3.2450f, 9.5580f, 9.1540f, 3.4760f)
                curveToRelative(3.1110f, -3.2030f, 1.6710f, -6.4050f, -3.9920f, -7.3720f)
                curveToRelative(3.240f, 0.5530f, 6.8820f, -0.3610f, 7.8810f, -3.9440f)
                curveToRelative(0.2880f, -1.0310f, 0.7780f, -7.380f, 0.7780f, -8.2370f)
                curveToRelative(00f, -4.2950f, -3.7530f, -2.9450f, -6.0690f, -1.2010f)
                close()
            }
        }.build()
    ),

    // https://carrd.co/docs/general/brand-assets
    CARRD(
        ImageVector.Builder(
            name = "Carrd",
            defaultWidth = 16.dp,
            defaultHeight = (75.631973 / 60.271461 * 16).dp,
            viewportWidth = 60.271461f,
            viewportHeight = 75.631973f
        ).apply {
            group(
                translationX = -35.88046f,
                translationY = -38.078525f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.Companion.NonZero
                ) {
                    moveTo(57.388827f, 113.55036f)
                    curveToRelative(-0.210f, -0.10660f, -0.46330f, -0.33220f, -0.59270f, -0.52760f)
                    lineToRelative(-0.225914f, -0.34135f)
                    lineToRelative(-0.04233f, -8.93805f)
                    lineToRelative(-0.04233f, -8.938051f)
                    lineToRelative(-9.144001f, -3.82741f)
                    curveToRelative(
                        -5.02920f,
                        -2.10510f,
                        -9.47950f,
                        -3.96690f,
                        -9.88950f,
                        -4.13740f
                    )
                    curveToRelative(
                        -0.85460f,
                        -0.35530f,
                        -1.18290f,
                        -0.60110f,
                        -1.41770f,
                        -1.06140f
                    )
                    curveToRelative(
                        -0.1640f,
                        -0.32140f,
                        -0.16580f,
                        -0.62220f,
                        -0.14450f,
                        -23.51170f
                    )
                    lineToRelative(0.02162f, -23.18619f)
                    lineToRelative(0.204032f, -0.29776f)
                    curveToRelative(0.36130f, -0.52730f, 0.71570f, -0.70590f, 1.39840f, -0.70490f)
                    lineToRelative(0.59887f, 0.00089f)
                    lineToRelative(18.891231f, 9.07961f)
                    curveToRelative(10.39020f, 4.99380f, 18.93710f, 9.07960f, 18.99310f, 9.07960f)
                    curveToRelative(0.0560f, 00f, 4.15020f, -1.94310f, 9.09820f, -4.3180f)
                    curveToRelative(9.66860f, -4.64060f, 9.30180f, -4.48740f, 10.02080f, -4.1870f)
                    curveToRelative(0.39920f, 0.16680f, 0.67080f, 0.42090f, 0.87450f, 0.81810f)
                    curveToRelative(0.15720f, 0.30650f, 0.16110f, 0.87410f, 0.16110f, 23.39250f)
                    curveToRelative(00f, 22.90210f, -0.0010f, 23.08090f, -0.16910f, 23.40990f)
                    curveToRelative(-0.0930f, 0.18230f, -0.2930f, 0.42980f, -0.44450f, 0.550f)
                    curveToRelative(-0.15150f, 0.12020f, -8.48590f, 4.16550f, -18.5210f, 8.98970f)
                    curveToRelative(-16.86720f, 8.10860f, -18.2840f, 8.7740f, -18.75370f, 8.8070f)
                    curveToRelative(-0.40440f, 0.02840f, -0.58280f, -0.0020f, -0.87480f, -0.15060f)
                    close()
                    moveToRelative(19.416749f, -12.29861f)
                    lineToRelative(15.91734f, -7.652881f)
                    lineToRelative(0.0213f, -20.80389f)
                    curveToRelative(
                        0.01180f,
                        -11.44850f,
                        -0.01110f,
                        -20.79120f,
                        -0.05080f,
                        -20.77560f
                    )
                    curveToRelative(
                        -0.26990f,
                        0.10570f,
                        -32.67370f,
                        15.69350f,
                        -32.75760f,
                        15.75810f
                    )
                    curveToRelative(-0.08420f, 0.06470f, -0.10580f, 4.33530f, -0.10580f, 20.85220f)
                    verticalLineToRelative(20.770851f)
                    lineToRelative(0.529167f, -0.2479f)
                    curveToRelative(0.2910f, -0.13630f, 7.6920f, -3.69170f, 16.44650f, -7.90080f)
                    close()
                    moveToRelative(-10.52871f, -3.612451f)
                    curveToRelative(
                        -0.80180f,
                        -0.33950f,
                        -1.22540f,
                        -1.17060f,
                        -1.01740f,
                        -1.99630f
                    )
                    curveToRelative(0.20660f, -0.82030f, -0.15970f, -0.6190f, 10.3720f, -5.70090f)
                    curveToRelative(8.84980f, -4.27030f, 9.69920f, -4.66280f, 10.0910f, -4.66280f)
                    curveToRelative(0.52180f, 00f, 0.82810f, 0.10870f, 1.1480f, 0.40760f)
                    curveToRelative(0.74040f, 0.69160f, 0.71910f, 1.90150f, -0.04410f, 2.50490f)
                    curveToRelative(-0.15630f, 0.12360f, -4.58940f, 2.30680f, -9.85140f, 4.85160f)
                    curveToRelative(-8.58070f, 4.14980f, -9.61540f, 4.62970f, -10.0330f, 4.65360f)
                    curveToRelative(-0.25610f, 0.01470f, -0.55540f, -0.01130f, -0.66510f, -0.05770f)
                    close()
                    moveToRelative(0.36871f, -10.36769f)
                    curveToRelative(
                        -0.04660f,
                        -0.01660f,
                        -0.18870f,
                        -0.05310f,
                        -0.31590f,
                        -0.08110f
                    )
                    curveToRelative(
                        -0.29140f,
                        -0.06420f,
                        -0.79340f,
                        -0.50020f,
                        -0.97210f,
                        -0.84420f
                    )
                    curveToRelative(-0.21820f, -0.42020f, -0.17960f, -1.14330f, 0.08510f, -1.59490f)
                    curveToRelative(0.12230f, -0.20860f, 0.30960f, -0.43580f, 0.41630f, -0.50470f)
                    curveToRelative(0.10670f, -0.0690f, 4.51830f, -2.21750f, 9.80360f, -4.77450f)
                    curveToRelative(9.54230f, -4.61650f, 9.61340f, -4.64910f, 10.13370f, -4.64760f)
                    curveToRelative(0.44360f, 0.0010f, 0.57910f, 0.04030f, 0.88250f, 0.2540f)
                    curveToRelative(0.92650f, 0.65260f, 0.99450f, 2.06290f, 0.12990f, 2.69580f)
                    curveToRelative(-0.47980f, 0.35120f, -19.38440f, 9.430f, -19.73980f, 9.47990f)
                    curveToRelative(-0.18630f, 0.02620f, -0.37680f, 0.0340f, -0.42330f, 0.01740f)
                    close()
                    moveToRelative(-0.30365f, -10.5354f)
                    curveToRelative(-0.67810f, -0.21930f, -1.12180f, -0.8550f, -1.12110f, -1.60610f)
                    curveToRelative(0.00050f, -0.52820f, 0.14240f, -0.86720f, 0.50710f, -1.21130f)
                    curveToRelative(0.17820f, -0.16820f, 15.72030f, -7.76570f, 19.15280f, -9.36250f)
                    curveToRelative(0.96520f, -0.4490f, 1.90740f, -0.17720f, 2.33080f, 0.67240f)
                    curveToRelative(0.130f, 0.2610f, 0.21910f, 0.58140f, 0.2180f, 0.78440f)
                    curveToRelative(-0.0020f, 0.40680f, -0.23390f, 0.94640f, -0.51870f, 1.20840f)
                    curveToRelative(-0.11240f, 0.10330f, -4.54650f, 2.28960f, -9.85370f, 4.85840f)
                    curveToRelative(-10.15570f, 4.91560f, -10.05230f, 4.87070f, -10.7150f, 4.65630f)
                    close()
                    moveToRelative(-9.814013f, 10.24781f)
                    verticalLineToRelative(-4.13729f)
                    lineToRelative(-5.5245f, -2.22377f)
                    curveToRelative(-3.03850f, -1.22310f, -5.64440f, -2.3130f, -5.7910f, -2.42210f)
                    curveToRelative(-0.66870f, -0.49760f, -0.82770f, -1.65380f, -0.3160f, -2.29710f)
                    curveToRelative(0.31840f, -0.40020f, 0.89410f, -0.69980f, 1.3450f, -0.69980f)
                    curveToRelative(0.25430f, 00f, 1.65260f, 0.52580f, 5.29120f, 1.98970f)
                    curveToRelative(2.720f, 1.09430f, 4.95720f, 1.98970f, 4.97140f, 1.98970f)
                    curveToRelative(0.01420f, 00f, 0.01590f, -1.53330f, 0.00370f, -3.40730f)
                    lineToRelative(-0.02213f, -3.40729f)
                    lineToRelative(-2.413001f, -0.97827f)
                    curveToRelative(
                        -1.32720f,
                        -0.53810f,
                        -3.81840f,
                        -1.54530f,
                        -5.53610f,
                        -2.23820f
                    )
                    curveToRelative(-1.71770f, -0.6930f, -3.23380f, -1.35320f, -3.36920f, -1.46710f)
                    curveToRelative(-0.78220f, -0.65820f, -0.7640f, -1.97840f, 0.03570f, -2.58750f)
                    curveToRelative(0.37910f, -0.28880f, 0.95190f, -0.42290f, 1.37760f, -0.32260f)
                    curveToRelative(0.18570f, 0.04380f, 2.43320f, 0.92740f, 4.99440f, 1.96360f)
                    curveToRelative(2.56120f, 1.03620f, 4.72060f, 1.90170f, 4.79880f, 1.92330f)
                    curveToRelative(0.12780f, 0.03530f, 0.14480f, -0.0930f, 0.16930f, -1.27370f)
                    lineToRelative(0.02725f, -1.31306f)
                    lineToRelative(0.254683f, -0.32506f)
                    curveToRelative(0.20250f, -0.25840f, 0.53650f, -0.4630f, 1.62980f, -0.99820f)
                    curveToRelative(0.75630f, -0.37020f, 1.37460f, -0.69410f, 1.3740f, -0.71970f)
                    curveToRelative(
                        -0.00060f,
                        -0.02560f,
                        -3.22280f,
                        -1.50950f,
                        -7.16030f,
                        -3.29750f
                    )
                    curveToRelative(-3.93750f, -1.7880f, -7.27970f, -3.33220f, -7.42710f, -3.43150f)
                    curveToRelative(-0.67760f, -0.45670f, -0.8710f, -1.53190f, -0.40710f, -2.26340f)
                    curveToRelative(0.30060f, -0.4740f, 0.81610f, -0.74840f, 1.40560f, -0.74840f)
                    curveToRelative(0.36210f, 00f, 1.43220f, 0.46560f, 8.96170f, 3.89930f)
                    curveToRelative(4.70280f, 2.14460f, 8.57720f, 3.90670f, 8.60990f, 3.91580f)
                    curveToRelative(0.07490f, 0.02090f, 8.20370f, -3.8630f, 8.20550f, -3.92040f)
                    curveToRelative(0.0010f, -0.0490f, -32.53110f, -15.69190f, -32.720f, -15.7330f)
                    curveToRelative(-0.1110f, -0.02420f, -0.12580f, 2.40820f, -0.12580f, 20.70940f)
                    verticalLineToRelative(20.73675f)
                    lineToRelative(8.5725f, 3.60172f)
                    curveToRelative(4.71490f, 1.98090f, 8.62010f, 3.60590f, 8.67830f, 3.61110f)
                    curveToRelative(0.08330f, 0.0070f, 0.10580f, -0.86950f, 0.10580f, -4.12790f)
                    close()
                }
            }
        }.build()
    ),

    // https://nucleoapp.com/social-media-icons
    DEVIANT_ART(
        ImageVector.Builder(
            name = "DeviantArt",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(24.403f, 2f)
                horizontalLineToRelative(-4.82f)
                lineToRelative(-0.514f, 0.511f)
                lineToRelative(-2.452f, 4.68f)
                lineToRelative(-0.716f, 0.413f)
                horizontalLineTo(7.597f)
                verticalLineToRelative(6.995f)
                horizontalLineToRelative(4.437f)
                lineToRelative(0.462f, 0.462f)
                lineToRelative(-4.899f, 9.351f)
                verticalLineToRelative(5.588f)
                horizontalLineToRelative(0f)
                lineToRelative(4.823f, -0.002f)
                lineToRelative(0.516f, -0.513f)
                lineToRelative(2.457f, -4.682f)
                lineToRelative(0.701f, -0.405f)
                horizontalLineToRelative(8.309f)
                verticalLineToRelative(-6.991f)
                horizontalLineToRelative(-4.45f)
                lineToRelative(-0.45f, -0.45f)
                lineToRelative(4.901f, -9.356f)
            }
        }.build()
    ),

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/discord.svg
    DISCORD(
        ImageVector.Builder(
            name = "Discord",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13.545f, 2.907f)
                arcToRelative(
                    13.2f,
                    13.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -3.257f,
                    -1.011f
                )
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.052f,
                    0.025f
                )
                curveToRelative(-0.141f, 0.25f, -0.297f, 0.577f, -0.406f, 0.833f)
                arcToRelative(
                    12.2f,
                    12.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -3.658f,
                    0f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.412f,
                    -0.833f
                )
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.052f,
                    -0.025f
                )
                curveToRelative(-1.125f, 0.194f, -2.22f, 0.534f, -3.257f, 1.011f)
                arcToRelative(
                    0.04f,
                    0.04f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.021f,
                    0.018f
                )
                curveTo(0.356f, 6.024f, -0.213f, 9.047f, 0.066f, 12.032f)
                quadToRelative(0.003f, 0.022f, 0.021f, 0.037f)
                arcToRelative(
                    13.3f,
                    13.3f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    3.995f,
                    2.02f
                )
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.056f,
                    -0.019f
                )
                quadToRelative(0.463f, -0.63f, 0.818f, -1.329f)
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.01f,
                    -0.059f
                )
                lineToRelative(-0.018f, -0.011f)
                arcToRelative(
                    9f,
                    9f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -1.248f,
                    -0.595f
                )
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.02f,
                    -0.066f
                )
                lineToRelative(0.015f, -0.019f)
                quadToRelative(0.127f, -0.095f, 0.248f, -0.195f)
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0.051f,
                    -0.007f
                )
                curveToRelative(2.619f, 1.196f, 5.454f, 1.196f, 8.041f, 0f)
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0.053f,
                    0.007f
                )
                quadToRelative(0.121f, 0.1f, 0.248f, 0.195f)
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.004f,
                    0.085f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -1.249f,
                    0.594f
                )
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.03f,
                    0.03f
                )
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.003f,
                    0.041f
                )
                curveToRelative(0.24f, 0.465f, 0.515f, 0.909f, 0.817f, 1.329f)
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.056f,
                    0.019f
                )
                arcToRelative(
                    13.2f,
                    13.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    4.001f,
                    -2.02f
                )
                arcToRelative(
                    0.05f,
                    0.05f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0.021f,
                    -0.037f
                )
                curveToRelative(0.334f, -3.451f, -0.559f, -6.449f, -2.366f, -9.106f)
                arcToRelative(
                    0.03f,
                    0.03f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.02f,
                    -0.019f
                )
                moveToRelative(-8.198f, 7.307f)
                curveToRelative(-0.789f, 0f, -1.438f, -0.724f, -1.438f, -1.612f)
                reflectiveCurveToRelative(0.637f, -1.613f, 1.438f, -1.613f)
                curveToRelative(0.807f, 0f, 1.45f, 0.73f, 1.438f, 1.613f)
                curveToRelative(0f, 0.888f, -0.637f, 1.612f, -1.438f, 1.612f)
                moveToRelative(5.316f, 0f)
                curveToRelative(-0.788f, 0f, -1.438f, -0.724f, -1.438f, -1.612f)
                reflectiveCurveToRelative(0.637f, -1.613f, 1.438f, -1.613f)
                curveToRelative(0.807f, 0f, 1.451f, 0.73f, 1.438f, 1.613f)
                curveToRelative(0f, 0.888f, -0.631f, 1.612f, -1.438f, 1.612f)
            }
        }.build()
    ),

    // Converted using Inkscape from https://www.etsy.com/press
    ETSY(
        ImageVector.Builder(
            name = "Etsy",
            defaultWidth = (605.17 / 286.099 * 16).dp,
            defaultHeight = 16.dp,
            viewportWidth = 180.118f,
            viewportHeight = 75.697f
        ).apply {
            group(
                translationY = -30.429f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(137.919f, 105.79f)
                    curveToRelative(-0.9370f, -0.2120f, -2.320f, -0.6880f, -3.0720f, -1.0560f)
                    lineToRelative(-1.367f, -0.669f)
                    lineToRelative(0.623f, -2.578f)
                    curveToRelative(0.3430f, -1.4180f, 0.8280f, -3.9040f, 1.0780f, -5.5230f)
                    lineToRelative(0.455f, -2.945f)
                    lineToRelative(1.574f, -0.165f)
                    curveToRelative(0.8660f, -0.090f, 1.6260f, -0.1130f, 1.6890f, -0.050f)
                    curveToRelative(0.0630f, 0.0630f, 0.4560f, 1.760f, 0.8730f, 3.7710f)
                    curveToRelative(0.6340f, 3.060f, 0.8680f, 3.7510f, 1.430f, 4.240f)
                    curveToRelative(1.840f, 1.5980f, 6.1030f, 0.7010f, 9.3620f, -1.9690f)
                    curveToRelative(1.5950f, -1.3070f, 3.6810f, -3.8970f, 5.0120f, -6.2230f)
                    lineToRelative(1.12f, -1.957f)
                    lineToRelative(-2.795f, -7.124f)
                    curveToRelative(-5.3870f, -13.730f, -11.0330f, -27.1350f, -12.1430f, -28.8280f)
                    curveToRelative(-0.2370f, -0.3620f, -1.070f, -0.8630f, -1.9980f, -1.2010f)
                    lineToRelative(-1.592f, -0.58f)
                    verticalLineToRelative(-2.708f)
                    lineToRelative(9.657f, -0.001f)
                    lineToRelative(9.657f, -0.002f)
                    verticalLineToRelative(2.721f)
                    lineToRelative(-1.785f, 0.336f)
                    curveToRelative(-1.430f, 0.270f, -1.9650f, 0.5150f, -2.680f, 1.2280f)
                    curveToRelative(-0.7770f, 0.7760f, -0.8760f, 1.020f, -0.7580f, 1.8770f)
                    curveToRelative(0.2070f, 1.4980f, 8.3370f, 23.5940f, 8.9060f, 24.2060f)
                    curveToRelative(0.3340f, 0.360f, 10.0760f, -23.0660f, 10.0760f, -24.230f)
                    curveToRelative(00f, -1.1670f, -1.170f, -2.2190f, -3.190f, -2.870f)
                    lineToRelative(-1.837f, -0.591f)
                    verticalLineToRelative(-2.674f)
                    horizontalLineToRelative(15.633f)
                    lineToRelative(-0.078f, 1.299f)
                    lineToRelative(-0.077f, 1.298f)
                    lineToRelative(-2.192f, 0.914f)
                    curveToRelative(-1.5120f, 0.630f, -2.3850f, 1.170f, -2.8160f, 1.7440f)
                    curveToRelative(-0.3440f, 0.4570f, -3.6880f, 8.0930f, -7.430f, 16.970f)
                    curveToRelative(-3.7440f, 8.8770f, -7.4630f, 17.450f, -8.2640f, 19.050f)
                    curveToRelative(-3.7470f, 7.4840f, -7.9650f, 11.8880f, -13.2330f, 13.8180f)
                    curveToRelative(-2.2870f, 0.8370f, -7.1990f, 1.0730f, -9.8380f, 0.4730f)
                    close()
                    moveTo(87.712f, 88.983f)
                    curveToRelative(-2.0210f, -0.660f, -3.6480f, -1.8640f, -4.7690f, -3.530f)
                    curveToRelative(-1.5120f, -2.2460f, -1.4990f, -2.110f, -1.5950f, -16.7720f)
                    lineToRelative(-0.088f, -13.56f)
                    horizontalLineToRelative(-7.121f)
                    verticalLineToRelative(-2.806f)
                    lineToRelative(1.534f, -0.317f)
                    curveToRelative(4.9270f, -1.0170f, 8.3750f, -4.8870f, 9.90f, -11.1080f)
                    lineToRelative(0.598f, -2.439f)
                    horizontalLineToRelative(3.314f)
                    verticalLineToRelative(12.216f)
                    lineToRelative(6.019f, -0.188f)
                    curveToRelative(3.310f, -0.1040f, 6.1260f, -0.250f, 6.2560f, -0.3270f)
                    curveToRelative(0.130f, -0.0760f, 0.170f, 0.1470f, 0.0870f, 0.4970f)
                    curveToRelative(-0.0830f, 0.3490f, -0.2340f, 1.5590f, -0.3350f, 2.6890f)
                    curveToRelative(-0.1010f, 1.130f, -0.2680f, 2.1060f, -0.3710f, 2.170f)
                    curveToRelative(-0.1030f, 0.0630f, -2.3210f, -0.0540f, -4.930f, -0.2610f)
                    curveToRelative(-2.6080f, -0.2070f, -5.1890f, -0.380f, -5.7340f, -0.3840f)
                    lineToRelative(-0.993f, -0.007f)
                    verticalLineTo(66.46f)
                    curveToRelative(00f, 6.7670f, 0.1130f, 12.20f, 0.270f, 13.0310f)
                    curveToRelative(0.7880f, 4.170f, 3.190f, 5.8540f, 6.9470f, 4.870f)
                    curveToRelative(1.7060f, -0.4450f, 2.5550f, -0.9090f, 3.8520f, -2.1030f)
                    lineToRelative(0.883f, -0.813f)
                    lineToRelative(0.771f, 0.916f)
                    curveToRelative(0.4240f, 0.5040f, 0.7710f, 1.050f, 0.7710f, 1.2120f)
                    curveToRelative(00f, 0.690f, -1.7050f, 2.7830f, -2.9830f, 3.6630f)
                    curveToRelative(-2.2470f, 1.5460f, -4.1090f, 2.0820f, -7.60f, 2.1880f)
                    curveToRelative(-2.5130f, 0.0770f, -3.3290f, 00f, -4.6830f, -0.4430f)
                    close()
                    moveToRelative(29.95f, 0.368f)
                    curveToRelative(-2.0070f, -0.2130f, -6.640f, -1.1540f, -8.0650f, -1.6380f)
                    lineToRelative(-1.428f, -0.485f)
                    lineToRelative(0.117f, -1.568f)
                    curveToRelative(0.0640f, -0.8620f, 0.080f, -3.390f, 0.0360f, -5.6160f)
                    lineToRelative(-0.08f, -4.048f)
                    lineToRelative(1.497f, 0.08f)
                    lineToRelative(1.498f, 0.08f)
                    lineToRelative(1.288f, 3.307f)
                    curveToRelative(1.4990f, 3.8460f, 2.3320f, 4.9790f, 4.4150f, 6.0020f)
                    curveToRelative(1.3470f, 0.6610f, 1.7980f, 0.7430f, 4.030f, 0.7340f)
                    curveToRelative(2.0850f, -0.0090f, 2.730f, -0.1160f, 3.7840f, -0.630f)
                    curveToRelative(3.9770f, -1.9430f, 4.1650f, -7.460f, 0.3520f, -10.3350f)
                    curveToRelative(-0.7470f, -0.5620f, -3.560f, -2.0850f, -6.2530f, -3.3840f)
                    curveToRelative(-4.8550f, -2.3410f, -6.9920f, -3.6910f, -8.2630f, -5.220f)
                    curveToRelative(-2.470f, -2.9680f, -2.9440f, -7.9280f, -1.1060f, -11.5550f)
                    curveToRelative(1.2320f, -2.4310f, 3.770f, -4.3080f, 7.2530f, -5.3620f)
                    curveToRelative(3.1750f, -0.9620f, 10.1630f, -0.7750f, 14.730f, 0.3930f)
                    curveToRelative(1.1160f, 0.2850f, 2.0630f, 0.5530f, 2.1060f, 0.5970f)
                    curveToRelative(0.0430f, 0.0430f, -0.0390f, 0.8460f, -0.1820f, 1.7850f)
                    reflectiveCurveToRelative(-0.348f, 3.136f, -0.454f, 4.882f)
                    lineToRelative(-0.193f, 3.175f)
                    lineToRelative(-1.495f, 0.078f)
                    lineToRelative(-1.496f, 0.079f)
                    lineToRelative(-1.11f, -3.333f)
                    curveToRelative(-1.010f, -3.0290f, -1.20f, -3.3920f, -2.0770f, -3.9710f)
                    curveToRelative(-1.920f, -1.2670f, -5.7830f, -1.5150f, -8.1560f, -0.5240f)
                    curveToRelative(-2.8130f, 1.1750f, -3.9780f, 3.960f, -2.7660f, 6.6130f)
                    curveToRelative(0.7390f, 1.6170f, 2.6030f, 3.1340f, 5.6330f, 4.5830f)
                    curveToRelative(1.5050f, 0.720f, 3.8670f, 1.8460f, 5.250f, 2.5040f)
                    curveToRelative(4.4190f, 2.10f, 7.2290f, 4.80f, 8.0940f, 7.7750f)
                    curveToRelative(0.2940f, 1.0130f, 0.3930f, 2.2160f, 0.330f, 4.0150f)
                    curveToRelative(-0.080f, 2.2670f, -0.1920f, 2.7750f, -0.9590f, 4.330f)
                    curveToRelative(-1.4640f, 2.9710f, -4.260f, 5.040f, -8.1580f, 6.0370f)
                    curveToRelative(-2.0420f, 0.5220f, -6.150f, 0.8340f, -8.1710f, 0.620f)
                    close()
                    moveToRelative(-95.856f, -2.338f)
                    lineToRelative(0.077f, -1.577f)
                    lineToRelative(3.03f, -0.593f)
                    curveToRelative(1.7330f, -0.340f, 3.3180f, -0.8070f, 3.7040f, -1.0930f)
                    curveToRelative(1.530f, -1.1310f, 1.4690f, -0.140f, 1.4690f, -23.6020f)
                    curveToRelative(00f, -16.8390f, -0.0750f, -21.7730f, -0.3390f, -22.5170f)
                    curveToRelative(-0.6410f, -1.8070f, -1.4650f, -2.210f, -6.4740f, -3.160f)
                    lineToRelative(-1.522f, -0.29f)
                    verticalLineToRelative(-3.457f)
                    lineToRelative(1.125f, 0.149f)
                    curveToRelative(0.6180f, 0.0820f, 8.7440f, 0.230f, 18.0570f, 0.3270f)
                    curveToRelative(16.1770f, 0.170f, 18.6730f, 0.1150f, 26.9640f, -0.5920f)
                    lineToRelative(2.093f, -0.178f)
                    lineToRelative(-0.16f, 1.564f)
                    curveToRelative(-0.0870f, 0.860f, -0.290f, 4.0640f, -0.4510f, 7.120f)
                    curveToRelative(-0.1610f, 3.0550f, -0.3540f, 6.0620f, -0.4280f, 6.680f)
                    lineToRelative(-0.136f, 1.125f)
                    horizontalLineToRelative(-1.506f)
                    lineToRelative(-1.505f, -0.001f)
                    lineToRelative(-0.86f, -3.324f)
                    curveToRelative(-0.4730f, -1.8280f, -1.1560f, -3.950f, -1.5180f, -4.7150f)
                    curveToRelative(-0.8260f, -1.7450f, -2.3180f, -3.2570f, -3.5520f, -3.60f)
                    curveToRelative(-0.550f, -0.1530f, -4.9830f, -0.2640f, -10.5210f, -0.2650f)
                    curveToRelative(-8.3790f, -0.0010f, -9.6250f, 0.050f, -9.9910f, 0.4170f)
                    curveToRelative(-0.370f, 0.370f, -0.410f, 1.670f, -0.350f, 11.3110f)
                    lineToRelative(0.07f, 10.892f)
                    lineToRelative(1.984f, 0.08f)
                    curveToRelative(3.2960f, 0.1320f, 10.6540f, -0.3890f, 11.7080f, -0.8290f)
                    curveToRelative(1.2450f, -0.520f, 1.6340f, -1.1730f, 2.3110f, -3.880f)
                    curveToRelative(0.310f, -1.2380f, 0.680f, -2.6960f, 0.8230f, -3.2420f)
                    lineToRelative(0.26f, -0.992f)
                    horizontalLineToRelative(3.46f)
                    lineToRelative(-0.217f, 2.712f)
                    curveToRelative(-0.120f, 1.4910f, -0.2020f, 6.1940f, -0.1840f, 10.450f)
                    lineToRelative(0.032f, 7.74f)
                    horizontalLineToRelative(-3.357f)
                    lineToRelative(-0.262f, -0.992f)
                    arcToRelative(
                        70.35f,
                        70.35f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        -0.554f,
                        -2.311f
                    )
                    curveToRelative(-0.5550f, -2.510f, -1.180f, -3.5440f, -2.4960f, -4.1260f)
                    curveToRelative(-1.0430f, -0.4620f, -1.9970f, -0.550f, -7.420f, -0.6930f)
                    lineToRelative(-6.22f, -0.164f)
                    verticalLineToRelative(9.317f)
                    curveToRelative(00f, 5.8570f, 0.1060f, 9.7440f, 0.2880f, 10.4650f)
                    curveToRelative(0.3320f, 1.320f, 1.5190f, 2.5730f, 2.860f, 3.020f)
                    curveToRelative(1.440f, 0.4810f, 17.2960f, 0.4080f, 18.9640f, -0.0870f)
                    curveToRelative(2.9840f, -0.8870f, 4.380f, -2.6130f, 7.010f, -8.6740f)
                    lineToRelative(1.694f, -3.903f)
                    horizontalLineToRelative(1.53f)
                    curveToRelative(1.4520f, 00f, 1.5230f, 0.030f, 1.3880f, 0.5950f)
                    curveToRelative(-0.1380f, 0.580f, -1.9850f, 15.6520f, -1.9850f, 16.1970f)
                    curveToRelative(00f, 0.1870f, -7.7880f, 0.2740f, -24.4850f, 0.2740f)
                    horizontalLineTo(21.73f)
                    close()
                }
            }
        }.build()
    ),

    // https://news.faire.com/company-info/
    FAIRE(
        ImageVector.Builder(
            name = "Faire",
            defaultWidth = (256 / 32 * 8).dp,
            defaultHeight = 8.dp,
            viewportWidth = 256f,
            viewportHeight = 32f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(27.7020913f, 7.9346049f)
                lineTo(27.2644595f, 8.15258856f)
                curveTo(26.06830f, 6.67030f, 24.93040f, 5.4060f, 23.85090f, 4.35970f)
                curveTo(22.77140f, 3.31330f, 21.72110f, 2.54310f, 20.70f, 2.0490f)
                curveTo(20.2040f, 1.81650f, 19.30690f, 1.62030f, 18.00850f, 1.46050f)
                curveTo(16.71020f, 1.30060f, 15.20040f, 1.22070f, 13.47910f, 1.22070f)
                curveTo(13.18730f, 1.22070f, 12.85180f, 1.2280f, 12.47250f, 1.24250f)
                curveTo(12.09320f, 1.2570f, 11.72850f, 1.27160f, 11.37840f, 1.28610f)
                curveTo(11.02830f, 1.30060f, 10.70010f, 1.31520f, 10.39380f, 1.32970f)
                curveTo(10.08740f, 1.34420f, 9.86130f, 1.35150f, 9.71540f, 1.35150f)
                curveTo(9.68620f, 1.52590f, 9.64980f, 1.70030f, 9.6060f, 1.87470f)
                curveTo(9.56230f, 2.0490f, 9.52580f, 2.25250f, 9.49660f, 2.4850f)
                curveTo(9.46740f, 2.71750f, 9.43830f, 3.01540f, 9.40910f, 3.37870f)
                curveTo(9.37990f, 3.74210f, 9.35070f, 4.18530f, 9.32160f, 4.70840f)
                curveTo(9.29240f, 5.72570f, 9.27050f, 6.83740f, 9.25590f, 8.04360f)
                curveTo(9.24130f, 9.24980f, 9.2340f, 10.39050f, 9.2340f, 11.46590f)
                lineTo(9.23403043f, 15.6076294f)
                lineTo(10.0217676f, 15.6076294f)
                curveTo(10.48860f, 15.60760f, 11.00640f, 15.59310f, 11.57540f, 15.5640f)
                curveTo(12.14430f, 15.5350f, 12.73510f, 15.49860f, 13.34780f, 15.4550f)
                curveTo(13.96050f, 15.41140f, 14.4710f, 15.3460f, 14.87950f, 15.25890f)
                curveTo(15.92980f, 15.02630f, 16.76130f, 14.72120f, 17.3740f, 14.34330f)
                curveTo(17.98670f, 13.96550f, 18.46810f, 13.5150f, 18.81820f, 12.99180f)
                curveTo(19.16830f, 12.46870f, 19.41630f, 11.89460f, 19.56210f, 11.26980f)
                curveTo(19.7080f, 10.64490f, 19.83930f, 9.96910f, 19.9560f, 9.24250f)
                lineTo(20.481167f, 9.24250681f)
                lineTo(20.481167f, 23.280654f)
                lineTo(19.9560089f, 23.280654f)
                curveTo(19.72260f, 22.35060f, 19.38710f, 21.36970f, 18.94950f, 20.33790f)
                curveTo(18.51180f, 19.30610f, 17.98670f, 18.51410f, 17.3740f, 17.96190f)
                curveTo(16.81960f, 17.46780f, 16.22880f, 17.09720f, 15.60160f, 16.85010f)
                curveTo(14.97430f, 16.60310f, 14.23760f, 16.45050f, 13.39150f, 16.39240f)
                curveTo(12.95390f, 16.36330f, 12.57460f, 16.34150f, 12.25370f, 16.3270f)
                curveTo(11.93280f, 16.31240f, 11.62640f, 16.30520f, 11.33470f, 16.30520f)
                lineTo(9.23403043f, 16.3051771f)
                lineTo(9.23403043f, 21.7111717f)
                curveTo(9.2340f, 22.5250f, 9.24130f, 23.27340f, 9.25590f, 23.95640f)
                curveTo(9.27050f, 24.63940f, 9.27780f, 25.2280f, 9.27780f, 25.72210f)
                curveTo(9.3070f, 26.65210f, 9.3580f, 27.53130f, 9.4310f, 28.35970f)
                curveTo(9.50390f, 29.1880f, 9.67170f, 29.80560f, 9.93420f, 30.21250f)
                curveTo(10.19680f, 30.61940f, 10.64170f, 30.93190f, 11.2690f, 31.14990f)
                curveTo(11.89630f, 31.36780f, 12.8080f, 31.50590f, 14.00420f, 31.5640f)
                lineTo(14.0042168f, 32f)
                lineTo(0f, 32f)
                lineTo(0f, 31.5640327f)
                curveTo(0.72940f, 31.44780f, 1.40770f, 31.25890f, 2.0350f, 30.99730f)
                curveTo(2.66230f, 30.73570f, 3.18010f, 30.37240f, 3.58860f, 29.90740f)
                curveTo(3.73450f, 29.7330f, 3.85120f, 29.47870f, 3.93870f, 29.14440f)
                curveTo(4.02620f, 28.81020f, 4.10640f, 28.42510f, 4.17940f, 27.98910f)
                curveTo(4.25230f, 27.55310f, 4.30340f, 27.09540f, 4.33260f, 26.61580f)
                curveTo(4.36170f, 26.13620f, 4.37630f, 25.66390f, 4.37630f, 25.19890f)
                curveTo(4.37630f, 24.7920f, 4.38360f, 24.23980f, 4.39820f, 23.54220f)
                curveTo(4.41280f, 22.84470f, 4.42740f, 22.08170f, 4.4420f, 21.25340f)
                curveTo(4.45660f, 20.42510f, 4.46380f, 19.57490f, 4.46380f, 18.7030f)
                lineTo(4.46384409f, 7.67302452f)
                curveTo(4.46380f, 7.14990f, 4.45660f, 6.61220f, 4.4420f, 6.05990f)
                curveTo(4.42740f, 5.50770f, 4.39820f, 4.98460f, 4.35440f, 4.49050f)
                curveTo(4.31070f, 3.99640f, 4.23770f, 3.55310f, 4.13560f, 3.16080f)
                curveTo(4.03350f, 2.76840f, 3.89490f, 2.4850f, 3.71990f, 2.31060f)
                curveTo(3.28220f, 1.84560f, 2.81540f, 1.54770f, 2.31940f, 1.41690f)
                curveTo(1.82350f, 1.28610f, 1.18160f, 1.16260f, 0.39390f, 1.04630f)
                lineTo(0.393868597f, 0.610354223f)
                lineTo(24.7261952f, 0.610354223f)
                lineTo(27.7020913f, 7.9346049f)
                close()
                moveTo(75.5206564f, 32f)
                lineTo(75.5206564f, 31.5640327f)
                curveTo(75.60820f, 31.5640f, 75.81970f, 31.5350f, 76.15520f, 31.47680f)
                curveTo(76.49070f, 31.41870f, 76.84080f, 31.3170f, 77.20550f, 31.17170f)
                curveTo(77.57020f, 31.02630f, 77.89120f, 30.82290f, 78.16830f, 30.56130f)
                curveTo(78.44550f, 30.29970f, 78.58410f, 29.96550f, 78.58410f, 29.55860f)
                curveTo(78.58410f, 29.2970f, 78.50380f, 28.86830f, 78.34340f, 28.27250f)
                curveTo(78.18290f, 27.67670f, 77.95680f, 26.950f, 77.66510f, 26.09260f)
                curveTo(77.37330f, 25.23520f, 77.03780f, 24.28340f, 76.65850f, 23.23710f)
                curveTo(76.27920f, 22.19070f, 75.87080f, 21.10080f, 75.43310f, 19.96730f)
                lineTo(63.7921249f, 19.9673025f)
                curveTo(63.35450f, 21.07180f, 62.93150f, 22.12530f, 62.5230f, 23.12810f)
                curveTo(62.11450f, 24.13080f, 61.70610f, 25.14080f, 61.29760f, 26.1580f)
                curveTo(60.68490f, 27.72750f, 60.37860f, 28.8320f, 60.37860f, 29.47140f)
                curveTo(60.37860f, 29.84920f, 60.49530f, 30.16170f, 60.72870f, 30.40870f)
                curveTo(60.96210f, 30.65580f, 61.26120f, 30.85920f, 61.62580f, 31.01910f)
                curveTo(61.99050f, 31.17890f, 62.38440f, 31.29520f, 62.80750f, 31.36780f)
                curveTo(63.23050f, 31.44050f, 63.63170f, 31.50590f, 64.01090f, 31.5640f)
                lineTo(64.0109408f, 32f)
                lineTo(54.689384f, 32f)
                lineTo(54.689384f, 31.520436f)
                curveTo(55.44790f, 31.43320f, 56.09710f, 31.22980f, 56.63680f, 30.91010f)
                curveTo(57.17660f, 30.59040f, 57.49020f, 30.37240f, 57.57780f, 30.25610f)
                curveTo(57.7820f, 30.05270f, 58.11750f, 29.57310f, 58.58430f, 28.81740f)
                curveTo(59.05110f, 28.06180f, 59.620f, 26.94280f, 60.29110f, 25.46050f)
                curveTo(60.90380f, 24.09450f, 61.58940f, 22.53950f, 62.34790f, 20.79560f)
                curveTo(63.10650f, 19.05180f, 63.88690f, 17.2570f, 64.68930f, 15.41140f)
                curveTo(65.49160f, 13.56580f, 66.28660f, 11.70570f, 67.07440f, 9.83110f)
                curveTo(67.86210f, 7.95640f, 68.60610f, 6.20530f, 69.30630f, 4.57770f)
                lineTo(68.8686535f, 3.61852861f)
                curveTo(69.39380f, 3.3860f, 69.86790f, 3.10990f, 70.2910f, 2.79020f)
                curveTo(70.7140f, 2.47050f, 71.0860f, 2.13620f, 71.40690f, 1.78750f)
                curveTo(71.72780f, 1.43870f, 71.99770f, 1.10450f, 72.21650f, 0.78470f)
                curveTo(72.43540f, 0.4650f, 72.60310f, 0.20350f, 72.71980f, 00f)
                lineTo(73.0699185f, 0f)
                curveTo(73.97440f, 2.41240f, 74.8350f, 4.70120f, 75.65190f, 6.86650f)
                curveTo(76.46890f, 9.03180f, 77.24930f, 11.11720f, 77.99330f, 13.12260f)
                curveTo(78.73730f, 15.12810f, 79.45930f, 17.06810f, 80.15960f, 18.94280f)
                curveTo(80.85980f, 20.81740f, 81.560f, 22.67030f, 82.26020f, 24.50140f)
                curveTo(82.58110f, 25.40240f, 82.88020f, 26.15080f, 83.15730f, 26.74660f)
                curveTo(83.43450f, 27.34240f, 83.74810f, 28.01820f, 84.09820f, 28.77380f)
                curveTo(84.15660f, 28.89010f, 84.28790f, 29.09350f, 84.49210f, 29.38420f)
                curveTo(84.69630f, 29.67480f, 84.96620f, 29.97280f, 85.30170f, 30.27790f)
                curveTo(85.63720f, 30.58310f, 86.0530f, 30.8520f, 86.5490f, 31.08450f)
                curveTo(87.0450f, 31.3170f, 87.62850f, 31.44780f, 88.29950f, 31.47680f)
                lineTo(88.2995042f, 32f)
                lineTo(75.5206564f, 32f)
                close()
                moveTo(69.7876802f, 5.58038147f)
                curveTo(69.61260f, 5.95820f, 69.35730f, 6.5540f, 69.02180f, 7.36780f)
                curveTo(68.68630f, 8.18170f, 68.27060f, 9.16980f, 67.77460f, 10.33240f)
                curveTo(67.27860f, 11.4950f, 66.73160f, 12.81020f, 66.13350f, 14.27790f)
                curveTo(65.53540f, 15.74570f, 64.88620f, 17.3370f, 64.1860f, 19.05180f)
                lineTo(75.0830247f, 19.0517711f)
                curveTo(74.5870f, 17.74390f, 74.08380f, 16.4360f, 73.57320f, 15.12810f)
                curveTo(73.06260f, 13.82020f, 72.56660f, 12.56310f, 72.08520f, 11.35690f)
                curveTo(71.60380f, 10.15080f, 71.16620f, 9.04630f, 70.77240f, 8.04360f)
                curveTo(70.37850f, 7.04090f, 70.05030f, 6.21980f, 69.78770f, 5.58040f)
                close()
                moveTo(131.347883f, 32f)
                lineTo(118.08764f, 32f)
                lineTo(118.08764f, 31.5640327f)
                curveTo(118.8170f, 31.44780f, 119.49540f, 31.25890f, 120.12260f, 30.99730f)
                curveTo(120.74990f, 30.73570f, 121.28230f, 30.35790f, 121.720f, 29.86380f)
                curveTo(121.86590f, 29.68940f, 121.97530f, 29.44230f, 122.04820f, 29.12260f)
                curveTo(122.12110f, 28.80290f, 122.17950f, 28.42510f, 122.22330f, 27.98910f)
                curveTo(122.2670f, 27.55310f, 122.29620f, 27.09540f, 122.31080f, 26.61580f)
                curveTo(122.32540f, 26.13620f, 122.33270f, 25.66390f, 122.33270f, 25.19890f)
                curveTo(122.33270f, 24.7920f, 122.340f, 24.23980f, 122.35460f, 23.54220f)
                curveTo(122.36910f, 22.84470f, 122.38370f, 22.08170f, 122.39830f, 21.25340f)
                curveTo(122.41290f, 20.42510f, 122.42020f, 19.57490f, 122.42020f, 18.7030f)
                lineTo(122.420195f, 6.10354223f)
                curveTo(122.42020f, 5.58040f, 122.39830f, 5.09360f, 122.35460f, 4.64310f)
                curveTo(122.31080f, 4.19250f, 122.24510f, 3.78570f, 122.15760f, 3.42230f)
                curveTo(122.07010f, 3.0590f, 121.93880f, 2.77570f, 121.76370f, 2.57220f)
                curveTo(121.35530f, 2.07810f, 120.8520f, 1.72210f, 120.25390f, 1.50410f)
                curveTo(119.65580f, 1.28610f, 118.97750f, 1.13350f, 118.21890f, 1.04630f)
                lineTo(118.21893f, 0.610354223f)
                lineTo(131.30412f, 0.610354223f)
                lineTo(131.30412f, 1.04632153f)
                curveTo(130.69140f, 1.13350f, 130.0350f, 1.29340f, 129.33480f, 1.52590f)
                curveTo(128.63460f, 1.75840f, 128.09480f, 2.22340f, 127.71550f, 2.9210f)
                curveTo(127.4530f, 3.41510f, 127.31440f, 4.040f, 127.29980f, 4.79560f)
                curveTo(127.28520f, 5.55130f, 127.26330f, 6.26340f, 127.23410f, 6.93190f)
                curveTo(127.23410f, 7.65850f, 127.22690f, 8.37780f, 127.21230f, 9.08990f)
                curveTo(127.19770f, 9.8020f, 127.19040f, 10.52130f, 127.19040f, 11.2480f)
                lineTo(127.190381f, 25.852861f)
                curveTo(127.19040f, 26.69570f, 127.2050f, 27.50950f, 127.23410f, 28.29430f)
                curveTo(127.26330f, 29.0790f, 127.39460f, 29.63120f, 127.6280f, 29.9510f)
                curveTo(127.97810f, 30.44510f, 128.53240f, 30.82290f, 129.2910f, 31.08450f)
                curveTo(130.04960f, 31.34610f, 130.73520f, 31.50590f, 131.34790f, 31.5640f)
                lineTo(131.347883f, 32f)
                close()
                moveTo(197.590746f, 32f)
                lineTo(188.619295f, 32f)
                curveTo(188.35670f, 31.73840f, 188.16710f, 31.54220f, 188.05040f, 31.41140f)
                curveTo(187.93370f, 31.28070f, 187.80240f, 31.12810f, 187.65650f, 30.95370f)
                curveTo(187.51060f, 30.77930f, 187.32830f, 30.5250f, 187.10950f, 30.19070f)
                curveTo(186.89060f, 29.85650f, 186.54780f, 29.35510f, 186.0810f, 28.68660f)
                curveTo(185.52670f, 27.87280f, 185.0380f, 27.1390f, 184.6150f, 26.4850f)
                curveTo(184.19190f, 25.83110f, 183.76160f, 25.18440f, 183.32390f, 24.5450f)
                curveTo(182.88630f, 23.90550f, 182.41220f, 23.25890f, 181.90160f, 22.60490f)
                curveTo(181.39110f, 21.9510f, 180.78570f, 21.21710f, 180.08550f, 20.40330f)
                curveTo(179.5020f, 19.73480f, 178.86740f, 19.10990f, 178.18180f, 18.52860f)
                curveTo(177.49620f, 17.94730f, 176.77410f, 17.5840f, 176.01550f, 17.43870f)
                curveTo(175.46120f, 17.32240f, 175.03810f, 17.26430f, 174.74640f, 17.26430f)
                curveTo(174.48380f, 17.26430f, 174.14830f, 17.24980f, 173.73980f, 17.22070f)
                lineTo(173.739814f, 22.147139f)
                curveTo(173.73980f, 23.54220f, 173.75440f, 24.71930f, 173.78360f, 25.67850f)
                curveTo(173.81280f, 26.60850f, 173.84190f, 27.40780f, 173.87110f, 28.07630f)
                curveTo(173.90030f, 28.74480f, 174.06070f, 29.2970f, 174.35250f, 29.7330f)
                curveTo(174.58590f, 30.11080f, 175.03810f, 30.49590f, 175.70920f, 30.88830f)
                curveTo(176.38020f, 31.28070f, 177.31380f, 31.50590f, 178.510f, 31.5640f)
                lineTo(178.510001f, 32f)
                lineTo(164.505784f, 32f)
                lineTo(164.505784f, 31.5640327f)
                curveTo(165.23520f, 31.44780f, 165.93540f, 31.27340f, 166.60640f, 31.04090f)
                curveTo(167.27750f, 30.80840f, 167.80260f, 30.45960f, 168.18190f, 29.99460f)
                curveTo(168.32780f, 29.82020f, 168.44450f, 29.55860f, 168.5320f, 29.20980f)
                curveTo(168.61950f, 28.8610f, 168.68520f, 28.46870f, 168.72890f, 28.03270f)
                curveTo(168.77270f, 27.59670f, 168.80920f, 27.13170f, 168.83830f, 26.63760f)
                curveTo(168.86750f, 26.14350f, 168.88210f, 25.66390f, 168.88210f, 25.19890f)
                curveTo(168.88210f, 24.7920f, 168.88940f, 24.23980f, 168.9040f, 23.54220f)
                curveTo(168.91860f, 22.84470f, 168.93320f, 22.08170f, 168.94770f, 21.25340f)
                curveTo(168.96230f, 20.42510f, 168.96960f, 19.57490f, 168.96960f, 18.7030f)
                lineTo(168.969628f, 7.67302452f)
                curveTo(168.96960f, 7.14990f, 168.96230f, 6.61940f, 168.94770f, 6.08170f)
                curveTo(168.93320f, 5.5440f, 168.9040f, 5.03540f, 168.86020f, 4.55590f)
                curveTo(168.81650f, 4.07630f, 168.74350f, 3.64030f, 168.64140f, 3.2480f)
                curveTo(168.53930f, 2.85560f, 168.40070f, 2.57220f, 168.22570f, 2.39780f)
                curveTo(167.7880f, 1.93280f, 167.29930f, 1.62030f, 166.75960f, 1.46050f)
                curveTo(166.21980f, 1.30060f, 165.55610f, 1.16260f, 164.76840f, 1.04630f)
                lineTo(164.768363f, 0.610354223f)
                lineTo(166.562653f, 0.610354223f)
                curveTo(167.55460f, 0.61040f, 168.67790f, 0.60310f, 169.93240f, 0.58860f)
                curveTo(171.1870f, 0.5740f, 172.44150f, 0.56680f, 173.69610f, 0.56680f)
                lineTo(176.759473f, 0.566757493f)
                curveTo(178.71420f, 0.56680f, 180.45020f, 0.62490f, 181.96730f, 0.74110f)
                curveTo(183.48440f, 0.85740f, 184.87020f, 1.24980f, 186.12480f, 1.91830f)
                curveTo(187.40850f, 2.58670f, 188.44420f, 3.4950f, 189.2320f, 4.64310f)
                curveTo(190.01970f, 5.79110f, 190.41360f, 7.2370f, 190.41360f, 8.98090f)
                curveTo(190.41360f, 10.05630f, 190.21670f, 11.02270f, 189.82280f, 11.88010f)
                curveTo(189.42890f, 12.73750f, 188.90380f, 13.49320f, 188.24730f, 14.14710f)
                curveTo(187.59090f, 14.80110f, 186.8250f, 15.36060f, 185.94970f, 15.82560f)
                curveTo(185.07450f, 16.29060f, 184.15550f, 16.6830f, 183.19270f, 17.00270f)
                lineTo(183.192661f, 17.133515f)
                curveTo(183.68860f, 17.39510f, 184.170f, 17.77290f, 184.63680f, 18.2670f)
                curveTo(185.10370f, 18.76110f, 185.55590f, 19.31330f, 185.99350f, 19.92370f)
                curveTo(186.75210f, 21.02820f, 187.57630f, 22.21980f, 188.46610f, 23.49860f)
                curveTo(189.3560f, 24.77750f, 190.1510f, 25.9110f, 190.85120f, 26.89920f)
                curveTo(191.93070f, 28.41050f, 192.9810f, 29.5150f, 194.00220f, 30.21250f)
                curveTo(195.02330f, 30.91010f, 196.21950f, 31.36060f, 197.59070f, 31.5640f)
                lineTo(197.590746f, 32f)
                close()
                moveTo(185.380819f, 8.71934605f)
                curveTo(185.38080f, 7.26610f, 185.01610f, 5.97280f, 184.28670f, 4.83920f)
                curveTo(183.55740f, 3.70570f, 182.49250f, 2.80470f, 181.0920f, 2.13620f)
                curveTo(180.39180f, 1.81650f, 179.65510f, 1.5840f, 178.8820f, 1.43870f)
                curveTo(178.10880f, 1.29340f, 177.29920f, 1.22070f, 176.45310f, 1.22070f)
                curveTo(175.84040f, 1.22070f, 175.34450f, 1.23520f, 174.96520f, 1.26430f)
                curveTo(174.58590f, 1.29340f, 174.29410f, 1.32240f, 174.08990f, 1.35150f)
                curveTo(174.06070f, 1.52590f, 174.03160f, 1.67850f, 174.00240f, 1.80930f)
                curveTo(173.97320f, 1.94010f, 173.95130f, 2.10720f, 173.93670f, 2.31060f)
                curveTo(173.92220f, 2.51410f, 173.90760f, 2.79750f, 173.8930f, 3.16080f)
                curveTo(173.87840f, 3.52410f, 173.85650f, 4.040f, 173.82730f, 4.70840f)
                curveTo(173.79820f, 5.72570f, 173.77630f, 6.87370f, 173.76170f, 8.15260f)
                curveTo(173.74710f, 9.43140f, 173.73980f, 10.66670f, 173.73980f, 11.85830f)
                lineTo(173.739814f, 16.653951f)
                curveTo(174.9360f, 16.6540f, 176.0520f, 16.60310f, 177.08770f, 16.50140f)
                curveTo(178.12340f, 16.39960f, 179.12270f, 16.18890f, 180.08550f, 15.86920f)
                curveTo(181.01910f, 15.57860f, 181.82140f, 15.18620f, 182.49240f, 14.69210f)
                curveTo(183.16350f, 14.1980f, 183.71050f, 13.63850f, 184.13360f, 13.01360f)
                curveTo(184.55660f, 12.38870f, 184.87020f, 11.7130f, 185.07450f, 10.98640f)
                curveTo(185.27870f, 10.25980f, 185.38080f, 9.50410f, 185.38080f, 8.71930f)
                close()
                moveTo(256f, 23.6730245f)
                lineTo(253.811841f, 32f)
                lineTo(227.028777f, 32f)
                lineTo(227.028777f, 31.5640327f)
                curveTo(227.75820f, 31.44780f, 228.43650f, 31.25890f, 229.06380f, 30.99730f)
                curveTo(229.6910f, 30.73570f, 230.20890f, 30.37240f, 230.61740f, 29.90740f)
                curveTo(230.76320f, 29.7330f, 230.87990f, 29.47870f, 230.96750f, 29.14440f)
                curveTo(231.0550f, 28.81020f, 231.13520f, 28.42510f, 231.20820f, 27.98910f)
                curveTo(231.28110f, 27.55310f, 231.33220f, 27.09540f, 231.36130f, 26.61580f)
                curveTo(231.39050f, 26.13620f, 231.40510f, 25.66390f, 231.40510f, 25.19890f)
                curveTo(231.40510f, 24.7920f, 231.41240f, 24.23980f, 231.4270f, 23.54220f)
                curveTo(231.44160f, 22.84470f, 231.45620f, 22.08170f, 231.47070f, 21.25340f)
                curveTo(231.48530f, 20.42510f, 231.49260f, 19.57490f, 231.49260f, 18.7030f)
                curveTo(231.49260f, 17.8020f, 231.49260f, 16.95910f, 231.49260f, 16.17440f)
                lineTo(231.492621f, 7.67302452f)
                curveTo(231.49260f, 7.14990f, 231.48530f, 6.61220f, 231.47070f, 6.05990f)
                curveTo(231.45620f, 5.50770f, 231.4270f, 4.98460f, 231.38320f, 4.49050f)
                curveTo(231.33940f, 3.99640f, 231.26650f, 3.55310f, 231.16440f, 3.16080f)
                curveTo(231.06230f, 2.76840f, 230.92370f, 2.4850f, 230.74860f, 2.31060f)
                curveTo(230.3110f, 1.84560f, 229.84420f, 1.54770f, 229.34820f, 1.41690f)
                curveTo(228.85220f, 1.28610f, 228.21040f, 1.16260f, 227.42260f, 1.04630f)
                lineTo(227.422645f, 0.610354223f)
                lineTo(251.579919f, 0.610354223f)
                lineTo(254.555815f, 7.80381471f)
                lineTo(254.118183f, 8.02179837f)
                curveTo(251.7550f, 5.08630f, 249.53760f, 3.10990f, 247.46620f, 2.09260f)
                curveTo(246.97020f, 1.86010f, 246.05120f, 1.65670f, 244.70910f, 1.48230f)
                curveTo(243.3670f, 1.30790f, 241.73320f, 1.22070f, 239.80760f, 1.22070f)
                curveTo(239.19490f, 1.22070f, 238.56040f, 1.24250f, 237.90390f, 1.28610f)
                curveTo(237.24750f, 1.32970f, 236.77340f, 1.38060f, 236.48160f, 1.43870f)
                curveTo(236.42330f, 1.90370f, 236.37220f, 2.86280f, 236.32850f, 4.31610f)
                curveTo(236.28470f, 5.76930f, 236.26280f, 7.4550f, 236.26280f, 9.37330f)
                curveTo(236.26280f, 10.47780f, 236.26280f, 11.40780f, 236.26280f, 12.16350f)
                curveTo(236.26280f, 12.91920f, 236.27010f, 13.53680f, 236.28470f, 14.01630f)
                curveTo(236.29930f, 14.49590f, 236.30660f, 14.86650f, 236.30660f, 15.12810f)
                curveTo(236.30660f, 15.36060f, 236.30660f, 15.52040f, 236.30660f, 15.60760f)
                curveTo(236.45240f, 15.60760f, 236.74420f, 15.60760f, 237.18180f, 15.60760f)
                curveTo(237.61950f, 15.60760f, 238.12270f, 15.59310f, 238.69170f, 15.5640f)
                curveTo(239.26060f, 15.5350f, 239.82950f, 15.49860f, 240.39840f, 15.4550f)
                curveTo(240.96740f, 15.41140f, 241.44140f, 15.3460f, 241.82070f, 15.25890f)
                curveTo(242.6960f, 15.05540f, 243.42540f, 14.77930f, 244.00890f, 14.43050f)
                curveTo(244.59240f, 14.08170f, 245.06650f, 13.66030f, 245.43120f, 13.16620f)
                curveTo(245.79590f, 12.67210f, 246.08760f, 12.09810f, 246.30650f, 11.44410f)
                curveTo(246.52530f, 10.79020f, 246.72220f, 10.05630f, 246.89730f, 9.24250f)
                lineTo(247.422417f, 9.24250681f)
                lineTo(247.422417f, 22.9754768f)
                lineTo(246.897259f, 22.9754768f)
                curveTo(246.66390f, 22.04540f, 246.30650f, 21.08630f, 245.82510f, 20.09810f)
                curveTo(245.34370f, 19.10990f, 244.84040f, 18.36880f, 244.31520f, 17.87470f)
                curveTo(243.84840f, 17.46780f, 243.28680f, 17.13350f, 242.63030f, 16.87190f)
                curveTo(241.97390f, 16.61040f, 241.2080f, 16.45050f, 240.33280f, 16.39240f)
                curveTo(239.89510f, 16.36330f, 239.53050f, 16.34150f, 239.23870f, 16.3270f)
                curveTo(238.94690f, 16.31240f, 238.65520f, 16.30520f, 238.36340f, 16.30520f)
                curveTo(238.10090f, 16.30520f, 237.80910f, 16.30520f, 237.48820f, 16.30520f)
                curveTo(237.19640f, 16.30520f, 236.81710f, 16.30520f, 236.35030f, 16.30520f)
                curveTo(236.2920f, 16.71210f, 236.26280f, 17.30790f, 236.26280f, 18.09260f)
                curveTo(236.26280f, 18.87740f, 236.26280f, 19.69120f, 236.26280f, 20.53410f)
                curveTo(236.26280f, 20.91190f, 236.26280f, 21.34790f, 236.26280f, 21.8420f)
                curveTo(236.26280f, 22.33610f, 236.26280f, 22.83020f, 236.26280f, 23.32430f)
                curveTo(236.26280f, 23.78930f, 236.27010f, 24.23250f, 236.28470f, 24.6540f)
                curveTo(236.29930f, 25.07540f, 236.30660f, 25.43140f, 236.30660f, 25.72210f)
                curveTo(236.33570f, 26.65210f, 236.38680f, 27.53130f, 236.45970f, 28.35970f)
                curveTo(236.53270f, 29.1880f, 236.70040f, 29.80560f, 236.9630f, 30.21250f)
                curveTo(237.22560f, 30.61940f, 237.85290f, 30.91010f, 238.84480f, 31.08450f)
                curveTo(239.83680f, 31.25890f, 240.91630f, 31.3460f, 242.08330f, 31.3460f)
                curveTo(242.57930f, 31.3460f, 243.21390f, 31.33150f, 243.9870f, 31.30250f)
                curveTo(244.76020f, 31.27340f, 245.56980f, 31.20070f, 246.41590f, 31.08450f)
                curveTo(247.34950f, 30.96820f, 248.21010f, 30.7430f, 248.99790f, 30.40870f)
                curveTo(249.78560f, 30.07450f, 250.54420f, 29.60220f, 251.27360f, 28.99180f)
                curveTo(252.0030f, 28.38150f, 252.71050f, 27.63310f, 253.39610f, 26.74660f)
                curveTo(254.08170f, 25.86010f, 254.78920f, 24.7920f, 255.51860f, 23.54220f)
                lineTo(256f, 23.6730245f)
                close()
            }
        }.build()
    ),

    // Manually converted using Inkscape from official logo
    GALLERY_NUCLEUS(
        ImageVector.Builder(
            name = "Gallery Nucleus",
            defaultWidth = 16.dp,
            defaultHeight = (67.435997 / 70.028999 * 16).dp,
            viewportWidth = 70.028999f,
            viewportHeight = 67.435997f
        ).apply {
            group(
                translationX = -279.126f,
                translationY = -357.061f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(339.658f, 357.061f)
                    curveToRelative(-2.5380f, 00f, -4.9210f, 0.9880f, -6.7160f, 2.7810f)
                    curveToRelative(-1.7930f, 1.7950f, -2.7850f, 4.1770f, -2.7850f, 6.7150f)
                    curveToRelative(00f, 1.490f, 0.750f, 3.3170f, 0.120f, 4.80f)
                    curveToRelative(-0.2950f, 0.6940f, -1.0060f, 1.6820f, -1.7470f, 1.9540f)
                    curveToRelative(-0.9470f, 0.3470f, -1.7890f, -0.2560f, -2.4920f, -0.8050f)
                    curveToRelative(-4.9250f, -3.8460f, -11.1270f, -6.140f, -17.8430f, -6.140f)
                    curveToRelative(-7.7630f, 00f, -15.0640f, 3.0250f, -20.5520f, 8.5150f)
                    curveToRelative(-5.4890f, 5.4890f, -8.5170f, 12.7860f, -8.5170f, 20.5530f)
                    curveToRelative(00f, 7.7620f, 3.0270f, 15.0640f, 8.5170f, 20.5490f)
                    curveToRelative(5.4870f, 5.4940f, 12.7890f, 8.5140f, 20.5520f, 8.5140f)
                    curveToRelative(16.0290f, 00f, 29.0650f, -13.0370f, 29.0650f, -29.0630f)
                    curveToRelative(00f, -3.8030f, -0.750f, -7.5960f, -2.2110f, -11.1040f)
                    curveToRelative(-0.3560f, -0.8530f, -0.750f, -1.6880f, -1.1870f, -2.5070f)
                    curveToRelative(-0.3540f, -0.6730f, -1.0540f, -1.4560f, -1.1910f, -2.2090f)
                    curveToRelative(-0.2820f, -1.5670f, 1.0290f, -2.850f, 2.1070f, -3.6940f)
                    curveToRelative(1.2830f, -1.0070f, 3.4050f, 0.1370f, 4.8790f, 0.1370f)
                    curveToRelative(5.2360f, 00f, 9.4980f, -4.2620f, 9.4980f, -9.4980f)
                    curveToRelative(0.0010f, -5.2380f, -4.260f, -9.4980f, -9.4970f, -9.4980f)
                    close()
                    moveToRelative(-17.178f, 50.567f)
                    curveToRelative(-0.6420f, 0.680f, -1.4010f, 1.0210f, -2.2870f, 1.0210f)
                    curveToRelative(-0.8430f, 00f, -1.5830f, -0.310f, -2.2270f, -0.9240f)
                    curveToRelative(-0.640f, -0.6140f, -0.9610f, -1.3860f, -0.9610f, -2.320f)
                    verticalLineToRelative(-12.393f)
                    curveToRelative(00f, -2.6290f, -0.8820f, -4.8640f, -2.6340f, -6.7070f)
                    curveToRelative(-1.7590f, -1.8460f, -3.8750f, -2.7670f, -6.3480f, -2.7670f)
                    curveToRelative(-2.4420f, 00f, -4.5310f, 0.9210f, -6.2860f, 2.7670f)
                    curveToRelative(-1.7580f, 1.8430f, -2.6410f, 4.0780f, -2.6410f, 6.7070f)
                    curveToRelative(00f, -0.0420f, 00f, 4.0050f, 00f, 12.1420f)
                    curveToRelative(00f, 0.9320f, -0.3220f, 1.7490f, -0.9610f, 2.4440f)
                    curveToRelative(-0.640f, 0.6980f, -1.4090f, 1.0510f, -2.2910f, 1.0510f)
                    curveToRelative(-0.8430f, 00f, -1.5840f, -0.310f, -2.2270f, -0.9240f)
                    curveToRelative(-0.6380f, -0.6140f, -0.960f, -1.4110f, -0.960f, -2.3910f)
                    verticalLineToRelative(-12.427f)
                    curveToRelative(00f, -4.4580f, 1.4880f, -8.2670f, 4.4660f, -11.4340f)
                    curveToRelative(2.9780f, -3.1660f, 6.5980f, -4.7450f, 10.8660f, -4.7450f)
                    curveToRelative(4.2230f, 00f, 7.850f, 1.5890f, 10.8650f, 4.7720f)
                    curveToRelative(3.0160f, 3.1880f, 4.5290f, 7.0110f, 4.5290f, 11.4740f)
                    curveToRelative(00f, -0.0450f, 0.0180f, 4.0340f, 0.0580f, 12.2310f)
                    curveToRelative(0.0020f, 0.9410f, -0.3150f, 1.7420f, -0.9610f, 2.4230f)
                    close()
                }
            }
        }.build()
    ),

    // https://gamejolt.com/about
    GAME_JOLT(
        ImageVector.Builder(
            name = "Game Jolt",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 64f,
            viewportHeight = 64f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF000000)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(16f, 0f)
                horizontalLineToRelative(48f)
                moveTo(16f, 1f)
                horizontalLineToRelative(48f)
                moveTo(16f, 2f)
                horizontalLineToRelative(48f)
                moveTo(16f, 3f)
                horizontalLineToRelative(48f)
                moveTo(16f, 4f)
                horizontalLineToRelative(44f)
                moveTo(16f, 5f)
                horizontalLineToRelative(44f)
                moveTo(16f, 6f)
                horizontalLineToRelative(44f)
                moveTo(16f, 7f)
                horizontalLineToRelative(44f)
                moveTo(12f, 8f)
                horizontalLineToRelative(8f)
                moveTo(48f, 8f)
                horizontalLineToRelative(8f)
                moveTo(12f, 9f)
                horizontalLineToRelative(8f)
                moveTo(48f, 9f)
                horizontalLineToRelative(8f)
                moveTo(12f, 10f)
                horizontalLineToRelative(8f)
                moveTo(48f, 10f)
                horizontalLineToRelative(8f)
                moveTo(12f, 11f)
                horizontalLineToRelative(8f)
                moveTo(48f, 11f)
                horizontalLineToRelative(8f)
                moveTo(12f, 12f)
                horizontalLineToRelative(8f)
                moveTo(44f, 12f)
                horizontalLineToRelative(8f)
                moveTo(12f, 13f)
                horizontalLineToRelative(8f)
                moveTo(44f, 13f)
                horizontalLineToRelative(8f)
                moveTo(12f, 14f)
                horizontalLineToRelative(8f)
                moveTo(44f, 14f)
                horizontalLineToRelative(8f)
                moveTo(12f, 15f)
                horizontalLineToRelative(8f)
                moveTo(44f, 15f)
                horizontalLineToRelative(8f)
                moveTo(8f, 16f)
                horizontalLineToRelative(8f)
                moveTo(40f, 16f)
                horizontalLineToRelative(8f)
                moveTo(8f, 17f)
                horizontalLineToRelative(8f)
                moveTo(40f, 17f)
                horizontalLineToRelative(8f)
                moveTo(8f, 18f)
                horizontalLineToRelative(8f)
                moveTo(40f, 18f)
                horizontalLineToRelative(8f)
                moveTo(8f, 19f)
                horizontalLineToRelative(8f)
                moveTo(40f, 19f)
                horizontalLineToRelative(8f)
                moveTo(8f, 20f)
                horizontalLineToRelative(8f)
                moveTo(36f, 20f)
                horizontalLineToRelative(24f)
                moveTo(8f, 21f)
                horizontalLineToRelative(8f)
                moveTo(36f, 21f)
                horizontalLineToRelative(24f)
                moveTo(8f, 22f)
                horizontalLineToRelative(8f)
                moveTo(36f, 22f)
                horizontalLineToRelative(24f)
                moveTo(8f, 23f)
                horizontalLineToRelative(8f)
                moveTo(36f, 23f)
                horizontalLineToRelative(24f)
                moveTo(4f, 24f)
                horizontalLineToRelative(8f)
                moveTo(16f, 24f)
                horizontalLineToRelative(4f)
                moveTo(24f, 24f)
                horizontalLineToRelative(4f)
                moveTo(32f, 24f)
                horizontalLineToRelative(24f)
                moveTo(4f, 25f)
                horizontalLineToRelative(8f)
                moveTo(16f, 25f)
                horizontalLineToRelative(4f)
                moveTo(24f, 25f)
                horizontalLineToRelative(4f)
                moveTo(32f, 25f)
                horizontalLineToRelative(24f)
                moveTo(4f, 26f)
                horizontalLineToRelative(8f)
                moveTo(16f, 26f)
                horizontalLineToRelative(4f)
                moveTo(24f, 26f)
                horizontalLineToRelative(4f)
                moveTo(32f, 26f)
                horizontalLineToRelative(24f)
                moveTo(4f, 27f)
                horizontalLineToRelative(8f)
                moveTo(16f, 27f)
                horizontalLineToRelative(4f)
                moveTo(24f, 27f)
                horizontalLineToRelative(4f)
                moveTo(32f, 27f)
                horizontalLineToRelative(24f)
                moveTo(4f, 28f)
                horizontalLineToRelative(12f)
                moveTo(20f, 28f)
                horizontalLineToRelative(4f)
                moveTo(28f, 28f)
                horizontalLineToRelative(4f)
                moveTo(36f, 28f)
                horizontalLineToRelative(4f)
                moveTo(44f, 28f)
                horizontalLineToRelative(8f)
                moveTo(4f, 29f)
                horizontalLineToRelative(12f)
                moveTo(20f, 29f)
                horizontalLineToRelative(4f)
                moveTo(28f, 29f)
                horizontalLineToRelative(4f)
                moveTo(36f, 29f)
                horizontalLineToRelative(4f)
                moveTo(44f, 29f)
                horizontalLineToRelative(8f)
                moveTo(4f, 30f)
                horizontalLineToRelative(12f)
                moveTo(20f, 30f)
                horizontalLineToRelative(4f)
                moveTo(28f, 30f)
                horizontalLineToRelative(4f)
                moveTo(36f, 30f)
                horizontalLineToRelative(4f)
                moveTo(44f, 30f)
                horizontalLineToRelative(8f)
                moveTo(4f, 31f)
                horizontalLineToRelative(12f)
                moveTo(20f, 31f)
                horizontalLineToRelative(4f)
                moveTo(28f, 31f)
                horizontalLineToRelative(4f)
                moveTo(36f, 31f)
                horizontalLineToRelative(4f)
                moveTo(44f, 31f)
                horizontalLineToRelative(8f)
                moveTo(0f, 32f)
                horizontalLineToRelative(48f)
                moveTo(0f, 33f)
                horizontalLineToRelative(48f)
                moveTo(0f, 34f)
                horizontalLineToRelative(48f)
                moveTo(0f, 35f)
                horizontalLineToRelative(48f)
                moveTo(0f, 36f)
                horizontalLineToRelative(44f)
                moveTo(0f, 37f)
                horizontalLineToRelative(44f)
                moveTo(0f, 38f)
                horizontalLineToRelative(44f)
                moveTo(0f, 39f)
                horizontalLineToRelative(44f)
                moveTo(24f, 40f)
                horizontalLineToRelative(16f)
                moveTo(24f, 41f)
                horizontalLineToRelative(16f)
                moveTo(24f, 42f)
                horizontalLineToRelative(16f)
                moveTo(24f, 43f)
                horizontalLineToRelative(16f)
                moveTo(24f, 44f)
                horizontalLineToRelative(12f)
                moveTo(24f, 45f)
                horizontalLineToRelative(12f)
                moveTo(24f, 46f)
                horizontalLineToRelative(12f)
                moveTo(24f, 47f)
                horizontalLineToRelative(12f)
                moveTo(20f, 48f)
                horizontalLineToRelative(12f)
                moveTo(20f, 49f)
                horizontalLineToRelative(12f)
                moveTo(20f, 50f)
                horizontalLineToRelative(12f)
                moveTo(20f, 51f)
                horizontalLineToRelative(12f)
                moveTo(20f, 52f)
                horizontalLineToRelative(8f)
                moveTo(20f, 53f)
                horizontalLineToRelative(8f)
                moveTo(20f, 54f)
                horizontalLineToRelative(8f)
                moveTo(20f, 55f)
                horizontalLineToRelative(8f)
                moveTo(16f, 56f)
                horizontalLineToRelative(8f)
                moveTo(16f, 57f)
                horizontalLineToRelative(8f)
                moveTo(16f, 58f)
                horizontalLineToRelative(8f)
                moveTo(16f, 59f)
                horizontalLineToRelative(8f)
                moveTo(16f, 60f)
                horizontalLineToRelative(4f)
                moveTo(16f, 61f)
                horizontalLineToRelative(4f)
                moveTo(16f, 62f)
                horizontalLineToRelative(4f)
                moveTo(16f, 63f)
                horizontalLineToRelative(4f)
            }
        }.build()
    ),

    // Manually converted using Inkscape from official logo
    GUMROAD(
        ImageVector.Builder(
            name = "Gumroad",
            defaultWidth = 16.dp,
            defaultHeight = (80.186 / 80.777 * 16).dp,
            viewportWidth = 21.372f,
            viewportHeight = 21.216f
        ).apply {
            group(scaleX = 0.26458f, scaleY = 0.26458f) {
                materialPath(
                    fillAlpha = 0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(38.605f, 76.433f)
                    curveToRelative(20.8920f, 00f, 37.8280f, -16.9360f, 37.8280f, -37.8280f)
                    curveToRelative(00f, -20.890f, -16.9360f, -37.8270f, -37.8280f, -37.8270f)
                    curveTo(17.7150f, 0.7780f, 0.7790f, 17.7140f, 0.7790f, 38.6060f)
                    curveToRelative(00f, 20.890f, 16.9360f, 37.8270f, 37.8280f, 37.8270f)
                    close()
                }
            }
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(9.364f, 15.153f)
                curveToRelative(-2.870f, 00f, -4.5590f, -2.3020f, -4.5590f, -5.1660f)
                curveToRelative(00f, -2.9760f, 1.8580f, -5.390f, 5.4030f, -5.390f)
                curveToRelative(3.6590f, 00f, 4.8970f, 2.470f, 4.9530f, 3.8740f)
                horizontalLineToRelative(-2.645f)
                curveToRelative(-0.0560f, -0.7860f, -0.7320f, -1.9660f, -2.3640f, -1.9660f)
                curveToRelative(-1.7450f, 00f, -2.870f, 1.5170f, -2.870f, 3.370f)
                curveToRelative(00f, 1.8530f, 1.1250f, 3.370f, 2.870f, 3.370f)
                curveToRelative(1.5760f, 00f, 2.2520f, -1.2360f, 2.5330f, -2.4720f)
                horizontalLineToRelative(-2.533f)
                verticalLineToRelative(-1.01f)
                horizontalLineToRelative(5.315f)
                verticalLineToRelative(5.166f)
                horizontalLineToRelative(-2.332f)
                verticalLineToRelative(-3.257f)
                curveToRelative(-0.1690f, 1.180f, -0.90f, 3.4820f, -3.7710f, 3.4820f)
                close()
            }
            group(
                translationX = -32.42f,
                translationY = -9.51f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(50.664f, 13.746f)
                    arcToRelative(
                        9.963f,
                        9.963f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        1.974f,
                        5.968f
                    )
                    curveToRelative(00f, 5.5280f, -4.480f, 10.0090f, -10.0080f, 10.0090f)
                    arcToRelative(
                        9.96f,
                        9.96f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        -4.955f,
                        -1.314f
                    )
                    arcToRelative(
                        9.758f,
                        9.758f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        6.313f,
                        2.307f
                    )
                    arcToRelative(
                        9.8f,
                        9.8f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        9.8f,
                        -9.8f
                    )
                    arcToRelative(
                        9.771f,
                        9.771f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        -3.124f,
                        -7.17f
                    )
                    close()
                }
            }
        }.build()
    ),

    // Converted with Inkscape from https://help.inprnt.com/article/72-is-there-a-high-res-version-of-the-inprnt-logo
    INPRNT(
        ImageVector.Builder(
            name = "Inprnt",
            defaultWidth = (158.72078 / 65.269485 * 16).dp,
            defaultHeight = 16.dp,
            viewportWidth = 158.72078f,
            viewportHeight = 65.269485f
        ).apply {
            group(
                translationX = -11.423103f,
                translationY = -133.92772f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(14.860865f, 199.09897f)
                    curveToRelative(
                        -0.23640f,
                        -0.05060f,
                        -0.70680f,
                        -0.22890f,
                        -1.04520f,
                        -0.39620f
                    )
                    curveToRelative(-0.85750f, -0.42380f, -1.47160f, -1.03810f, -1.89460f, -1.8950f)
                    curveToRelative(-0.5450f, -1.10420f, -0.49690f, 1.82460f, -0.49690f, -30.24340f)
                    curveToRelative(00f, -32.0680f, -0.04810f, -29.13920f, 0.49690f, -30.24340f)
                    curveToRelative(0.42320f, -0.85730f, 1.03730f, -1.47140f, 1.89460f, -1.89450f)
                    curveToRelative(1.10250f, -0.54420f, -1.54320f, -0.49680f, 27.80870f, -0.49810f)
                    curveToRelative(23.32250f, -0.00070f, 26.32170f, 0.01020f, 26.75130f, 0.10f)
                    curveToRelative(1.26890f, 0.26530f, 2.41760f, 1.17010f, 2.9840f, 2.35030f)
                    curveToRelative(0.16470f, 0.34310f, 0.33160f, 0.78150f, 0.37110f, 0.97430f)
                    curveToRelative(0.10150f, 0.49620f, 0.10150f, 57.92680f, 00f, 58.4230f)
                    curveToRelative(-0.03940f, 0.19280f, -0.20640f, 0.63120f, -0.37110f, 0.97430f)
                    curveToRelative(-0.56640f, 1.18020f, -1.71510f, 2.0850f, -2.9840f, 2.35030f)
                    curveToRelative(-0.42980f, 0.08990f, -3.42170f, 0.10060f, -26.78430f, 0.09590f)
                    curveToRelative(
                        -22.20710f,
                        -0.0040f,
                        -26.36750f,
                        -0.01960f,
                        -26.73040f,
                        -0.09730f
                    )
                    close()
                    moveToRelative(17.839843f, -31.52608f)
                    lineToRelative(0.01673f, -13.80551f)
                    horizontalLineToRelative(-3.637529f)
                    horizontalLineToRelative(-3.637535f)
                    verticalLineToRelative(13.77795f)
                    curveToRelative(00f, 7.57790f, 0.02010f, 13.7980f, 0.04460f, 13.82260f)
                    curveToRelative(0.02460f, 0.02460f, 1.65390f, 0.0370f, 3.62080f, 0.02750f)
                    lineToRelative(3.576191f, -0.0171f)
                    close()
                    moveToRelative(12.35577f, 12.63162f)
                    curveToRelative(
                        -0.06620f,
                        -1.90110f,
                        -0.31460f,
                        -13.08860f,
                        -0.29110f,
                        -13.11030f
                    )
                    curveToRelative(0.01170f, -0.01070f, 1.75770f, 3.20630f, 3.88010f, 7.14890f)
                    lineToRelative(3.858923f, 7.16835f)
                    horizontalLineToRelative(3.465434f)
                    horizontalLineToRelative(3.465427f)
                    verticalLineToRelative(-13.82204f)
                    verticalLineToRelative(-13.82204f)
                    horizontalLineToRelative(-3.610577f)
                    horizontalLineToRelative(-3.610578f)
                    lineToRelative(0.04181f, 1.27308f)
                    curveToRelative(0.08010f, 2.4370f, 0.30780f, 12.96130f, 0.28110f, 12.98810f)
                    curveToRelative(-0.01480f, 0.01480f, -1.75390f, -3.18050f, -3.86450f, -7.10060f)
                    lineToRelative(-3.837601f, -7.12753f)
                    lineToRelative(-3.479672f, -0.0172f)
                    lineToRelative(-3.47968f, -0.0171f)
                    verticalLineToRelative(13.82262f)
                    verticalLineToRelative(13.82263f)
                    horizontalLineToRelative(3.611484f)
                    horizontalLineToRelative(3.611485f)
                    close()
                    moveToRelative(71.465862f, 18.00121f)
                    curveToRelative(-1.14460f, -0.5720f, -1.90290f, -2.26770f, -2.30510f, -5.15470f)
                    curveToRelative(
                        -0.10370f,
                        -0.74440f,
                        -0.11880f,
                        -1.80060f,
                        -0.1520f,
                        -10.61450f
                    )
                    curveToRelative(
                        -0.03680f,
                        -9.7650f,
                        -0.03720f,
                        -9.78880f,
                        -0.17770f,
                        -10.19880f
                    )
                    curveToRelative(
                        -0.27750f,
                        -0.80990f,
                        -0.76640f,
                        -1.40140f,
                        -1.40110f,
                        -1.69470f
                    )
                    curveToRelative(-0.28520f, -0.13180f, -0.43380f, -0.14450f, -1.9130f, -0.16370f)
                    lineToRelative(-1.60375f, -0.0208f)
                    verticalLineToRelative(13.9255f)
                    verticalLineToRelative(13.9255f)
                    horizontalLineToRelative(-3.37285f)
                    horizontalLineToRelative(-3.37284f)
                    verticalLineTo(166.2006f)
                    verticalLineTo(134.19166f)
                    lineToRelative(4.94353f, 0.0007f)
                    curveToRelative(2.86430f, 0.00050f, 5.27280f, 0.02960f, 5.72630f, 0.06940f)
                    curveToRelative(4.32490f, 0.37940f, 6.95180f, 3.2670f, 7.4480f, 8.18720f)
                    curveToRelative(0.04430f, 0.43910f, 0.06910f, 3.88970f, 0.06910f, 9.6140f)
                    curveToRelative(00f, 9.70390f, 0.0040f, 9.5890f, -0.36960f, 10.8780f)
                    curveToRelative(-0.46430f, 1.60230f, -1.5420f, 3.11690f, -2.85730f, 4.01550f)
                    lineToRelative(-0.46845f, 0.32007f)
                    lineToRelative(0.55818f, 0.39704f)
                    curveToRelative(1.65520f, 1.17740f, 2.6840f, 2.73410f, 3.15680f, 4.77670f)
                    curveToRelative(0.13510f, 0.58370f, 0.13860f, 0.7820f, 0.18120f, 10.21770f)
                    curveToRelative(0.03790f, 8.37530f, 0.05760f, 9.72730f, 0.15190f, 10.43090f)
                    curveToRelative(0.14990f, 1.11790f, 0.32960f, 1.90030f, 0.61720f, 2.68730f)
                    curveToRelative(0.23390f, 0.64020f, 0.24620f, 0.71510f, 0.26920f, 1.64620f)
                    lineToRelative(0.0241f, 0.97548f)
                    horizontalLineToRelative(-2.37358f)
                    horizontalLineToRelative(-2.37358f)
                    close()
                    moveToRelative(-4.12817f, -34.5768f)
                    curveToRelative(0.63520f, -0.28710f, 1.05840f, -0.90550f, 1.29060f, -1.8860f)
                    curveToRelative(
                        0.12480f,
                        -0.52720f,
                        0.11420f,
                        -18.45280f,
                        -0.01130f,
                        -18.95380f
                    )
                    curveToRelative(
                        -0.21380f,
                        -0.85410f,
                        -0.68490f,
                        -1.47180f,
                        -1.25190f,
                        -1.64170f
                    )
                    curveToRelative(
                        -0.16790f,
                        -0.05030f,
                        -0.82360f,
                        -0.07780f,
                        -1.85570f,
                        -0.07780f
                    )
                    horizontalLineToRelative(-1.59608f)
                    verticalLineToRelative(11.37947f)
                    verticalLineToRelative(11.37948f)
                    lineToRelative(1.53762f, -0.0209f)
                    curveToRelative(1.45510f, -0.01980f, 1.55640f, -0.02940f, 1.88680f, -0.17870f)
                    close()
                    moveToRelative(-33.449296f, 2.5678f)
                    verticalLineToRelative(-32.01281f)
                    lineToRelative(5.3734f, 0.0246f)
                    curveToRelative(4.67250f, 0.02140f, 5.45250f, 0.03910f, 5.980f, 0.13560f)
                    curveToRelative(1.77220f, 0.32420f, 3.19840f, 1.04570f, 4.2950f, 2.17280f)
                    curveToRelative(1.40280f, 1.44170f, 2.20020f, 3.37950f, 2.46840f, 5.99820f)
                    curveToRelative(0.0460f, 0.44910f, 0.07010f, 4.02410f, 0.07010f, 10.38350f)
                    curveToRelative(00f, 8.1710f, -0.01550f, 9.83130f, -0.09850f, 10.5410f)
                    curveToRelative(-0.47480f, 4.05920f, -2.32560f, 6.61910f, -5.52510f, 7.64220f)
                    curveToRelative(-1.03990f, 0.33250f, -1.78550f, 0.40970f, -3.95780f, 0.40970f)
                    horizontalLineToRelative(-1.925935f)
                    verticalLineToRelative(13.3591f)
                    verticalLineToRelative(13.3591f)
                    horizontalLineToRelative(-3.339776f)
                    horizontalLineToRelative(-3.339775f)
                    close()
                    moveToRelative(10.481701f, -1.28573f)
                    curveToRelative(0.55880f, -0.34720f, 0.88160f, -0.96080f, 1.02630f, -1.9510f)
                    curveToRelative(0.08670f, -0.59340f, 0.08380f, -19.1310f, -0.00310f, -19.74110f)
                    curveToRelative(
                        -0.11620f,
                        -0.81590f,
                        -0.30790f,
                        -1.27820f,
                        -0.68570f,
                        -1.65360f
                    )
                    curveToRelative(
                        -0.21170f,
                        -0.21040f,
                        -0.43820f,
                        -0.36480f,
                        -0.60920f,
                        -0.41530f
                    )
                    curveToRelative(
                        -0.18140f,
                        -0.05360f,
                        -0.81560f,
                        -0.08040f,
                        -1.90140f,
                        -0.08040f
                    )
                    horizontalLineToRelative(-1.629147f)
                    verticalLineToRelative(12.04042f)
                    verticalLineToRelative(12.04043f)
                    lineToRelative(1.741414f, -0.0205f)
                    lineToRelative(1.741416f, -0.0205f)
                    close()
                    moveToRelative(37.994645f, 1.28961f)
                    verticalLineToRelative(-32.00894f)
                    horizontalLineToRelative(3.36567f)
                    horizontalLineToRelative(3.36567f)
                    lineToRelative(0.0366f, 0.14881f)
                    curveToRelative(0.02020f, 0.08180f, 1.40750f, 7.34310f, 3.08290f, 16.13620f)
                    curveToRelative(2.79860f, 14.68750f, 3.1030f, 16.35430f, 3.7450f, 20.50090f)
                    curveToRelative(0.38430f, 2.48240f, 0.71150f, 4.52620f, 0.7270f, 4.54180f)
                    curveToRelative(0.01550f, 0.01550f, 0.02820f, -9.27680f, 0.02820f, -20.64970f)
                    verticalLineToRelative(-20.67796f)
                    horizontalLineToRelative(2.94296f)
                    horizontalLineToRelative(2.94298f)
                    verticalLineToRelative(32.00894f)
                    verticalLineToRelative(32.00894f)
                    horizontalLineToRelative(-2.66558f)
                    horizontalLineToRelative(-2.66558f)
                    lineToRelative(-3.71568f, -16.21941f)
                    lineToRelative(-3.71568f, -16.2194f)
                    lineToRelative(-0.77776f, -4.40935f)
                    lineToRelative(-0.77777f, -4.40936f)
                    lineToRelative(-0.0167f, 20.62876f)
                    lineToRelative(-0.0167f, 20.62876f)
                    horizontalLineToRelative(-2.94287f)
                    horizontalLineToRelative(-2.94287f)
                    close()
                    moveToRelative(30.42172f, 3.47205f)
                    verticalLineToRelative(-28.5369f)
                    horizontalLineToRelative(-2.97604f)
                    horizontalLineToRelative(-2.97604f)
                    verticalLineToRelative(-3.47204f)
                    verticalLineToRelative(-3.47205f)
                    horizontalLineToRelative(9.12651f)
                    horizontalLineToRelative(9.12652f)
                    verticalLineToRelative(3.47205f)
                    verticalLineToRelative(3.47204f)
                    horizontalLineToRelative(-2.84377f)
                    horizontalLineToRelative(-2.84376f)
                    verticalLineToRelative(28.5369f)
                    verticalLineToRelative(28.53689f)
                    horizontalLineToRelative(-3.30672f)
                    horizontalLineToRelative(-3.3067f)
                    close()
                }
            }
        }.build()
    ),

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/instagram.svg
    INSTAGRAM(
        ImageVector.Builder(
            name = "Instagram",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(8f, 0f)
                curveTo(5.829f, 0f, 5.556f, 0.01f, 4.703f, 0.048f)
                curveTo(3.85f, 0.088f, 3.269f, 0.222f, 2.76f, 0.42f)
                arcToRelative(
                    3.9f,
                    3.9f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.417f,
                    0.923f
                )
                arcTo(3.9f, 3.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.42f, 2.76f)
                curveTo(0.222f, 3.268f, 0.087f, 3.85f, 0.048f, 4.7f)
                curveTo(0.01f, 5.555f, 0f, 5.827f, 0f, 8.001f)
                curveToRelative(0f, 2.172f, 0.01f, 2.444f, 0.048f, 3.297f)
                curveToRelative(0.04f, 0.852f, 0.174f, 1.433f, 0.372f, 1.942f)
                curveToRelative(0.205f, 0.526f, 0.478f, 0.972f, 0.923f, 1.417f)
                curveToRelative(0.444f, 0.445f, 0.89f, 0.719f, 1.416f, 0.923f)
                curveToRelative(0.51f, 0.198f, 1.09f, 0.333f, 1.942f, 0.372f)
                curveTo(5.555f, 15.99f, 5.827f, 16f, 8f, 16f)
                reflectiveCurveToRelative(2.444f, -0.01f, 3.298f, -0.048f)
                curveToRelative(0.851f, -0.04f, 1.434f, -0.174f, 1.943f, -0.372f)
                arcToRelative(
                    3.9f,
                    3.9f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    1.416f,
                    -0.923f
                )
                curveToRelative(0.445f, -0.445f, 0.718f, -0.891f, 0.923f, -1.417f)
                curveToRelative(0.197f, -0.509f, 0.332f, -1.09f, 0.372f, -1.942f)
                curveTo(15.99f, 10.445f, 16f, 10.173f, 16f, 8f)
                reflectiveCurveToRelative(-0.01f, -2.445f, -0.048f, -3.299f)
                curveToRelative(-0.04f, -0.851f, -0.175f, -1.433f, -0.372f, -1.941f)
                arcToRelative(
                    3.9f,
                    3.9f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -0.923f,
                    -1.417f
                )
                arcTo(3.9f, 3.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.24f, 0.42f)
                curveToRelative(-0.51f, -0.198f, -1.092f, -0.333f, -1.943f, -0.372f)
                curveTo(10.443f, 0.01f, 10.172f, 0f, 7.998f, 0f)
                close()
                moveToRelative(-0.717f, 1.442f)
                horizontalLineToRelative(0.718f)
                curveToRelative(2.136f, 0f, 2.389f, 0.007f, 3.232f, 0.046f)
                curveToRelative(0.78f, 0.035f, 1.204f, 0.166f, 1.486f, 0.275f)
                curveToRelative(0.373f, 0.145f, 0.64f, 0.319f, 0.92f, 0.599f)
                reflectiveCurveToRelative(0.453f, 0.546f, 0.598f, 0.92f)
                curveToRelative(0.11f, 0.281f, 0.24f, 0.705f, 0.275f, 1.485f)
                curveToRelative(0.039f, 0.843f, 0.047f, 1.096f, 0.047f, 3.231f)
                reflectiveCurveToRelative(-0.008f, 2.389f, -0.047f, 3.232f)
                curveToRelative(-0.035f, 0.78f, -0.166f, 1.203f, -0.275f, 1.485f)
                arcToRelative(
                    2.5f,
                    2.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.599f,
                    0.919f
                )
                curveToRelative(-0.28f, 0.28f, -0.546f, 0.453f, -0.92f, 0.598f)
                curveToRelative(-0.28f, 0.11f, -0.704f, 0.24f, -1.485f, 0.276f)
                curveToRelative(-0.843f, 0.038f, -1.096f, 0.047f, -3.232f, 0.047f)
                reflectiveCurveToRelative(-2.39f, -0.009f, -3.233f, -0.047f)
                curveToRelative(-0.78f, -0.036f, -1.203f, -0.166f, -1.485f, -0.276f)
                arcToRelative(
                    2.5f,
                    2.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.92f,
                    -0.598f
                )
                arcToRelative(
                    2.5f,
                    2.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.6f,
                    -0.92f
                )
                curveToRelative(-0.109f, -0.281f, -0.24f, -0.705f, -0.275f, -1.485f)
                curveToRelative(-0.038f, -0.843f, -0.046f, -1.096f, -0.046f, -3.233f)
                reflectiveCurveToRelative(0.008f, -2.388f, 0.046f, -3.231f)
                curveToRelative(0.036f, -0.78f, 0.166f, -1.204f, 0.276f, -1.486f)
                curveToRelative(0.145f, -0.373f, 0.319f, -0.64f, 0.599f, -0.92f)
                reflectiveCurveToRelative(0.546f, -0.453f, 0.92f, -0.598f)
                curveToRelative(0.282f, -0.11f, 0.705f, -0.24f, 1.485f, -0.276f)
                curveToRelative(0.738f, -0.034f, 1.024f, -0.044f, 2.515f, -0.045f)
                close()
                moveToRelative(4.988f, 1.328f)
                arcToRelative(
                    0.96f,
                    0.96f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    0f,
                    1.92f
                )
                arcToRelative(
                    0.96f,
                    0.96f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -1.92f
                )
                moveToRelative(-4.27f, 1.122f)
                arcToRelative(
                    4.109f,
                    4.109f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    0f,
                    8.217f
                )
                arcToRelative(
                    4.109f,
                    4.109f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    -8.217f
                )
                moveToRelative(0f, 1.441f)
                arcToRelative(
                    2.667f,
                    2.667f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    0f,
                    5.334f
                )
                arcToRelative(
                    2.667f,
                    2.667f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    0f,
                    -5.334f
                )
            }
        }.build()
    ),

    // https://itch.io/press-kit
    ITCH_IO(
        ImageVector.Builder(
            name = "itch.io",
            defaultWidth = 16.dp,
            defaultHeight = (235.452 / 261.728 * 16).dp,
            viewportWidth = 245.371f,
            viewportHeight = 220.736f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(31.99f, 1.365f)
                curveTo(21.2870f, 7.720f, 0.20f, 31.9450f, 00f, 38.2980f)
                verticalLineToRelative(10.516f)
                curveTo(00f, 62.1440f, 12.460f, 73.860f, 23.7730f, 73.860f)
                curveToRelative(13.5840f, 00f, 24.9020f, -11.2580f, 24.9030f, -24.620f)
                curveToRelative(00f, 13.3620f, 10.930f, 24.620f, 24.5150f, 24.620f)
                curveToRelative(13.5860f, 00f, 24.1650f, -11.2580f, 24.1650f, -24.620f)
                curveToRelative(00f, 13.3620f, 11.6220f, 24.620f, 25.2070f, 24.620f)
                horizontalLineToRelative(0.246f)
                curveToRelative(13.5860f, 00f, 25.2080f, -11.2580f, 25.2080f, -24.620f)
                curveToRelative(00f, 13.3620f, 10.580f, 24.620f, 24.1640f, 24.620f)
                curveToRelative(13.5850f, 00f, 24.5150f, -11.2580f, 24.5150f, -24.620f)
                curveToRelative(00f, 13.3620f, 11.320f, 24.620f, 24.9030f, 24.620f)
                curveToRelative(11.3130f, 00f, 23.7730f, -11.7140f, 23.7730f, -25.0460f)
                verticalLineTo(38.298f)
                curveToRelative(-0.20f, -6.3540f, -21.2870f, -30.580f, -31.9880f, -36.9330f)
                curveTo(180.1180f, 0.1970f, 157.0560f, -0.0050f, 122.6850f, 00f)
                curveToRelative(-34.370f, 0.0030f, -81.2280f, 0.540f, -90.6970f, 1.3650f)
                close()
                moveToRelative(65.194f, 66.217f)
                arcToRelative(
                    28.025f,
                    28.025f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -4.78f,
                    6.155f
                )
                curveToRelative(-5.1280f, 5.0140f, -12.1570f, 8.1220f, -19.9060f, 8.1220f)
                arcToRelative(
                    28.482f,
                    28.482f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -19.948f,
                    -8.126f
                )
                curveToRelative(-1.8580f, -1.820f, -3.270f, -3.7660f, -4.5630f, -6.0320f)
                lineToRelative(-0.006f, 0.004f)
                curveToRelative(-1.2920f, 2.270f, -3.0920f, 4.2150f, -4.9540f, 6.0370f)
                arcToRelative(
                    28.5f,
                    28.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -19.948f,
                    8.12f
                )
                curveToRelative(-0.9340f, 00f, -1.9060f, -0.2580f, -2.6920f, -0.5280f)
                curveToRelative(-1.0920f, 11.3720f, -1.5530f, 22.240f, -1.7160f, 30.1640f)
                lineToRelative(-0.002f, 0.045f)
                curveToRelative(-0.020f, 4.0240f, -0.040f, 7.3330f, -0.060f, 11.930f)
                curveToRelative(0.210f, 23.860f, -2.3630f, 77.3340f, 10.520f, 90.4730f)
                curveToRelative(19.9640f, 4.6550f, 56.70f, 6.7750f, 93.5550f, 6.7880f)
                horizontalLineToRelative(0.006f)
                curveToRelative(36.8540f, -0.0130f, 73.590f, -2.1330f, 93.5540f, -6.7880f)
                curveToRelative(12.8830f, -13.140f, 10.310f, -66.6140f, 10.520f, -90.4740f)
                curveToRelative(-0.0220f, -4.5960f, -0.040f, -7.9050f, -0.060f, -11.930f)
                lineToRelative(-0.003f, -0.045f)
                curveToRelative(-0.1620f, -7.9260f, -0.6230f, -18.7930f, -1.7150f, -30.1650f)
                curveToRelative(-0.7860f, 0.270f, -1.7570f, 0.5280f, -2.6920f, 0.5280f)
                arcToRelative(
                    28.5f,
                    28.5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -19.948f,
                    -8.12f
                )
                curveToRelative(-1.8620f, -1.8220f, -3.6620f, -3.7660f, -4.9550f, -6.0370f)
                lineToRelative(-0.006f, -0.004f)
                curveToRelative(-1.2940f, 2.2660f, -2.7050f, 4.2130f, -4.5630f, 6.0320f)
                arcToRelative(
                    28.48f,
                    28.48f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -19.947f,
                    8.125f
                )
                curveToRelative(-7.7480f, 00f, -14.7780f, -3.110f, -19.9060f, -8.1230f)
                arcToRelative(
                    28.025f,
                    28.025f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -4.78f,
                    -6.155f
                )
                arcToRelative(
                    27.99f,
                    27.99f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -4.736f,
                    6.155f
                )
                arcToRelative(
                    28.49f,
                    28.49f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -19.95f,
                    8.124f
                )
                curveToRelative(-0.270f, 00f, -0.540f, -0.0120f, -0.810f, -0.020f)
                horizontalLineToRelative(-0.007f)
                curveToRelative(-0.270f, 0.0080f, -0.540f, 0.020f, -0.8130f, 0.020f)
                arcToRelative(
                    28.49f,
                    28.49f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -19.95f,
                    -8.123f
                )
                arcToRelative(
                    27.992f,
                    27.992f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -4.736f,
                    -6.155f
                )
                close()
                moveToRelative(-20.486f, 26.49f)
                lineToRelative(-0.002f, 0.01f)
                horizontalLineToRelative(0.015f)
                curveToRelative(8.1130f, 0.0170f, 15.320f, 00f, 24.250f, 9.7460f)
                curveToRelative(7.0280f, -0.7370f, 14.3720f, -1.1050f, 21.7220f, -1.0940f)
                horizontalLineToRelative(0.006f)
                curveToRelative(7.350f, -0.010f, 14.6940f, 0.3570f, 21.7230f, 1.0940f)
                curveToRelative(8.930f, -9.7470f, 16.1370f, -9.730f, 24.250f, -9.7460f)
                horizontalLineToRelative(0.014f)
                lineToRelative(-0.002f, -0.01f)
                curveToRelative(3.8330f, 00f, 19.1660f, 00f, 29.850f, 30.0070f)
                lineTo(210f, 165.244f)
                curveToRelative(8.5040f, 30.6240f, -2.7230f, 31.3730f, -16.7270f, 31.40f)
                curveToRelative(-20.7680f, -0.7730f, -32.2670f, -15.8550f, -32.2670f, -30.9350f)
                curveToRelative(-11.4960f, 1.8840f, -24.9070f, 2.8260f, -38.3180f, 2.8270f)
                horizontalLineToRelative(-0.006f)
                curveToRelative(-13.4120f, 00f, -26.8230f, -0.9430f, -38.3180f, -2.8270f)
                curveToRelative(00f, 15.080f, -11.50f, 30.1620f, -32.2670f, 30.9350f)
                curveToRelative(-14.0040f, -0.0270f, -25.230f, -0.7750f, -16.7260f, -31.40f)
                lineTo(46.85f, 124.08f)
                curveTo(57.5340f, 94.0730f, 72.8670f, 94.0730f, 76.70f, 94.0730f)
                close()
                moveToRelative(45.985f, 23.582f)
                verticalLineToRelative(0.006f)
                curveToRelative(-0.020f, 0.020f, -21.8630f, 20.080f, -25.790f, 27.2150f)
                lineToRelative(14.304f, -0.573f)
                verticalLineToRelative(12.474f)
                curveToRelative(00f, 0.5840f, 5.740f, 0.3460f, 11.4860f, 0.080f)
                horizontalLineToRelative(0.006f)
                curveToRelative(5.7440f, 0.2660f, 11.4850f, 0.5040f, 11.4850f, -0.080f)
                verticalLineToRelative(-12.474f)
                lineToRelative(14.304f, 0.573f)
                curveToRelative(-3.9280f, -7.1350f, -25.790f, -27.2150f, -25.790f, -27.2150f)
                verticalLineToRelative(-0.006f)
                lineToRelative(-0.003f, 0.002f)
                close()
            }
        }.build()
    ),

    // https://more.ko-fi.com/brand-assets
    KO_FI(
        ImageVector.Builder(
            name = "Ko-Fi",
            defaultWidth = 16.dp,
            defaultHeight = (194f / 241 * 16).dp,
            viewportWidth = 241f,
            viewportHeight = 194f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(114.54492f, 16.158203f)
                curveToRelative(-23.6380f, 00f, -43.64630f, 0.2290f, -59.78910f, 2.27540f)
                curveToRelative(-21.36930f, 2.73060f, -39.55860f, 19.09510f, -39.55860f, 49.3340f)
                horizontalLineToRelative(-0.0059f)
                curveToRelative(00f, 30.68890f, 1.58910f, 53.65490f, 13.86720f, 74.33980f)
                curveToRelative(13.86820f, 23.6440f, 37.05750f, 36.59960f, 67.06840f, 36.59960f)
                horizontalLineToRelative(7.273437f)
                curveToRelative(36.8280f, 00f, 56.83490f, -19.55180f, 66.83790f, -35.00980f)
                curveToRelative(4.3210f, -6.8240f, 7.50090f, -13.63890f, 9.54690f, -20.46290f)
                curveToRelative(26.1460f, -2.2680f, 45.47070f, -23.86570f, 45.47070f, -50.24020f)
                verticalLineToRelative(-3.636719f)
                curveToRelative(00f, -28.42070f, -18.63930f, -48.20050f, -50.69730f, -51.38090f)
                curveToRelative(-13.640f, -1.36210f, -23.18570f, -1.81840f, -60.01370f, -1.81840f)
                close()
                moveToRelative(-0.008f, 17.050781f)
                curveToRelative(36.8340f, 00f, 44.56550f, 0.45570f, 57.52150f, 1.58980f)
                curveToRelative(22.9660f, 2.72410f, 36.14840f, 13.86850f, 36.14840f, 35.00980f)
                verticalLineToRelative(3.408203f)
                curveToRelative(00f, 18.87340f, -15.6860f, 33.64650f, -33.8750f, 33.64650f)
                horizontalLineToRelative(-8.18555f)
                lineToRelative(-1.36328f, 5.6836f)
                curveToRelative(-1.8180f, 8.870f, -5.22480f, 15.91420f, -9.08980f, 22.28120f)
                curveToRelative(-7.9570f, 12.50f, -22.2820f, 26.82420f, -51.83590f, 26.82420f)
                horizontalLineTo(96.8125f)
                curveToRelative(-22.50990f, 00f, -42.28940f, -7.72790f, -53.19920f, -27.96290f)
                curveTo(34.0660f, 116.18450f, 32.24610f, 97.31710f, 32.24610f, 67.99020f)
                horizontalLineToRelative(-0.0059f)
                curveToRelative(00f, -19.09480f, 7.50810f, -29.77720f, 26.3750f, -32.50780f)
                curveToRelative(13.4120f, -2.04640f, 31.14410f, -2.27340f, 55.92190f, -2.27340f)
                close()
            }
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(166.158f, 83.6801f)
                curveToRelative(00f, 2.73060f, 2.0460f, 4.77710f, 5.6830f, 4.77710f)
                curveToRelative(11.5940f, 00f, 17.9610f, -6.59530f, 17.9610f, -17.50490f)
                curveToRelative(00f, -10.90960f, -6.3670f, -17.73280f, -17.9610f, -17.73280f)
                curveToRelative(-3.6370f, 00f, -5.6830f, 2.04620f, -5.6830f, 4.77680f)
                verticalLineToRelative(25.6903f)
                close()
            }
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(54.5321f, 82.3198f)
                curveToRelative(00f, 13.41220f, 7.50110f, 25.00620f, 17.04860f, 34.10420f)
                curveToRelative(6.36710f, 6.1380f, 16.37080f, 12.5060f, 23.18780f, 16.5980f)
                curveToRelative(2.04620f, 1.1350f, 4.09260f, 1.8190f, 6.36750f, 1.8190f)
                curveToRelative(2.730f, 00f, 4.9980f, -0.6840f, 6.8230f, -1.8190f)
                curveToRelative(6.8230f, -4.0920f, 16.820f, -10.460f, 22.960f, -16.5980f)
                curveToRelative(9.7750f, -9.0920f, 17.2760f, -20.68570f, 17.2760f, -34.10420f)
                curveToRelative(00f, -14.55250f, -10.9090f, -27.50830f, -26.5960f, -27.50830f)
                curveToRelative(-9.3190f, 00f, -15.6870f, 4.77670f, -20.4630f, 11.36570f)
                curveTo(96.81470f, 59.5820f, 90.22590f, 54.81150f, 80.90010f, 54.81150f)
                curveToRelative(-15.91460f, 00f, -26.37450f, 12.95580f, -26.37450f, 27.50830f)
            }
        }.build()
    ),

    // https://uxwing.com/linktree-logo-icon/
    LINKTREE(
        ImageVector.Builder(
            name = "Linktree",
            defaultWidth = 16.dp,
            defaultHeight = (512.238 / 417 * 16).dp,
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
    ),

    // https://www.patreon.com/brand
    PATREON(
        ImageVector.Builder(
            name = "Patreon",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
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
    ),

    // Converted using Inkscape from https://redbubble.design/brand/logo
    REDBUBBLE(
        ImageVector.Builder(
            name = "Redbubble",
            defaultWidth = 16.dp,
            defaultHeight = (189.25365 / 189.2473 * 16).dp,
            viewportWidth = 189.2473f,
            viewportHeight = 189.25365f
        ).apply {
            group(
                translationX = -9.9706181f,
                translationY = -55.864252f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(101.3513f, 245.06438f)
                    curveTo(75.57670f, 244.20610f, 51.33520f, 232.86270f, 34.07360f, 213.58290f)
                    curveTo(20.97370f, 198.95130f, 12.79450f, 180.75470f, 10.57860f, 161.31250f)
                    curveTo(9.85920f, 155.00020f, 9.77610f, 148.25280f, 10.34060f, 141.97020f)
                    curveTo(12.4860f, 118.09120f, 23.58410f, 96.00710f, 41.49930f, 79.96730f)
                    curveTo(56.13150f, 66.86670f, 74.32650f, 58.68830f, 93.76970f, 56.47220f)
                    curveToRelative(6.31220f, -0.71940f, 13.05970f, -0.80250f, 19.34230f, -0.2380f)
                    curveToRelative(23.98880f, 2.15530f, 46.1710f, 13.3510f, 62.22560f, 31.40620f)
                    curveToRelative(12.96390f, 14.57930f, 21.070f, 32.69980f, 23.27240f, 52.0230f)
                    curveToRelative(0.71940f, 6.31220f, 0.80250f, 13.05970f, 0.2380f, 19.34220f)
                    curveToRelative(
                        -2.16520f,
                        24.09970f,
                        -13.44970f,
                        46.36440f,
                        -31.64330f,
                        62.43380f
                    )
                    curveToRelative(
                        -18.16530f,
                        16.04440f,
                        -41.55150f,
                        24.43420f,
                        -65.85330f,
                        23.62490f
                    )
                    close()
                    moveToRelative(3.57222f, -60.51124f)
                    curveToRelative(0.67410f, -0.12420f, 1.29650f, -0.52860f, 1.6870f, -1.09620f)
                    curveToRelative(0.56980f, -0.82830f, 0.57170f, -2.00610f, 0.0050f, -2.81220f)
                    curveToRelative(-0.09920f, -0.14090f, -4.10420f, -5.39350f, -8.90f, -11.67230f)
                    lineTo(88.9958f, 157.5564f)
                    lineTo(89.179728f, 157.473f)
                    curveToRelative(2.70850f, -1.22830f, 4.3070f, -2.30270f, 6.06940f, -4.07930f)
                    curveToRelative(2.73740f, -2.75950f, 4.44160f, -6.05550f, 5.29470f, -10.24040f)
                    curveToRelative(0.88330f, -4.33320f, 0.51360f, -9.6160f, -0.94780f, -13.54550f)
                    curveToRelative(
                        -1.02880f,
                        -2.76630f,
                        -2.37980f,
                        -4.89370f,
                        -4.37650f,
                        -6.89170f
                    )
                    curveToRelative(
                        -3.98280f,
                        -3.98540f,
                        -9.54650f,
                        -6.12280f,
                        -16.85790f,
                        -6.47620f
                    )
                    curveToRelative(
                        -1.74280f,
                        -0.08420f,
                        -26.31530f,
                        -0.07810f,
                        -26.71840f,
                        0.0070f
                    )
                    curveToRelative(-1.00770f, 0.21190f, -1.81850f, 1.0250f, -1.9990f, 2.00440f)
                    curveToRelative(-0.04020f, 0.21790f, -0.0550f, 10.90680f, -0.04490f, 32.35540f)
                    lineToRelative(0.01503f, 32.03001f)
                    lineToRelative(0.13747f, 0.33011f)
                    curveToRelative(0.3620f, 0.86940f, 1.15390f, 1.4910f, 2.05110f, 1.61010f)
                    curveToRelative(0.09760f, 0.01290f, 12.01080f, 0.02750f, 26.47380f, 0.03240f)
                    curveToRelative(18.80820f, 0.0060f, 26.39630f, -0.0090f, 26.64730f, -0.05580f)
                    close()
                    moveToRelative(39.33122f, -0.0452f)
                    curveToRelative(4.97170f, -0.44180f, 8.31220f, -1.26120f, 11.60420f, -2.84620f)
                    curveToRelative(2.10950f, -1.01570f, 3.86320f, -2.24410f, 5.40330f, -3.78510f)
                    curveToRelative(2.87620f, -2.87770f, 4.4940f, -6.32540f, 5.06650f, -10.79710f)
                    curveToRelative(0.14960f, -1.16850f, 0.14840f, -4.40450f, -0.0020f, -5.67850f)
                    curveToRelative(-0.42910f, -3.63330f, -1.36230f, -6.3950f, -2.96810f, -8.78390f)
                    curveToRelative(-1.5090f, -2.24480f, -3.36790f, -3.85280f, -5.86330f, -5.07220f)
                    lineToRelative(-0.8992f, -0.43938f)
                    lineToRelative(0.42031f, -0.51228f)
                    curveToRelative(2.32880f, -2.83830f, 3.53260f, -6.74190f, 3.53260f, -11.45510f)
                    curveToRelative(00f, -5.62310f, -1.63960f, -10.26260f, -4.75220f, -13.44730f)
                    curveToRelative(
                        -2.03830f,
                        -2.08550f,
                        -4.78820f,
                        -3.61430f,
                        -8.09430f,
                        -4.49980f
                    )
                    curveToRelative(-1.46780f, -0.39310f, -3.31210f, -0.6990f, -5.42920f, -0.90030f)
                    curveToRelative(
                        -0.99770f,
                        -0.09480f,
                        -26.28330f,
                        -0.14540f,
                        -26.78030f,
                        -0.05350f
                    )
                    curveToRelative(-1.06360f, 0.19660f, -1.8890f, 1.00590f, -2.07960f, 2.03910f)
                    curveToRelative(-0.08720f, 0.47280f, -0.09240f, 63.75180f, -0.0060f, 64.21860f)
                    curveToRelative(0.13560f, 0.72630f, 0.65110f, 1.44030f, 1.28970f, 1.78630f)
                    curveToRelative(0.6440f, 0.34890f, -0.18330f, 0.33160f, 14.91720f, 0.31250f)
                    curveToRelative(9.87450f, -0.01240f, 14.09230f, -0.03720f, 14.63980f, -0.08580f)
                    close()
                }
            }
        }.build()
    ),

    // Converted using Inkscape from https://www.shopify.com/brand-assets
    SHOPIFY(
        ImageVector.Builder(
            name = "Shopify",
            defaultWidth = 16.dp,
            defaultHeight = (165.276 / 145.797 * 16).dp,
            viewportWidth = 38.576f,
            viewportHeight = 43.729f
        ).apply {
            group(
                translationX = -114.025f,
                translationY = -84.448f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(127.265f, 125.876f)
                    curveToRelative(-7.270f, -1.2610f, -13.2270f, -2.3040f, -13.240f, -2.3170f)
                    curveToRelative(-0.0510f, -0.0510f, 2.9650f, -22.9890f, 3.3620f, -25.5650f)
                    curveToRelative(0.1430f, -0.9320f, 0.1720f, -1.0520f, 0.2910f, -1.220f)
                    curveToRelative(0.1760f, -0.2460f, 0.6090f, -0.4050f, 3.6440f, -1.3410f)
                    lineToRelative(1.67f, -0.515f)
                    lineToRelative(0.072f, -0.42f)
                    curveToRelative(0.2150f, -1.2580f, 0.7840f, -2.9080f, 1.4780f, -4.2930f)
                    curveToRelative(1.2910f, -2.5760f, 2.9470f, -4.4190f, 4.720f, -5.2540f)
                    curveToRelative(1.3060f, -0.6150f, 2.7460f, -0.670f, 3.790f, -0.1440f)
                    curveToRelative(0.3240f, 0.1640f, 0.7070f, 0.4530f, 0.930f, 0.7050f)
                    lineToRelative(0.149f, 0.166f)
                    lineToRelative(0.508f, 0.016f)
                    curveToRelative(1.0640f, 0.0340f, 1.910f, 0.4160f, 2.710f, 1.2240f)
                    curveToRelative(0.6960f, 0.7020f, 1.2180f, 1.620f, 1.690f, 2.9740f)
                    curveToRelative(0.0110f, 0.0320f, 0.2430f, -0.020f, 0.6870f, -0.1510f)
                    arcToRelative(
                        17.4f,
                        17.4f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        0.808f,
                        -0.227f
                    )
                    lineToRelative(0.137f, -0.027f)
                    verticalLineToRelative(38.691f)
                    lineToRelative(-0.095f, -0.004f)
                    curveToRelative(-0.0530f, -0.0020f, -6.0430f, -1.0360f, -13.3110f, -2.2980f)
                    close()
                    moveToRelative(1.337f, -5.364f)
                    curveToRelative(1.2120f, -0.2070f, 2.240f, -0.7190f, 3.0890f, -1.540f)
                    curveToRelative(1.1730f, -1.1340f, 1.8170f, -2.6130f, 1.950f, -4.4810f)
                    curveToRelative(0.1540f, -2.1460f, -0.5020f, -3.90f, -1.9730f, -5.2760f)
                    curveToRelative(-0.5180f, -0.4850f, -1.0620f, -0.8920f, -2.0770f, -1.5540f)
                    curveToRelative(-1.6490f, -1.0750f, -1.9960f, -1.3670f, -2.2530f, -1.8890f)
                    curveToRelative(-0.1340f, -0.2730f, -0.1380f, -0.2940f, -0.1220f, -0.720f)
                    curveToRelative(0.0150f, -0.4070f, 0.0280f, -0.4650f, 0.1740f, -0.760f)
                    curveToRelative(0.4470f, -0.9060f, 1.4620f, -1.3050f, 3.1630f, -1.2480f)
                    arcToRelative(
                        6.76f,
                        6.76f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        2.116f,
                        0.381f
                    )
                    curveToRelative(0.2440f, 0.0840f, 0.4470f, 0.1480f, 0.4510f, 0.1440f)
                    arcToRelative(
                        594.32f,
                        594.32f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        1.328f,
                        -5.038f
                    )
                    curveToRelative(00f, -0.0370f, -0.6350f, -0.2230f, -1.0160f, -0.2990f)
                    curveToRelative(-1.6020f, -0.3170f, -3.8150f, -0.1170f, -5.5240f, 0.50f)
                    curveToRelative(-1.5550f, 0.5620f, -2.9320f, 1.5870f, -3.8420f, 2.8620f)
                    curveToRelative(-0.5670f, 0.7930f, -1.0510f, 1.940f, -1.2620f, 2.9850f)
                    curveToRelative(-0.1180f, 0.5850f, -0.1620f, 1.9070f, -0.0830f, 2.4910f)
                    curveToRelative(0.1170f, 0.8650f, 0.3890f, 1.6230f, 0.8220f, 2.2910f)
                    curveToRelative(0.3280f, 0.5060f, 1.1490f, 1.3180f, 1.840f, 1.820f)
                    curveToRelative(1.1350f, 0.8240f, 1.3910f, 1.0170f, 1.6470f, 1.240f)
                    curveToRelative(0.720f, 0.630f, 1.1120f, 1.5140f, 0.9910f, 2.2390f)
                    curveToRelative(-0.0960f, 0.5820f, -0.4630f, 1.0830f, -0.960f, 1.310f)
                    curveToRelative(-0.4180f, 0.1910f, -1.2320f, 0.2050f, -1.8980f, 0.0330f)
                    curveToRelative(-0.8160f, -0.210f, -2.1550f, -0.8650f, -2.9110f, -1.4240f)
                    curveToRelative(-0.0670f, -0.050f, -0.1280f, -0.0820f, -0.1360f, -0.0720f)
                    curveToRelative(-0.0080f, 0.010f, -0.2150f, 0.8750f, -0.4620f, 1.9230f)
                    lineToRelative(-0.449f, 1.905f)
                    lineToRelative(0.092f, 0.1f)
                    curveToRelative(0.1540f, 0.1680f, 0.7520f, 0.6220f, 1.130f, 0.8560f)
                    curveToRelative(1.2140f, 0.7540f, 2.5950f, 1.1870f, 4.2320f, 1.3260f)
                    curveToRelative(0.3920f, 0.0330f, 1.4770f, -0.0250f, 1.9430f, -0.1050f)
                    close()
                    moveTo(126.31f, 93.86f)
                    curveToRelative(0.9710f, -0.2950f, 1.780f, -0.5480f, 1.7980f, -0.5630f)
                    curveToRelative(0.0170f, -0.0140f, 0.0970f, -0.2920f, 0.1770f, -0.6170f)
                    curveToRelative(0.5480f, -2.220f, 1.7170f, -4.4140f, 2.950f, -5.5320f)
                    curveToRelative(0.3620f, -0.3290f, 0.9450f, -0.7630f, 1.170f, -0.8710f)
                    curveToRelative(0.0760f, -0.0370f, 0.1320f, -0.0870f, 0.1240f, -0.1120f)
                    curveToRelative(-0.0080f, -0.0240f, -0.1750f, -0.10f, -0.3710f, -0.1670f)
                    curveToRelative(-0.6370f, -0.2190f, -1.3440f, -0.1580f, -2.1720f, 0.1870f)
                    curveToRelative(-1.0140f, 0.4220f, -2.240f, 1.560f, -3.210f, 2.9830f)
                    curveToRelative(-0.9350f, 1.3720f, -1.7750f, 3.1750f, -2.1730f, 4.6690f)
                    lineToRelative(-0.131f, 0.486f)
                    curveToRelative(-0.0120f, 0.040f, 00f, 0.0740f, 0.0250f, 0.0740f)
                    reflectiveCurveToRelative(0.84f, -0.242f, 1.812f, -0.537f)
                    close()
                    moveToRelative(5.618f, -1.706f)
                    curveToRelative(1.1410f, -0.350f, 2.120f, -0.6570f, 2.1760f, -0.680f)
                    lineToRelative(0.102f, -0.045f)
                    lineToRelative(-0.03f, -0.664f)
                    arcToRelative(
                        12.405f,
                        12.405f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        -0.243f,
                        -2.04f
                    )
                    curveToRelative(-0.0910f, -0.420f, -0.3820f, -1.2950f, -0.4540f, -1.3680f)
                    curveToRelative(-0.0760f, -0.0750f, -0.6460f, 0.2980f, -1.2040f, 0.7890f)
                    curveToRelative(-0.8910f, 0.7820f, -1.7880f, 2.2960f, -2.340f, 3.9470f)
                    curveToRelative(-0.1260f, 0.3780f, -0.230f, 0.70f, -0.230f, 0.7160f)
                    curveToRelative(00f, 0.0150f, 0.0340f, 0.0170f, 0.0750f, 0.0050f)
                    lineToRelative(2.148f, -0.66f)
                    close()
                    moveToRelative(4.354f, -1.33f)
                    curveToRelative(0.330f, -0.1020f, 0.7840f, -0.2370f, 1.010f, -0.30f)
                    lineToRelative(0.41f, -0.117f)
                    lineToRelative(-0.108f, -0.302f)
                    curveToRelative(-0.540f, -1.5250f, -1.3980f, -2.5360f, -2.410f, -2.840f)
                    curveToRelative(-0.1930f, -0.0590f, -0.1950f, -0.0580f, -0.1720f, 0.0330f)
                    curveToRelative(0.0130f, 0.050f, 0.090f, 0.3250f, 0.1710f, 0.6080f)
                    curveToRelative(0.230f, 0.8030f, 0.4050f, 2.0320f, 0.4070f, 2.840f)
                    curveToRelative(00f, 0.1610f, 0.0180f, 0.2640f, 0.0460f, 0.2640f)
                    arcToRelative(
                        9f,
                        9f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = false,
                        0.646f,
                        -0.186f
                    )
                    close()
                    moveToRelative(5.277f, 18.174f)
                    verticalLineTo(89.942f)
                    lineToRelative(0.328f, 0.297f)
                    curveToRelative(0.180f, 0.1640f, 0.4520f, 0.4250f, 0.6030f, 0.580f)
                    curveToRelative(0.1520f, 0.1540f, 0.5930f, 0.5850f, 0.980f, 0.9560f)
                    lineToRelative(0.705f, 0.675f)
                    lineToRelative(1.707f, 0.043f)
                    curveToRelative(1.8250f, 0.0470f, 1.7880f, 0.0420f, 1.9050f, 0.2620f)
                    curveToRelative(0.0440f, 0.080f, 0.0270f, -0.030f, 2.8730f, 19.2090f)
                    curveToRelative(1.0730f, 7.2530f, 1.9460f, 13.2240f, 1.940f, 13.270f)
                    curveToRelative(-0.0120f, 0.0720f, -0.6420f, 0.2380f, -5.4320f, 1.4270f)
                    lineToRelative(-5.514f, 1.368f)
                    lineToRelative(-0.095f, 0.023f)
                    close()
                }
            }
        }.build()
    ),

    // Manually converted using Inkscape from the official logo
    STORENVY(
        ImageVector.Builder(
            name = "Storenvy",
            defaultWidth = 16.dp,
            defaultHeight = (141.25276 / 160.19859 * 16).dp,
            viewportWidth = 160.19859f,
            viewportHeight = 141.25276f
        ).apply {
            group(
                translationX = -20.954644f,
                translationY = -81.119143f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(99.269458f, 222.23535f)
                    curveToRelative(-2.0040f, -0.35910f, -3.54580f, -1.06760f, -5.21270f, -2.39550f)
                    curveToRelative(
                        -0.52540f,
                        -0.41860f,
                        -16.22460f,
                        -16.04550f,
                        -34.88710f,
                        -34.72640f
                    )
                    curveToRelative(
                        -28.1120f,
                        -28.13980f,
                        -34.07450f,
                        -34.18260f,
                        -34.76410f,
                        -35.23220f
                    )
                    curveToRelative(-1.51860f, -2.31150f, -2.57820f, -4.87870f, -3.22310f, -7.8090f)
                    curveToRelative(-0.30990f, -1.4080f, -0.3010f, -17.05650f, 0.01050f, -18.62940f)
                    curveToRelative(0.52870f, -2.6690f, 1.82250f, -5.7230f, 3.36980f, -7.95440f)
                    curveToRelative(1.030f, -1.48540f, 29.59210f, -30.02250f, 30.96140f, -30.93430f)
                    curveToRelative(2.0590f, -1.37110f, 4.85620f, -2.57080f, 7.42660f, -3.18530f)
                    curveToRelative(1.05430f, -0.2520f, 1.99250f, -0.27530f, 9.37170f, -0.23270f)
                    curveToRelative(9.18550f, 0.05310f, 9.82280f, 0.11490f, 12.89040f, 1.25170f)
                    curveToRelative(3.62370f, 1.34280f, 4.67870f, 2.14560f, 10.71480f, 8.15320f)
                    lineToRelative(5.214614f, 5.189988f)
                    lineToRelative(5.13505f, -5.11613f)
                    curveToRelative(5.35250f, -5.33280f, 6.25020f, -6.07770f, 8.79720f, -7.30070f)
                    curveToRelative(0.6130f, -0.29430f, 1.76230f, -0.75160f, 2.55380f, -1.01630f)
                    curveToRelative(2.67110f, -0.89290f, 3.51030f, -0.9610f, 12.41340f, -1.00760f)
                    lineToRelative(8.12051f, -0.04243f)
                    lineToRelative(1.88504f, 0.51012f)
                    curveToRelative(2.03590f, 0.55090f, 4.83230f, 1.80610f, 6.49660f, 2.9160f)
                    curveToRelative(1.42440f, 0.950f, 29.95610f, 29.43990f, 31.0120f, 30.96660f)
                    curveToRelative(1.44240f, 2.08560f, 2.54130f, 4.56070f, 3.21680f, 7.24480f)
                    curveToRelative(0.37750f, 1.50040f, 0.38060f, 1.58110f, 0.38060f, 9.95160f)
                    curveToRelative(00f, 6.33150f, -0.05170f, 8.67750f, -0.20710f, 9.39430f)
                    curveToRelative(-0.63120f, 2.91140f, -1.94450f, 5.99950f, -3.47230f, 8.16510f)
                    curveToRelative(
                        -0.82330f,
                        1.1670f,
                        -68.22510f,
                        68.53860f,
                        -69.76990f,
                        69.73870f
                    )
                    curveToRelative(-2.22320f, 1.72710f, -5.69360f, 2.59110f, -8.43440f, 2.10010f)
                    close()
                    moveToRelative(3.556202f, -6.30143f)
                    curveToRelative(0.72310f, -0.23770f, 2.80740f, -2.27770f, 26.57920f, -26.01590f)
                    curveToRelative(
                        14.1870f,
                        -14.16690f,
                        25.86620f,
                        -25.78380f,
                        25.95380f,
                        -25.81530f
                    )
                    curveToRelative(0.08760f, -0.03150f, 0.74810f, -0.66890f, 1.46790f, -1.41650f)
                    lineToRelative(1.30865f, -1.35921f)
                    lineToRelative(-35.54216f, -35.52051f)
                    curveTo(103.04480f, 106.27020f, 86.72840f, 90.07120f, 86.33440f, 89.80860f)
                    curveTo(85.26350f, 89.09530f, 83.85970f, 88.42530f, 82.43330f, 87.9470f)
                    curveTo(80.64480f, 87.34730f, 77.88880f, 87.18710f, 70.41180f, 87.24840f)
                    lineToRelative(-6.130189f, 0.05025f)
                    lineToRelative(-1.596883f, 0.563722f)
                    curveToRelative(-2.20810f, 0.77950f, -3.45520f, 1.51910f, -5.19280f, 3.07960f)
                    curveToRelative(-0.81950f, 0.73610f, -7.44180f, 7.31090f, -14.71610f, 14.61080f)
                    curveToRelative(
                        -11.50590f,
                        11.54630f,
                        -13.29720f,
                        13.40710f,
                        -13.77280f,
                        14.30750f
                    )
                    curveToRelative(-0.82250f, 1.55710f, -1.25630f, 2.62930f, -1.60470f, 3.96650f)
                    curveToRelative(-0.29360f, 1.12680f, -0.31390f, 1.72190f, -0.29960f, 8.82020f)
                    curveToRelative(0.00840f, 4.18650f, 0.08110f, 7.95870f, 0.16150f, 8.38270f)
                    curveToRelative(0.2310f, 1.21780f, 0.76610f, 2.67610f, 1.55130f, 4.22720f)
                    lineToRelative(0.725407f, 1.43303f)
                    lineToRelative(34.489121f, 34.49496f)
                    curveToRelative(
                        38.02110f,
                        38.02750f,
                        34.78930f,
                        34.96830f,
                        36.96860f,
                        34.99450f
                    )
                    curveToRelative(0.57550f, 0.0070f, 1.39950f, -0.10350f, 1.83110f, -0.24530f)
                    close()
                    moveTo(55.917939f, 126.38529f)
                    curveToRelative(
                        -2.12730f,
                        -0.32790f,
                        -3.74440f,
                        -1.17050f,
                        -5.34890f,
                        -2.78690f
                    )
                    curveToRelative(
                        -3.69480f,
                        -3.72220f,
                        -3.65830f,
                        -9.40420f,
                        0.08430f,
                        -13.14690f
                    )
                    curveToRelative(3.78130f, -3.78130f, 9.46550f, -3.78070f, 13.23110f, 0.0010f)
                    curveToRelative(1.90830f, 1.91660f, 2.7810f, 4.02890f, 2.76840f, 6.70070f)
                    curveToRelative(-0.01870f, 3.95350f, -2.6620f, 7.56260f, -6.45890f, 8.81890f)
                    curveToRelative(-1.34850f, 0.44620f, -3.01570f, 0.60710f, -4.2760f, 0.41290f)
                    close()
                    moveToRelative(3.109218f, -2.9164f)
                    curveToRelative(0.72120f, -0.12270f, 1.8690f, -0.76520f, 2.61680f, -1.46470f)
                    curveToRelative(0.80260f, -0.75080f, 1.7110f, -2.08870f, 1.53660f, -2.26310f)
                    curveToRelative(-0.05180f, -0.05180f, 0.0190f, -0.26690f, 0.15730f, -0.4780f)
                    curveToRelative(0.13830f, -0.21110f, 0.21520f, -0.47850f, 0.17080f, -0.59420f)
                    curveToRelative(-0.04440f, -0.11570f, -0.03790f, -0.25320f, 0.01440f, -0.30560f)
                    curveToRelative(0.05240f, -0.05240f, 0.08030f, -0.57880f, 0.06210f, -1.170f)
                    curveToRelative(-0.08430f, -2.7360f, -1.33790f, -4.74630f, -3.67620f, -5.89580f)
                    curveToRelative(-0.79750f, -0.3920f, -1.04890f, -0.43340f, -2.63320f, -0.43340f)
                    curveToRelative(-1.5670f, 00f, -1.84740f, 0.0450f, -2.66190f, 0.42670f)
                    curveToRelative(-1.84540f, 0.86490f, -3.12350f, 2.50240f, -3.550f, 4.54810f)
                    curveToRelative(-0.34190f, 1.63980f, -0.13770f, 3.76850f, 0.3930f, 4.09650f)
                    curveToRelative(0.09440f, 0.05830f, 0.13660f, 0.16280f, 0.09370f, 0.23220f)
                    curveToRelative(-0.11270f, 0.18230f, 0.90380f, 1.5610f, 1.52950f, 2.07440f)
                    curveToRelative(0.5950f, 0.48830f, 1.56860f, 1.02630f, 1.56860f, 0.86690f)
                    curveToRelative(00f, -0.05670f, 0.1090f, -0.01270f, 0.24220f, 0.09780f)
                    curveToRelative(0.13320f, 0.11050f, 0.40190f, 0.2280f, 0.59710f, 0.2610f)
                    curveToRelative(0.19520f, 0.0330f, 0.67740f, 0.11440f, 1.07140f, 0.18080f)
                    curveToRelative(0.39410f, 0.06650f, 1.0390f, 0.06540f, 1.4330f, -0.0020f)
                    curveToRelative(0.39410f, -0.06770f, 0.85980f, -0.14760f, 1.0350f, -0.17740f)
                    close()
                    moveToRelative(114.005473f, 22.4883f)
                    curveToRelative(0.41690f, -0.68950f, 1.01730f, -1.9890f, 1.33440f, -2.88780f)
                    lineToRelative(0.57644f, -1.63405f)
                    verticalLineToRelative(-8.43897f)
                    curveToRelative(00f, -8.27810f, -0.0070f, -8.46020f, -0.35360f, -9.55350f)
                    curveToRelative(-0.4980f, -1.56970f, -1.46670f, -3.49680f, -2.32110f, -4.61750f)
                    curveToRelative(
                        -0.99470f,
                        -1.30490f,
                        -28.18220f,
                        -28.40850f,
                        -29.14280f,
                        -29.05290f
                    )
                    curveToRelative(-1.24160f, -0.83290f, -3.30670f, -1.7290f, -4.80450f, -2.08470f)
                    curveToRelative(-1.33130f, -0.31620f, -1.73860f, -0.32850f, -9.05630f, -0.2730f)
                    lineToRelative(-7.66225f, 0.05807f)
                    lineToRelative(-1.43303f, 0.440883f)
                    curveToRelative(-1.64320f, 0.50550f, -3.60370f, 1.44550f, -4.67590f, 2.24180f)
                    curveToRelative(-0.40580f, 0.30140f, -2.77960f, 2.59440f, -5.27510f, 5.09560f)
                    lineToRelative(-4.53732f, 4.547683f)
                    lineToRelative(28.50022f, 28.500561f)
                    lineToRelative(28.50022f, 28.50056f)
                    lineToRelative(4.7964f, -4.79447f)
                    curveToRelative(3.91270f, -3.91120f, 4.9360f, -5.02550f, 5.55430f, -6.04820f)
                    close()
                    moveToRelative(-30.49162f, -19.33313f)
                    curveToRelative(-1.58580f, -0.43620f, -2.78580f, -1.13990f, -4.00930f, -2.3510f)
                    curveToRelative(-0.63810f, -0.63170f, -1.14350f, -1.20230f, -1.12310f, -1.2680f)
                    curveToRelative(0.02050f, -0.06570f, -0.03360f, -0.11940f, -0.12010f, -0.11940f)
                    curveToRelative(-0.25630f, 00f, -0.98820f, -1.27640f, -1.36950f, -2.38840f)
                    curveToRelative(
                        -0.50520f,
                        -1.47330f,
                        -0.60660f,
                        -2.96820f,
                        -0.32630f,
                        -4.81020f
                    )
                    curveToRelative(0.31570f, -2.07440f, 0.89540f, -3.16570f, 2.56210f, -4.82320f)
                    curveToRelative(1.97880f, -1.96780f, 3.92190f, -2.78620f, 6.61520f, -2.78620f)
                    curveToRelative(2.54470f, 00f, 4.5990f, 0.81890f, 6.44760f, 2.57010f)
                    curveToRelative(1.81360f, 1.71810f, 2.79230f, 3.70120f, 3.03320f, 6.14560f)
                    curveToRelative(0.1240f, 1.25860f, -0.19440f, 3.18630f, -0.73430f, 4.44590f)
                    curveToRelative(-0.67930f, 1.58470f, -2.29980f, 3.43620f, -3.86350f, 4.41420f)
                    curveToRelative(-1.52550f, 0.95410f, -5.29950f, 1.46910f, -7.11210f, 0.97060f)
                    close()
                    moveToRelative(3.34056f, -2.94174f)
                    curveToRelative(1.53910f, -0.29190f, 2.63920f, -0.91160f, 3.75240f, -2.1140f)
                    curveToRelative(0.56350f, -0.60860f, 0.99590f, -1.15270f, 0.96110f, -1.20920f)
                    curveToRelative(-0.03490f, -0.05650f, 0.06340f, -0.36340f, 0.21840f, -0.68210f)
                    curveToRelative(0.21080f, -0.43330f, 0.28490f, -0.99420f, 0.2940f, -2.22370f)
                    curveToRelative(0.0070f, -0.90430f, -0.05080f, -1.72020f, -0.12780f, -1.81290f)
                    curveToRelative(-0.0770f, -0.09280f, -0.17690f, -0.37230f, -0.2220f, -0.62110f)
                    curveToRelative(-0.04510f, -0.24880f, -0.20380f, -0.5870f, -0.35260f, -0.75140f)
                    curveToRelative(
                        -0.14880f,
                        -0.16440f,
                        -0.20990f,
                        -0.29890f,
                        -0.13580f,
                        -0.29890f
                    )
                    curveToRelative(0.07410f, 00f, -0.0040f, -0.10520f, -0.17440f, -0.23380f)
                    curveToRelative(-0.170f, -0.12860f, -0.24130f, -0.23610f, -0.15840f, -0.23880f)
                    curveToRelative(0.22480f, -0.0080f, -0.8970f, -1.11960f, -1.12930f, -1.11960f)
                    curveToRelative(-0.10980f, 00f, -0.27270f, -0.12540f, -0.3620f, -0.27860f)
                    curveToRelative(
                        -0.08930f,
                        -0.15330f,
                        -0.16450f,
                        -0.21610f,
                        -0.16710f,
                        -0.13970f
                    )
                    curveToRelative(-0.0020f, 0.07640f, -0.11960f, 0.04370f, -0.25970f, -0.07260f)
                    curveToRelative(
                        -0.14020f,
                        -0.11630f,
                        -0.35210f,
                        -0.22580f,
                        -0.47080f,
                        -0.24320f
                    )
                    curveToRelative(
                        -0.11870f,
                        -0.01740f,
                        -0.42550f,
                        -0.13630f,
                        -0.68180f,
                        -0.26410f
                    )
                    curveToRelative(-0.32430f, -0.16180f, -0.94350f, -0.22630f, -2.0370f, -0.21210f)
                    curveToRelative(-1.40090f, 0.0180f, -1.68320f, 0.07340f, -2.60610f, 0.51030f)
                    curveToRelative(-1.29760f, 0.61450f, -1.97820f, 1.18620f, -2.62750f, 2.20730f)
                    curveToRelative(-0.82240f, 1.29320f, -1.08220f, 2.29760f, -1.0050f, 3.88430f)
                    curveToRelative(0.08190f, 1.68230f, 0.55350f, 2.82240f, 1.65420f, 3.99920f)
                    curveToRelative(0.81630f, 0.87270f, 2.25360f, 1.67270f, 3.41140f, 1.89880f)
                    curveToRelative(0.99890f, 0.19510f, 1.27230f, 0.19710f, 2.2260f, 0.01620f)
                    close()
                }
            }
        }.build()
    ),

    // Converted using Inkscape from https://substack.com/brand
    SUBSTACK(
        ImageVector.Builder(
            name = "Substack",
            defaultWidth = 16.dp,
            defaultHeight = (211.66664 / 185.91388 * 16).dp,
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
    ),

    // Converted using Inkscape from https://www.threadless.com/about-us/
    THREADLESS(
        ImageVector.Builder(
            name = "Threadless",
            defaultWidth = (759.655 / 245.638 * 16).dp,
            defaultHeight = 16.dp,
            viewportWidth = 200.992f,
            viewportHeight = 64.992f
        ).apply {
            group(
                translationX = -4.554f,
                translationY = -37.961f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(21.335f, 102.757f)
                    curveToRelative(-0.540f, -0.1510f, -1.6980f, -0.6580f, -2.5760f, -1.1270f)
                    lineToRelative(-1.596f, -0.852f)
                    lineToRelative(-2.653f, 1.038f)
                    curveToRelative(-1.4590f, 0.5710f, -2.7130f, 1.0380f, -2.7870f, 1.0380f)
                    curveToRelative(-0.1520f, 00f, -0.3930f, -1.290f, -1.0320f, -5.5340f)
                    curveToRelative(-0.8030f, -5.3350f, -0.7640f, -11.2820f, 0.0980f, -14.90f)
                    lineToRelative(0.32f, -1.345f)
                    horizontalLineTo(9.726f)
                    curveToRelative(-2.4180f, 00f, -5.1710f, -1.1790f, -5.1710f, -2.2140f)
                    curveToRelative(00f, -0.8490f, 1.7370f, -1.970f, 4.8720f, -3.1430f)
                    curveToRelative(1.0060f, -0.3760f, 1.6430f, -0.7220f, 1.7720f, -0.9620f)
                    curveToRelative(0.2940f, -0.5440f, 0.4030f, -5.8120f, 0.1540f, -7.4120f)
                    curveToRelative(-0.1650f, -1.0610f, -0.160f, -1.3540f, 0.0230f, -1.4710f)
                    curveToRelative(0.50f, -0.3180f, 4.4040f, -1.9060f, 9.7550f, -3.9670f)
                    lineToRelative(5.62f, -2.164f)
                    lineToRelative(0.395f, -2.142f)
                    curveToRelative(0.4870f, -2.6370f, 0.8350f, -3.9770f, 1.5210f, -5.8610f)
                    lineToRelative(0.531f, -1.458f)
                    lineToRelative(8.407f, -3.303f)
                    lineToRelative(8.543f, -3.355f)
                    curveToRelative(0.0750f, -0.0280f, -0.130f, 0.7350f, -0.4560f, 1.6960f)
                    curveToRelative(-1.5670f, 4.6170f, -2.1640f, 9.590f, -3.0270f, 25.2240f)
                    curveToRelative(-0.2470f, 4.4670f, -0.5680f, 9.4880f, -0.7130f, 11.1570f)
                    curveToRelative(-0.2740f, 3.1470f, -0.9730f, 9.0510f, -1.0860f, 9.1750f)
                    curveToRelative(-0.0360f, 0.0390f, -0.8220f, -0.2950f, -1.7470f, -0.7420f)
                    curveToRelative(-2.8090f, -1.3580f, -5.1050f, -3.8210f, -6.2060f, -6.6560f)
                    curveToRelative(-1.0370f, -2.6720f, -1.070f, -5.7640f, -0.0920f, -8.8890f)
                    lineToRelative(0.4f, -1.278f)
                    lineToRelative(1.867f, -0.12f)
                    curveToRelative(1.240f, -0.0790f, 2.3470f, -0.2740f, 3.2960f, -0.580f)
                    curveToRelative(1.960f, -0.630f, 1.9630f, -0.6330f, 1.6970f, -1.4370f)
                    curveToRelative(-0.670f, -2.0280f, -3.5910f, -3.2640f, -5.9740f, -2.5240f)
                    curveToRelative(-0.4860f, 0.150f, -0.9040f, 0.2530f, -0.930f, 0.2280f)
                    curveToRelative(-0.0250f, -0.0250f, 0.0860f, -0.5240f, 0.2470f, -1.1090f)
                    curveToRelative(0.4070f, -1.4860f, 0.4030f, -4.5810f, -0.0090f, -5.4860f)
                    curveToRelative(-0.7290f, -1.6030f, -2.330f, -2.7050f, -4.2620f, -2.9340f)
                    curveToRelative(-0.8920f, -0.1060f, -0.9060f, -0.10f, -0.80f, 0.3230f)
                    curveToRelative(0.060f, 0.2380f, 0.1920f, 1.3240f, 0.2950f, 2.4150f)
                    curveToRelative(0.1890f, 2.0020f, 0.0530f, 4.4030f, -0.3550f, 6.2650f)
                    curveToRelative(-0.3280f, 1.4960f, -0.2480f, 1.4190f, -1.3910f, 1.3360f)
                    curveToRelative(-1.8760f, -0.1360f, -4.2840f, 0.7870f, -5.1010f, 1.9530f)
                    curveToRelative(-0.2350f, 0.3360f, -0.2320f, 0.4270f, 0.0240f, 0.8190f)
                    curveToRelative(0.7050f, 1.0760f, 2.7270f, 1.8330f, 4.8980f, 1.8330f)
                    curveToRelative(0.7680f, 00f, 1.3960f, 0.060f, 1.3950f, 0.1330f)
                    curveToRelative(00f, 0.0740f, -0.1750f, 0.9780f, -0.3890f, 2.0090f)
                    curveToRelative(-0.3340f, 1.6140f, -0.390f, 2.4820f, -0.40f, 6.2480f)
                    curveToRelative(-0.0090f, 2.7250f, 0.0760f, 5.0130f, 0.2230f, 6.070f)
                    curveToRelative(0.3430f, 2.4650f, 1.1330f, 7.2170f, 1.2140f, 7.3050f)
                    curveToRelative(0.0380f, 0.0420f, 0.7990f, -0.7020f, 1.690f, -1.6510f)
                    curveToRelative(0.8910f, -0.950f, 1.6920f, -1.7270f, 1.7790f, -1.7270f)
                    curveToRelative(0.0870f, 00f, 0.9040f, 0.490f, 1.8150f, 1.090f)
                    curveToRelative(2.8060f, 1.8450f, 4.3780f, 2.480f, 6.1350f, 2.480f)
                    curveToRelative(2.3750f, 00f, 5.3250f, -1.6670f, 6.820f, -3.8540f)
                    curveToRelative(1.4220f, -2.080f, 2.8260f, -5.9560f, 4.5870f, -12.6590f)
                    curveToRelative(1.6770f, -6.3860f, 2.1540f, -7.7650f, 2.6890f, -7.7650f)
                    curveToRelative(0.1470f, 00f, 0.3360f, 0.180f, 0.420f, 0.4010f)
                    curveToRelative(0.1970f, 0.520f, 1.2120f, 5.7660f, 2.2530f, 11.6490f)
                    curveToRelative(0.9180f, 5.1820f, 1.110f, 5.6980f, 2.5250f, 6.7790f)
                    curveToRelative(1.0180f, 0.7770f, 1.9880f, 1.0530f, 3.2920f, 0.9360f)
                    curveToRelative(1.940f, -0.1740f, 3.6350f, -1.2120f, 4.3840f, -2.6830f)
                    curveToRelative(0.610f, -1.1970f, 0.6840f, -2.70f, 0.450f, -9.1380f)
                    curveToRelative(-0.0960f, -2.6510f, -0.1440f, -4.8520f, -0.1070f, -4.8910f)
                    curveToRelative(0.0360f, -0.040f, 0.7390f, 0.110f, 1.560f, 0.330f)
                    lineToRelative(1.494f, 0.402f)
                    lineToRelative(0.119f, 2.609f)
                    curveToRelative(0.220f, 4.8330f, 0.9580f, 8.0590f, 2.3050f, 10.0620f)
                    curveToRelative(2.2430f, 3.3350f, 5.780f, 2.9860f, 10.5540f, -1.040f)
                    lineToRelative(1.053f, -0.888f)
                    lineToRelative(1.446f, 0.426f)
                    curveToRelative(1.2660f, 0.3730f, 1.7140f, 0.4160f, 3.590f, 0.350f)
                    curveToRelative(1.7140f, -0.0620f, 2.3780f, -0.1660f, 3.3280f, -0.5210f)
                    curveToRelative(1.4880f, -0.5570f, 3.4110f, -1.9160f, 4.0260f, -2.8440f)
                    curveToRelative(0.2580f, -0.390f, 0.5560f, -0.670f, 0.6630f, -0.6210f)
                    curveToRelative(2.070f, 0.9290f, 2.590f, 1.070f, 4.230f, 1.1420f)
                    curveToRelative(2.9630f, 0.130f, 4.9930f, -0.8310f, 6.2310f, -2.9530f)
                    lineToRelative(0.622f, -1.065f)
                    lineToRelative(0.672f, 0.762f)
                    curveToRelative(1.7720f, 2.0130f, 4.610f, 2.2620f, 7.0060f, 0.6130f)
                    curveToRelative(0.440f, -0.3020f, 1.0790f, -0.8370f, 1.4190f, -1.1870f)
                    lineToRelative(0.618f, -0.636f)
                    lineToRelative(1.096f, 0.732f)
                    curveToRelative(3.0570f, 2.040f, 6.2180f, 1.5880f, 8.1950f, -1.1730f)
                    lineToRelative(0.678f, -0.947f)
                    lineToRelative(0.808f, 0.981f)
                    curveToRelative(1.0710f, 1.3020f, 2.1440f, 2.1850f, 3.340f, 2.750f)
                    curveToRelative(0.830f, 0.3920f, 1.230f, 0.4630f, 2.590f, 0.4630f)
                    curveToRelative(1.3450f, 00f, 1.750f, -0.0710f, 2.4980f, -0.4390f)
                    curveToRelative(0.8770f, -0.4310f, 2.720f, -2.0840f, 3.080f, -2.7620f)
                    curveToRelative(0.1770f, -0.3320f, 0.2040f, -0.3280f, 0.7340f, 0.1190f)
                    curveToRelative(1.450f, 1.2190f, 3.9270f, 1.9790f, 6.4510f, 1.9790f)
                    curveToRelative(2.0150f, 00f, 3.5040f, -0.3520f, 5.1550f, -1.2160f)
                    lineToRelative(1.182f, -0.62f)
                    lineToRelative(1.16f, 0.383f)
                    curveToRelative(0.90f, 0.2970f, 1.6250f, 0.3860f, 3.2230f, 0.3980f)
                    curveToRelative(2.0380f, 0.0160f, 2.0780f, 0.0080f, 3.4220f, -0.6560f)
                    curveToRelative(1.0270f, -0.5080f, 1.6380f, -0.9720f, 2.4920f, -1.8940f)
                    lineToRelative(1.131f, -1.222f)
                    lineToRelative(0.8f, 0.613f)
                    curveToRelative(1.9030f, 1.460f, 4.660f, 2.3440f, 6.5730f, 2.1040f)
                    curveToRelative(1.9270f, -0.240f, 3.3280f, -0.8830f, 5.3630f, -2.460f)
                    lineToRelative(1.134f, -0.878f)
                    lineToRelative(1.186f, 0.535f)
                    curveToRelative(2.8380f, 1.2790f, 4.940f, 1.720f, 8.1490f, 1.7070f)
                    curveToRelative(2.220f, -0.0090f, 3.8330f, -0.2120f, 5.5880f, -0.7030f)
                    curveToRelative(1.5380f, -0.4310f, -7.8250f, 3.2270f, -14.370f, 5.6140f)
                    curveToRelative(-4.6750f, 1.7050f, -5.6360f, 1.8890f, -9.160f, 1.750f)
                    curveToRelative(-2.5950f, -0.1040f, -3.590f, -0.2770f, -5.7980f, -1.0120f)
                    lineToRelative(-1.19f, -0.396f)
                    lineToRelative(-1.459f, 0.538f)
                    curveToRelative(-5.130f, 1.890f, -8.9690f, 2.2330f, -11.3730f, 1.0150f)
                    lineToRelative(-0.908f, -0.46f)
                    lineToRelative(-2.285f, 0.978f)
                    curveToRelative(-1.2570f, 0.5380f, -2.7990f, 1.120f, -3.4260f, 1.2930f)
                    curveToRelative(-1.4310f, 0.3950f, -4.1330f, 0.4020f, -5.5760f, 0.0140f)
                    lineToRelative(-1.042f, -0.28f)
                    lineToRelative(-1.846f, 0.605f)
                    curveToRelative(-1.0160f, 0.3330f, -2.3850f, 0.6990f, -3.0440f, 0.8130f)
                    curveToRelative(-1.8070f, 0.3150f, -4.3530f, 0.1280f, -6.0360f, -0.4440f)
                    lineToRelative(-1.408f, -0.477f)
                    lineToRelative(-2.705f, 0.987f)
                    curveToRelative(-1.4880f, 0.5440f, -3.1640f, 1.0730f, -3.7250f, 1.1770f)
                    curveToRelative(-1.9170f, 0.3540f, -4.0130f, -0.3250f, -5.7740f, -1.8730f)
                    lineToRelative(-0.917f, -0.805f)
                    lineToRelative(-1.103f, 0.448f)
                    curveToRelative(-2.930f, 1.1910f, -6.5450f, 1.4940f, -8.1040f, 0.680f)
                    lineToRelative(-0.686f, -0.358f)
                    lineToRelative(-1.484f, 0.596f)
                    curveToRelative(-3.160f, 1.2690f, -5.7940f, 1.6330f, -7.0780f, 0.9780f)
                    lineToRelative(-0.638f, -0.325f)
                    lineToRelative(-1.944f, 0.765f)
                    curveToRelative(-2.9310f, 1.1530f, -4.890f, 1.560f, -6.9830f, 1.450f)
                    lineToRelative(-1.721f, -0.09f)
                    lineToRelative(-2.763f, 1.066f)
                    curveToRelative(-3.8190f, 1.4720f, -5.6860f, 1.9220f, -7.9770f, 1.9220f)
                    horizontalLineToRelative(-1.835f)
                    lineToRelative(-3.007f, 1.18f)
                    curveToRelative(-4.1080f, 1.6120f, -6.4910f, 2.390f, -7.3150f, 2.390f)
                    curveToRelative(-1.4520f, 00f, -2.8920f, -0.7430f, -3.6680f, -1.8940f)
                    curveToRelative(-0.5150f, -0.7630f, -0.3870f, -0.7910f, -4.310f, 0.9380f)
                    curveToRelative(-5.6950f, 2.5110f, -7.7660f, 2.6340f, -9.8620f, 0.5850f)
                    lineToRelative(-0.726f, -0.71f)
                    lineToRelative(-3.29f, 1.318f)
                    curveToRelative(-7.6650f, 3.0690f, -11.5580f, 4.590f, -12.3960f, 4.8460f)
                    curveToRelative(-1.380f, 0.4210f, -3.0650f, 0.4920f, -4.1940f, 0.1760f)
                    close()
                    moveTo(62.45f, 87.689f)
                    curveToRelative(-0.3130f, -0.190f, -0.6540f, -0.5060f, -0.7590f, -0.7010f)
                    curveToRelative(-0.1180f, -0.2220f, -0.190f, -1.9330f, -0.190f, -4.570f)
                    curveToRelative(00f, -2.3170f, -0.0850f, -4.9810f, -0.190f, -5.920f)
                    curveToRelative(-0.1690f, -1.5160f, -0.6380f, -3.7240f, -0.9680f, -4.5570f)
                    curveToRelative(-0.090f, -0.2290f, 0.0660f, -0.380f, 0.6650f, -0.6450f)
                    curveToRelative(1.0340f, -0.4570f, 2.1060f, -0.8070f, 2.1970f, -0.7160f)
                    curveToRelative(0.040f, 0.040f, 0.2720f, 1.6340f, 0.5160f, 3.5430f)
                    curveToRelative(1.130f, 8.8560f, 1.2080f, 12.8290f, 0.2650f, 13.6020f)
                    curveToRelative(-0.490f, 0.4010f, -0.8320f, 0.3930f, -1.5360f, -0.0360f)
                    close()
                    moveToRelative(14.315f, -1.943f)
                    curveToRelative(-0.8880f, -0.5420f, -1.7410f, -1.6270f, -1.620f, -2.060f)
                    curveToRelative(0.6960f, -2.4980f, 0.7780f, -5.8650f, 0.1970f, -8.0560f)
                    curveToRelative(-0.4860f, -1.8320f, -2.4140f, -5.4490f, -3.1460f, -5.90f)
                    curveToRelative(-0.1080f, -0.0670f, -0.570f, 0.2420f, -1.0270f, 0.6870f)
                    lineToRelative(-0.83f, 0.81f)
                    lineToRelative(-1.161f, -0.27f)
                    curveToRelative(-0.6380f, -0.150f, -1.2810f, -0.310f, -1.4280f, -0.3580f)
                    curveToRelative(-0.6530f, -0.2130f, 5.4630f, -2.8520f, 12.3170f, -5.3150f)
                    curveToRelative(1.8170f, -0.6520f, 3.6240f, -1.3020f, 4.0170f, -1.4440f)
                    curveToRelative(0.6970f, -0.2510f, 0.7040f, -0.250f, 0.270f, 0.0540f)
                    curveToRelative(-2.080f, 1.460f, -3.5220f, 3.0830f, -4.6230f, 5.2070f)
                    curveToRelative(-2.5350f, 4.8890f, -2.270f, 11.060f, 0.6110f, 14.2650f)
                    lineToRelative(0.794f, 0.883f)
                    lineToRelative(-0.702f, 0.635f)
                    curveToRelative(-1.3560f, 1.2240f, -2.5970f, 1.5160f, -3.670f, 0.8620f)
                    close()
                    moveToRelative(13.3f, -2.64f)
                    curveToRelative(-0.7960f, -0.1340f, -1.9640f, -0.4680f, -1.9640f, -0.5610f)
                    curveToRelative(00f, -0.050f, 0.3090f, -0.4260f, 0.6870f, -0.8360f)
                    curveToRelative(2.030f, -2.2040f, 3.0670f, -5.7740f, 2.680f, -9.2330f)
                    curveToRelative(-0.370f, -3.3120f, -1.9210f, -6.0380f, -4.5020f, -7.9140f)
                    lineToRelative(-0.492f, -0.357f)
                    lineToRelative(7.698f, -2.677f)
                    arcToRelative(
                        3048.13f,
                        3048.13f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        7.909f,
                        -2.746f
                    )
                    curveToRelative(0.1160f, -0.0370f, -0.110f, 0.1520f, -0.5020f, 0.420f)
                    curveToRelative(-1.7690f, 1.2040f, -3.1860f, 30f, -4.4490f, 5.6380f)
                    curveToRelative(-1.7630f, 3.6820f, -2.3440f, 7.0360f, -2.1870f, 12.6210f)
                    lineToRelative(0.107f, 3.795f)
                    lineToRelative(-0.531f, 0.605f)
                    curveToRelative(-0.2930f, 0.3330f, -0.8320f, 0.7250f, -1.1990f, 0.870f)
                    curveToRelative(-0.7220f, 0.2870f, -2.5360f, 0.4960f, -3.2560f, 0.3750f)
                    close()
                    moveToRelative(-6.13f, -3.682f)
                    curveToRelative(-1.0620f, -1.4160f, -1.4070f, -2.5230f, -1.3970f, -4.4780f)
                    curveToRelative(0.0110f, -2.1140f, 0.9280f, -4.380f, 2.3050f, -5.70f)
                    lineToRelative(0.505f, -0.484f)
                    lineToRelative(0.618f, 0.33f)
                    curveToRelative(1.3410f, 0.7140f, 2.10f, 3.4150f, 1.6140f, 5.750f)
                    curveToRelative(-0.380f, 1.830f, -2.2890f, 5.1620f, -2.9570f, 5.1620f)
                    curveToRelative(-0.140f, 00f, -0.4490f, -0.2610f, -0.6880f, -0.580f)
                    close()
                    moveToRelative(17.447f, 0.154f)
                    curveToRelative(-1.3730f, -0.6160f, -1.9440f, -2.4830f, -1.8070f, -5.9110f)
                    curveToRelative(0.130f, -3.2960f, 1.010f, -6.2780f, 2.380f, -8.0740f)
                    curveToRelative(0.440f, -0.5770f, 1.4460f, -1.2320f, 1.6220f, -1.0560f)
                    curveToRelative(0.050f, 0.050f, 0.4120f, 1.4820f, 0.8050f, 3.1820f)
                    curveToRelative(0.3920f, 1.70f, 1.0620f, 4.2960f, 1.4870f, 5.770f)
                    lineToRelative(0.774f, 2.677f)
                    lineToRelative(-0.339f, 0.767f)
                    curveToRelative(-0.530f, 1.1980f, -0.970f, 1.7130f, -1.90f, 2.2170f)
                    curveToRelative(-1.0910f, 0.590f, -2.2830f, 0.7590f, -3.0220f, 0.4280f)
                    close()
                    moveToRelative(11.947f, -0.593f)
                    curveToRelative(-0.2140f, -0.040f, -0.4730f, -0.1760f, -0.5770f, -0.30f)
                    curveToRelative(-0.1040f, -0.1260f, -0.7520f, -2.4170f, -1.440f, -5.0920f)
                    curveToRelative(-2.0550f, -7.9870f, -2.6520f, -9.850f, -3.9680f, -12.3820f)
                    curveToRelative(-0.7910f, -1.5220f, -1.9440f, -2.6950f, -2.820f, -2.870f)
                    curveToRelative(-0.4810f, -0.0960f, -0.5350f, -0.1750f, -0.5350f, -0.7810f)
                    curveToRelative(00f, -0.370f, 0.120f, -1.3970f, 0.2650f, -2.280f)
                    arcToRelative(
                        30.523f,
                        30.523f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        3.169f,
                        -9.405f
                    )
                    curveToRelative(0.8310f, -1.5670f, 0.1880f, -1.1910f, 6.1160f, -3.570f)
                    curveToRelative(3.5560f, -1.4250f, 10.5440f, -4.1090f, 11.2730f, -4.3280f)
                    curveToRelative(0.3290f, -0.0990f, 0.2130f, 0.220f, -0.8160f, 2.2560f)
                    curveToRelative(-1.2480f, 2.470f, -2.10f, 4.8780f, -2.6410f, 7.460f)
                    curveToRelative(-0.4170f, 1.990f, -0.4290f, 9.2860f, -0.0190f, 11.5630f)
                    lineToRelative(0.266f, 1.476f)
                    lineToRelative(-0.46f, 0.093f)
                    curveToRelative(-3.7220f, 0.7430f, -5.7110f, 2.0350f, -6.9280f, 4.4960f)
                    curveToRelative(-0.6440f, 1.3030f, -0.6770f, 1.4580f, -0.7350f, 3.4270f)
                    curveToRelative(-0.0840f, 2.8560f, 0.5430f, 5.3250f, 1.9630f, 7.7340f)
                    curveToRelative(0.280f, 0.4730f, 0.5070f, 1.0030f, 0.5070f, 1.1780f)
                    curveToRelative(00f, 0.7480f, -1.5430f, 1.5290f, -2.620f, 1.3250f)
                    close()
                    moveToRelative(20.9f, -0.738f)
                    curveToRelative(-2.40f, -1.750f, -2.8420f, -3.0240f, -2.8290f, -8.150f)
                    curveToRelative(0.0070f, -2.8520f, 0.0990f, -4.0040f, 0.5750f, -7.230f)
                    curveToRelative(0.3110f, -2.1120f, 0.6560f, -4.990f, 0.7660f, -6.3970f)
                    curveToRelative(0.3540f, -4.5260f, -0.2830f, -8.4860f, -1.8850f, -11.7190f)
                    curveToRelative(-0.4370f, -0.8810f, -0.7360f, -1.6540f, -0.6650f, -1.7180f)
                    curveToRelative(0.3130f, -0.280f, 9.5010f, -3.7950f, 9.5010f, -3.6350f)
                    curveToRelative(00f, 0.0430f, -0.480f, 1.7690f, -1.0680f, 3.8360f)
                    curveToRelative(-2.180f, 7.6710f, -2.7740f, 11.7660f, -2.6260f, 18.1150f)
                    curveToRelative(0.1050f, 4.5530f, 0.4360f, 7.230f, 1.470f, 11.8870f)
                    curveToRelative(0.340f, 1.530f, 0.6180f, 2.9190f, 0.6180f, 3.0860f)
                    curveToRelative(00f, 0.4930f, -0.6660f, 1.6550f, -1.180f, 2.0590f)
                    curveToRelative(-0.7080f, 0.5560f, -1.8040f, 0.5020f, -2.6760f, -0.1340f)
                    close()
                    moveToRelative(12.273f, -1.262f)
                    curveToRelative(-1.070f, -0.370f, -1.7130f, -0.7980f, -2.2240f, -1.4830f)
                    lineToRelative(-0.346f, -0.463f)
                    lineToRelative(0.55f, -0.963f)
                    curveToRelative(1.5850f, -2.7690f, 3.2380f, -8.1380f, 3.6270f, -11.7750f)
                    curveToRelative(0.1190f, -1.1130f, 0.2330f, -2.0470f, 0.2520f, -2.0770f)
                    curveToRelative(0.0510f, -0.0750f, 3.710f, -1.4630f, 3.8570f, -1.4630f)
                    curveToRelative(0.0660f, 00f, -0.2380f, 0.7150f, -0.6750f, 1.590f)
                    curveToRelative(-1.2380f, 2.4760f, -1.7270f, 4.5930f, -1.7380f, 7.5140f)
                    curveToRelative(-0.010f, 2.7160f, 0.220f, 3.920f, 1.2370f, 6.4620f)
                    curveToRelative(0.730f, 1.8220f, 0.6720f, 2.1120f, -0.520f, 2.6540f)
                    curveToRelative(-0.970f, 0.440f, -2.750f, 0.4420f, -4.020f, 0.0040f)
                    close()
                    moveToRelative(-100.337f, -0.551f)
                    curveToRelative(-0.0070f, -0.3440f, 0.3470f, -5.7260f, 0.7880f, -11.960f)
                    curveToRelative(0.440f, -6.2360f, 0.8040f, -11.5780f, 0.8070f, -11.8720f)
                    curveToRelative(0.010f, -0.8330f, 0.2750f, -0.2180f, 0.6130f, 1.420f)
                    curveToRelative(1.1750f, 5.6870f, 0.1990f, 17.9780f, -1.7540f, 22.1070f)
                    lineToRelative(-0.44f, 0.93f)
                    close()
                    moveToRelative(111.089f, 0.194f)
                    lineToRelative(-0.558f, -0.34f)
                    lineToRelative(1.049f, -2.114f)
                    curveToRelative(2.2910f, -4.6160f, 2.3650f, -8.0360f, 0.2380f, -11.0780f)
                    curveToRelative(-0.4280f, -0.6120f, -0.7210f, -1.1470f, -0.6530f, -1.1880f)
                    curveToRelative(0.1960f, -0.120f, 9.510f, -3.6830f, 9.6250f, -3.6830f)
                    curveToRelative(0.2130f, 00f, -0.1330f, 2.9260f, -0.5860f, 4.9510f)
                    curveToRelative(-1.0090f, 4.5110f, -2.8780f, 8.9430f, -4.9120f, 11.6420f)
                    curveToRelative(-1.5410f, 2.0450f, -2.8760f, 2.620f, -4.2030f, 1.810f)
                    close()
                    moveToRelative(-36.038f, -0.822f)
                    curveToRelative(-1.7270f, -0.8680f, -3.1080f, -4.2570f, -2.7430f, -6.7360f)
                    curveToRelative(0.2380f, -1.6170f, 1.4550f, -2.9050f, 3.2490f, -3.4380f)
                    curveToRelative(0.4910f, -0.1460f, 0.9070f, -0.2450f, 0.9240f, -0.2210f)
                    curveToRelative(0.0170f, 0.0240f, 0.4060f, 1.1550f, 0.8650f, 2.5140f)
                    curveToRelative(0.460f, 1.3580f, 1.2360f, 3.320f, 1.7270f, 4.360f)
                    lineToRelative(0.892f, 1.892f)
                    lineToRelative(-0.483f, 0.505f)
                    curveToRelative(-1.1570f, 1.2070f, -3.2230f, 1.730f, -4.4310f, 1.1240f)
                    close()
                    moveToRelative(53.196f, -1.425f)
                    curveToRelative(0.8510f, -2.5170f, 1.1140f, -6.7230f, 0.5510f, -8.8230f)
                    curveToRelative(-0.6370f, -2.3790f, -2.1430f, -4.5150f, -4.040f, -5.7320f)
                    curveToRelative(-0.4930f, -0.3170f, -0.8730f, -0.5950f, -0.8450f, -0.6190f)
                    curveToRelative(0.2070f, -0.1710f, 10.5310f, -4.040f, 10.60f, -3.9710f)
                    curveToRelative(0.120f, 0.120f, -0.5880f, 6.9220f, -0.920f, 8.8440f)
                    curveToRelative(-0.4270f, 2.4740f, -0.9310f, 4.0980f, -1.8860f, 6.0750f)
                    curveToRelative(-0.730f, 1.5110f, -1.1320f, 2.090f, -2.2350f, 3.2130f)
                    curveToRelative(-0.740f, 0.7540f, -1.2920f, 1.210f, -1.2250f, 1.0130f)
                    close()
                    moveToRelative(14.63f, -1.385f)
                    curveToRelative(1.3470f, -3.5760f, 1.3270f, -7.8680f, -0.0510f, -11.0540f)
                    lineToRelative(-0.361f, -0.835f)
                    lineToRelative(0.747f, -0.26f)
                    curveToRelative(0.410f, -0.1430f, 1.3050f, -0.4730f, 1.9880f, -0.7330f)
                    curveToRelative(1.2370f, -0.470f, 1.240f, -0.470f, 0.870f, -0.0650f)
                    curveToRelative(-0.5750f, 0.6310f, -0.7730f, 1.1230f, -0.7730f, 1.9220f)
                    curveToRelative(00f, 1.3610f, 0.9920f, 2.5450f, 2.320f, 2.770f)
                    curveToRelative(2.1850f, 0.3690f, 3.9610f, -2.3080f, 2.7540f, -4.1520f)
                    curveToRelative(-0.3180f, -0.4850f, -0.8320f, -0.230f, 4.7450f, -2.3470f)
                    lineToRelative(4.195f, -1.592f)
                    lineToRelative(0.058f, 1.278f)
                    curveToRelative(0.2510f, 5.5940f, -3.1420f, 11.2590f, -8.3490f, 13.9350f)
                    curveToRelative(-2.3150f, 1.190f, -4.1340f, 1.6990f, -7.7270f, 2.1620f)
                    lineToRelative(-0.844f, 0.109f)
                    close()
                    moveToRelative(-20.497f, -0.031f)
                    curveToRelative(-0.380f, -0.140f, -0.7570f, -0.320f, -0.8380f, -0.40f)
                    curveToRelative(-0.080f, -0.0820f, 0.1730f, -1.0830f, 0.5630f, -2.2250f)
                    curveToRelative(0.390f, -1.1420f, 0.8120f, -2.790f, 0.9390f, -3.660f)
                    lineToRelative(0.23f, -1.582f)
                    lineToRelative(0.57f, 0.982f)
                    curveToRelative(0.3120f, 0.540f, 0.7270f, 1.4050f, 0.9230f, 1.9220f)
                    curveToRelative(1.2460f, 3.2960f, 0.020f, 5.8440f, -2.3870f, 4.9630f)
                    close()
                    moveToRelative(-14.654f, -1.204f)
                    curveToRelative(-0.6610f, -2.2250f, -0.7060f, -5.5950f, -0.0950f, -7.2010f)
                    curveToRelative(0.1820f, -0.480f, 0.1950f, -0.4830f, 0.5460f, -0.1660f)
                    curveToRelative(0.1980f, 0.180f, 0.6470f, 0.9070f, 0.9980f, 1.6170f)
                    curveToRelative(0.590f, 1.1940f, 0.6330f, 1.3890f, 0.5710f, 2.6120f)
                    curveToRelative(-0.0540f, 1.0660f, -0.1840f, 1.5510f, -0.6720f, 2.5130f)
                    curveToRelative(-0.8660f, 1.7070f, -1.0070f, 1.7720f, -1.3480f, 0.6250f)
                    close()
                    moveToRelative(28.392f, -0.712f)
                    curveToRelative(0.790f, -1.560f, 1.3410f, -4.080f, 1.4550f, -6.6560f)
                    lineToRelative(0.102f, -2.312f)
                    lineToRelative(0.529f, 0.616f)
                    curveToRelative(0.290f, 0.3390f, 0.7790f, 1.1380f, 1.0850f, 1.7760f)
                    curveToRelative(1.2850f, 2.6760f, 0.3560f, 6.1250f, -1.9280f, 7.1620f)
                    curveToRelative(-0.3970f, 0.180f, -0.9440f, 0.330f, -1.2160f, 0.3320f)
                    lineToRelative(-0.494f, 0.004f)
                    close()
                    moveToRelative(-40.844f, -1.317f)
                    curveToRelative(-0.060f, -0.0960f, -0.2420f, -0.690f, -0.4050f, -1.3190f)
                    curveToRelative(-1.1440f, -4.4160f, -1.1050f, -13.560f, 0.0850f, -19.6980f)
                    curveToRelative(0.5730f, -2.9540f, 0.5530f, -2.9460f, 1.3120f, -0.5450f)
                    curveToRelative(2.0750f, 6.5620f, 2.2330f, 12.9430f, 0.4540f, 18.3480f)
                    curveToRelative(-0.6670f, 2.0240f, -1.3050f, 3.4430f, -1.4460f, 3.2140f)
                    close()
                    moveToRelative(-14.189f, -6.778f)
                    curveToRelative(-1.4140f, -5.7280f, -2.0180f, -9.4860f, -2.0180f, -12.570f)
                    curveToRelative(00f, -2.4010f, 0.7290f, -6.50f, 1.090f, -6.1340f)
                    curveToRelative(0.320f, 0.3220f, 1.230f, 3.490f, 1.5030f, 5.2280f)
                    curveToRelative(0.3450f, 2.1960f, 0.5470f, 11.420f, 0.3180f, 14.5470f)
                    lineToRelative(-0.144f, 1.963f)
                    close()
                    moveToRelative(65.674f, 0.979f)
                    curveToRelative(-1.2380f, -0.7550f, -1.390f, -2.4230f, -0.320f, -3.4940f)
                    curveToRelative(1.0720f, -1.0720f, 2.740f, -0.920f, 3.4950f, 0.320f)
                    curveToRelative(1.2630f, 2.070f, -1.1030f, 4.4370f, -3.1750f, 3.1740f)
                    close()
                    moveToRelative(0.859f, -1.156f)
                    curveToRelative(00f, -0.7660f, 0.2130f, -0.7920f, 0.7140f, -0.090f)
                    curveToRelative(0.210f, 0.2950f, 0.5020f, 0.5360f, 0.650f, 0.5360f)
                    curveToRelative(0.2130f, 00f, 0.1910f, -0.1060f, -0.1090f, -0.5270f)
                    curveToRelative(-0.320f, -0.450f, -0.3420f, -0.5810f, -0.1470f, -0.8930f)
                    curveToRelative(0.350f, -0.5610f, -0.010f, -0.9780f, -0.9030f, -1.0440f)
                    lineToRelative(-0.74f, -0.055f)
                    lineToRelative(0.001f, 0.68f)
                    curveToRelative(0.0030f, 0.9780f, 0.1980f, 2.0180f, 0.3790f, 2.0180f)
                    curveToRelative(0.0850f, 00f, 0.1550f, -0.2810f, 0.1550f, -0.6250f)
                    close()
                    moveToRelative(-0.058f, -1.232f)
                    curveToRelative(-0.0560f, -0.1470f, 0.0420f, -0.2860f, 0.2390f, -0.3370f)
                    curveToRelative(0.3780f, -0.10f, 0.5330f, 0.0020f, 0.5330f, 0.350f)
                    curveToRelative(00f, 0.3160f, -0.650f, 0.3060f, -0.7720f, -0.0130f)
                    close()
                }
            }
        }.build()
    ),

    // https://github.com/twbs/icons/blob/main/icons/threads.svg
    THREADS(
        ImageVector.Builder(
            name = "Threads",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6.321f, 6.016f)
                curveToRelative(-0.27f, -0.18f, -1.166f, -0.802f, -1.166f, -0.802f)
                curveToRelative(0.756f, -1.081f, 1.753f, -1.502f, 3.132f, -1.502f)
                curveToRelative(0.975f, 0f, 1.803f, 0.327f, 2.394f, 0.948f)
                reflectiveCurveToRelative(0.928f, 1.509f, 1.005f, 2.644f)
                quadToRelative(0.492f, 0.207f, 0.905f, 0.484f)
                curveToRelative(1.109f, 0.745f, 1.719f, 1.86f, 1.719f, 3.137f)
                curveToRelative(0f, 2.716f, -2.226f, 5.075f, -6.256f, 5.075f)
                curveTo(4.594f, 16f, 1f, 13.987f, 1f, 7.994f)
                curveTo(1f, 2.034f, 4.482f, 0f, 8.044f, 0f)
                curveTo(9.69f, 0f, 13.55f, 0.243f, 15f, 5.036f)
                lineToRelative(-1.36f, 0.353f)
                curveTo(12.516f, 1.974f, 10.163f, 1.43f, 8.006f, 1.43f)
                curveToRelative(-3.565f, 0f, -5.582f, 2.171f, -5.582f, 6.79f)
                curveToRelative(0f, 4.143f, 2.254f, 6.343f, 5.63f, 6.343f)
                curveToRelative(2.777f, 0f, 4.847f, -1.443f, 4.847f, -3.556f)
                curveToRelative(0f, -1.438f, -1.208f, -2.127f, -1.27f, -2.127f)
                curveToRelative(-0.236f, 1.234f, -0.868f, 3.31f, -3.644f, 3.31f)
                curveToRelative(-1.618f, 0f, -3.013f, -1.118f, -3.013f, -2.582f)
                curveToRelative(0f, -2.09f, 1.984f, -2.847f, 3.55f, -2.847f)
                curveToRelative(0.586f, 0f, 1.294f, 0.04f, 1.663f, 0.114f)
                curveToRelative(0f, -0.637f, -0.54f, -1.728f, -1.9f, -1.728f)
                curveToRelative(-1.25f, 0f, -1.566f, 0.405f, -1.967f, 0.868f)
                close()
                moveTo(8.716f, 8.19f)
                curveToRelative(-2.04f, 0f, -2.304f, 0.87f, -2.304f, 1.416f)
                curveToRelative(0f, 0.878f, 1.043f, 1.168f, 1.6f, 1.168f)
                curveToRelative(1.02f, 0f, 2.067f, -0.282f, 2.232f, -2.423f)
                arcToRelative(
                    6.2f,
                    6.2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -1.528f,
                    -0.161f
                )
            }
        }.build()
    ),

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/tiktok.svg
    TIK_TOK(
        ImageVector.Builder(
            name = "TikTok",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(9f, 0f)
                horizontalLineToRelative(1.98f)
                curveToRelative(0.144f, 0.715f, 0.54f, 1.617f, 1.235f, 2.512f)
                curveTo(12.895f, 3.389f, 13.797f, 4f, 15f, 4f)
                verticalLineToRelative(2f)
                curveToRelative(-1.753f, 0f, -3.07f, -0.814f, -4f, -1.829f)
                verticalLineTo(11f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -5f, -5f)
                verticalLineToRelative(2f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 3f, 3f)
                close()
            }
        }.build()
    ),

    // https://uxwing.com/tumblr-icon/
    TUMBLR(
        ImageVector.Builder(
            name = "Tumblr",
            defaultWidth = 16.dp,
            defaultHeight = (512.184 / 289.999 * 16).dp,
            viewportWidth = 289.999f,
            viewportHeight = 512.184f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(204.435f, 512.184f)
                curveToRelative(-77.020f, 00f, -134.4270f, -39.6290f, -134.4270f, -134.4380f)
                verticalLineTo(225.914f)
                horizontalLineTo(0f)
                verticalLineTo(143.7f)
                curveTo(77.0480f, 123.6990f, 109.260f, 57.4080f, 112.9830f, 00f)
                horizontalLineToRelative(79.974f)
                verticalLineToRelative(130.361f)
                horizontalLineToRelative(93.314f)
                verticalLineToRelative(95.553f)
                horizontalLineToRelative(-93.314f)
                verticalLineToRelative(132.21f)
                curveToRelative(00f, 39.6290f, 19.9950f, 53.3230f, 51.8520f, 53.3230f)
                horizontalLineToRelative(45.19f)
                verticalLineToRelative(100.737f)
                horizontalLineToRelative(-85.564f)
                close()
            }
        }.build()
    ),

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/twitch.svg
    TWITCH(
        ImageVector.Builder(
            name = "Twitch",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3.857f, 0f)
                lineTo(1f, 2.857f)
                verticalLineToRelative(10.286f)
                horizontalLineToRelative(3.429f)
                verticalLineTo(16f)
                lineToRelative(2.857f, -2.857f)
                horizontalLineTo(9.57f)
                lineTo(14.714f, 8f)
                verticalLineTo(0f)
                close()
                moveToRelative(9.714f, 7.429f)
                lineToRelative(-2.285f, 2.285f)
                horizontalLineTo(9f)
                lineToRelative(-2f, 2f)
                verticalLineToRelative(-2f)
                horizontalLineTo(4.429f)
                verticalLineTo(1.143f)
                horizontalLineToRelative(9.142f)
                close()
            }
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11.857f, 3.143f)
                horizontalLineToRelative(-1.143f)
                verticalLineTo(6.57f)
                horizontalLineToRelative(1.143f)
                close()
                moveToRelative(-3.143f, 0f)
                horizontalLineTo(7.571f)
                verticalLineTo(6.57f)
                horizontalLineToRelative(1.143f)
                close()
            }
        }.build()
    ),

    // Converted using Inkscape from https://help.vgen.co/hc/en-us/articles/12884824880663-VGen-Brand-Assets
    VGEN(
        ImageVector.Builder(
            name = "VGen",
            defaultWidth = 16.dp,
            defaultHeight = (45.751148 / 58.917126 * 16).dp,
            viewportWidth = 58.917126f,
            viewportHeight = 45.751148f
        ).apply {
            group(
                translationX = -72.56781f,
                translationY = -32.308155f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(93.318546f, 49.109463f)
                    curveToRelative(-2.3620f, -2.46970f, -4.05870f, -5.44360f, -7.40830f, -6.71880f)
                    curveToRelative(-6.28070f, -2.39120f, -13.34240f, 2.42160f, -13.34240f, 9.18830f)
                    curveToRelative(00f, 5.88570f, 5.97290f, 11.32620f, 9.81460f, 15.16940f)
                    curveToRelative(5.25230f, 5.25430f, 11.34330f, 10.46110f, 19.050f, 11.22870f)
                    curveToRelative(10.26670f, 1.02260f, 18.72430f, -7.69030f, 24.38250f, -15.10930f)
                    curveToRelative(2.00710f, -2.63170f, 4.4950f, -5.5560f, 5.33520f, -8.81940f)
                    curveToRelative(1.17170f, -4.5510f, -0.9070f, -9.41660f, -3.52260f, -13.05280f)
                    curveToRelative(-7.93080f, -11.02530f, -24.71450f, -12.25070f, -32.02850f, 0.35280f)
                    curveToRelative(-1.36620f, 2.35420f, -2.16370f, 5.04780f, -2.28050f, 7.76110f)
                    moveToRelative(11.994444f, 12.69999f)
                    curveToRelative(-3.49750f, -3.50720f, -6.98640f, -7.12780f, -7.19850f, -12.34720f)
                    curveToRelative(-0.32970f, -8.1130f, 7.55810f, -14.06570f, 15.31240f, -12.07880f)
                    curveToRelative(4.13570f, 1.05970f, 7.37120f, 4.46240f, 9.23920f, 8.19830f)
                    curveToRelative(2.67290f, 5.34590f, -0.56120f, 8.88290f, -3.82830f, 13.05280f)
                    curveToRelative(-1.94270f, 2.47950f, -4.05580f, 4.86730f, -6.46920f, 6.90070f)
                    curveToRelative(-11.16080f, 9.40330f, -21.7080f, 1.8790f, -29.8350f, -7.60620f)
                    curveToRelative(-1.87260f, -2.18560f, -9.01410f, -10.25860f, -1.9150f, -11.19420f)
                    curveToRelative(3.11450f, -0.41040f, 5.14840f, 3.54890f, 6.85360f, 5.54970f)
                    curveToRelative(4.56080f, 5.35150f, 10.38520f, 12.62410f, 17.84090f, 9.5250f)
                    moveToRelative(3.52778f, -17.4369f)
                    curveToRelative(-4.89970f, 1.39790f, -3.74810f, 6.59040f, -1.05670f, 9.3230f)
                    curveToRelative(0.7750f, 0.78680f, 2.16690f, 2.70730f, 3.4660f, 2.34750f)
                    curveToRelative(6.61970f, -1.83360f, 5.08970f, -13.810f, -2.40930f, -11.67050f)
                    close()
                }
            }
        }.build()
    ),

    // Converted using Inkscape from https://www.weebly.com/press/press-kit
    WEEBLY(
        ImageVector.Builder(
            name = "Weebly",
            defaultWidth = 16.dp,
            defaultHeight = (171.805 / 226.014 * 16).dp,
            viewportWidth = 59.799f,
            viewportHeight = 45.457f
        ).apply {
            group(
                translationX = -72.628f,
                translationY = -130.085f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(102.268f, 169.228f)
                    horizontalLineToRelative(0.53f)
                    curveToRelative(2.1430f, 7.2840f, 12.5030f, 8.0720f, 17.4160f, 3.360f)
                    curveToRelative(2.2730f, -2.180f, 3.1490f, -5.2710f, 4.2280f, -8.1220f)
                    curveToRelative(2.0530f, -5.420f, 40f, -10.9190f, 5.8710f, -16.4040f)
                    curveToRelative(1.010f, -2.9630f, 2.4580f, -6.060f, 2.0410f, -9.260f)
                    curveToRelative(-1.370f, -10.5120f, -17.2420f, -11.9490f, -19.7670f, -1.3240f)
                    horizontalLineToRelative(-0.265f)
                    curveToRelative(-2.3120f, -9.730f, -17.2650f, -9.7350f, -19.5790f, 00f)
                    curveToRelative(-1.590f, -2.7920f, -2.7130f, -5.130f, -5.820f, -6.5310f)
                    curveToRelative(-5.7530f, -2.5940f, -13.7720f, 0.8770f, -14.2640f, 7.590f)
                    curveToRelative(-0.2660f, 3.6320f, 1.2620f, 7.2460f, 2.510f, 10.5830f)
                    curveToRelative(1.570f, 4.20f, 3.0980f, 8.4480f, 4.5210f, 12.70f)
                    curveToRelative(1.2060f, 3.6030f, 2.30f, 7.710f, 4.9670f, 10.5370f)
                    curveToRelative(4.7430f, 5.0250f, 15.470f, 4.150f, 17.6110f, -3.1290f)
                    moveToRelative(-9.79f, -12.7f)
                    horizontalLineToRelative(0.265f)
                    curveToRelative(0.370f, -4.2340f, 2.5410f, -8.9260f, 3.880f, -12.9640f)
                    curveToRelative(0.6570f, -1.9780f, 1.0050f, -4.3280f, 2.1970f, -6.0740f)
                    curveToRelative(1.4740f, -2.1570f, 4.920f, -2.450f, 6.8310f, -0.7010f)
                    curveToRelative(1.570f, 1.4360f, 1.9110f, 4.0460f, 2.530f, 5.9810f)
                    curveToRelative(1.4460f, 4.5280f, 2.5290f, 9.3960f, 4.4060f, 13.7580f)
                    curveToRelative(1.0840f, -3.3330f, 2.040f, -6.7080f, 3.0850f, -10.0540f)
                    curveToRelative(0.8690f, -2.7850f, 1.3690f, -7.6570f, 3.5860f, -9.6850f)
                    curveToRelative(2.740f, -2.5080f, 7.4150f, -0.6480f, 7.5470f, 3.070f)
                    curveToRelative(0.0940f, 2.6590f, -1.6220f, 5.7380f, -2.4860f, 8.2030f)
                    curveToRelative(-2.1210f, 6.0540f, -3.6670f, 13.9050f, -7.1180f, 19.3130f)
                    curveToRelative(-1.5930f, 2.4970f, -4.9180f, 3.4550f, -7.5230f, 1.8520f)
                    curveToRelative(-1.9970f, -1.2290f, -2.4180f, -3.4950f, -3.090f, -5.5550f)
                    curveToRelative(-1.3090f, -4.0190f, -2.3430f, -8.3080f, -4.0550f, -12.170f)
                    curveToRelative(-1.7180f, 3.8740f, -2.7170f, 8.150f, -4.0570f, 12.170f)
                    curveToRelative(-0.6740f, 2.0220f, -1.1390f, 4.3550f, -3.0880f, 5.5550f)
                    curveToRelative(-2.5580f, 1.5740f, -5.9430f, 0.5910f, -7.5240f, -1.8550f)
                    curveToRelative(-1.1610f, -1.7960f, -1.6330f, -4.0920f, -2.3770f, -6.0810f)
                    arcToRelative(
                        503.023f,
                        503.023f,
                        0f,
                        isMoreThanHalf = false,
                        isPositiveArc = true,
                        -4.915f,
                        -13.759f
                    )
                    curveToRelative(-0.850f, -2.490f, -2.5440f, -5.5010f, -2.3210f, -8.2020f)
                    curveToRelative(0.290f, -3.5230f, 5.1280f, -4.7640f, 7.5570f, -2.5410f)
                    curveToRelative(2.2360f, 2.0460f, 2.6750f, 6.8890f, 3.5830f, 9.6850f)
                    curveToRelative(1.0830f, 3.3340f, 2.0030f, 6.720f, 3.0880f, 10.0540f)
                    close()
                }
            }
        }.build()
    ),

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/twitter-x.svg
    X(
        ImageVector.Builder(
            name = "X",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12.6f, 0.75f)
                horizontalLineToRelative(2.454f)
                lineToRelative(-5.36f, 6.142f)
                lineTo(16f, 15.25f)
                horizontalLineToRelative(-4.937f)
                lineToRelative(-3.867f, -5.07f)
                lineToRelative(-4.425f, 5.07f)
                horizontalLineTo(0.316f)
                lineToRelative(5.733f, -6.57f)
                lineTo(0f, 0.75f)
                horizontalLineToRelative(5.063f)
                lineToRelative(3.495f, 4.633f)
                lineTo(12.601f, 0.75f)
                close()
                moveToRelative(-0.86f, 13.028f)
                horizontalLineToRelative(1.36f)
                lineTo(4.323f, 2.145f)
                horizontalLineTo(2.865f)
                close()
            }
        }.build()
    ),


    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/youtube.svg
    YOU_TUBE(
        ImageVector.Builder(
            name = "YouTube",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(8.051f, 1.999f)
                horizontalLineToRelative(0.089f)
                curveToRelative(0.822f, 0.003f, 4.987f, 0.033f, 6.11f, 0.335f)
                arcToRelative(
                    2.01f,
                    2.01f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    1.415f,
                    1.42f
                )
                curveToRelative(0.101f, 0.38f, 0.172f, 0.883f, 0.22f, 1.402f)
                lineToRelative(0.01f, 0.104f)
                lineToRelative(0.022f, 0.26f)
                lineToRelative(0.008f, 0.104f)
                curveToRelative(0.065f, 0.914f, 0.073f, 1.77f, 0.074f, 1.957f)
                verticalLineToRelative(0.075f)
                curveToRelative(-0.001f, 0.194f, -0.01f, 1.108f, -0.082f, 2.06f)
                lineToRelative(-0.008f, 0.105f)
                lineToRelative(-0.009f, 0.104f)
                curveToRelative(-0.05f, 0.572f, -0.124f, 1.14f, -0.235f, 1.558f)
                arcToRelative(
                    2.01f,
                    2.01f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -1.415f,
                    1.42f
                )
                curveToRelative(-1.16f, 0.312f, -5.569f, 0.334f, -6.18f, 0.335f)
                horizontalLineToRelative(-0.142f)
                curveToRelative(-0.309f, 0f, -1.587f, -0.006f, -2.927f, -0.052f)
                lineToRelative(-0.17f, -0.006f)
                lineToRelative(-0.087f, -0.004f)
                lineToRelative(-0.171f, -0.007f)
                lineToRelative(-0.171f, -0.007f)
                curveToRelative(-1.11f, -0.049f, -2.167f, -0.128f, -2.654f, -0.26f)
                arcToRelative(
                    2.01f,
                    2.01f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -1.415f,
                    -1.419f
                )
                curveToRelative(-0.111f, -0.417f, -0.185f, -0.986f, -0.235f, -1.558f)
                lineTo(0.09f, 9.82f)
                lineToRelative(-0.008f, -0.104f)
                arcTo(31f, 31f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 7.68f)
                verticalLineToRelative(-0.123f)
                curveToRelative(0.002f, -0.215f, 0.01f, -0.958f, 0.064f, -1.778f)
                lineToRelative(0.007f, -0.103f)
                lineToRelative(0.003f, -0.052f)
                lineToRelative(0.008f, -0.104f)
                lineToRelative(0.022f, -0.26f)
                lineToRelative(0.01f, -0.104f)
                curveToRelative(0.048f, -0.519f, 0.119f, -1.023f, 0.22f, -1.402f)
                arcToRelative(
                    2.01f,
                    2.01f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    1.415f,
                    -1.42f
                )
                curveToRelative(0.487f, -0.13f, 1.544f, -0.21f, 2.654f, -0.26f)
                lineToRelative(0.17f, -0.007f)
                lineToRelative(0.172f, -0.006f)
                lineToRelative(0.086f, -0.003f)
                lineToRelative(0.171f, -0.007f)
                arcTo(100f, 100f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.858f, 2f)
                close()
                moveTo(6.4f, 5.209f)
                verticalLineToRelative(4.818f)
                lineToRelative(4.157f, -2.408f)
                close()
            }
        }.build()
    ),
}
