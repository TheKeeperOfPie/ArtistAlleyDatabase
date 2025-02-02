package com.thekeeperofpie.artistalleydatabase.alley.ui

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

object Logos {
    // https://www.artstation.com/about/logo
    val artStation by lazy {
        ImageVector.Builder(
            name = "ArtStation",
            defaultWidth = 16.dp,
            defaultHeight = (93.099998 / 105.8 * 16).dp,
            viewportWidth = 105.8f,
            viewportHeight = 93.100001f
        ).apply {
            group(
                scaleX = 1f,
                scaleY = 1f,
                translationX = -51.4f,
                translationY = -51.5f,
                pivotX = 0f,
                pivotY = 0f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
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
                    pathFillType = PathFillType.NonZero
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
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(60.2f, 108.1f)
                    lineTo(108.1f, 108.1f)
                    lineTo(84.2f, 66.6f)
                    close()
                }
            }
        }.build()
    }

    // https://nucleoapp.com/social-media-icons
    val bluesky by lazy {
        ImageVector.Builder(
            name = "Bluesky",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            materialPath(
                fillAlpha = 1.0f,
                strokeAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
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
    }

    // https://carrd.co/docs/general/brand-assets
    val carrd by lazy {
        ImageVector.Builder(
            name = "Carrd",
            defaultWidth = 16.dp,
            defaultHeight = (75.631973 / 60.271461 * 16).dp,
            viewportWidth = 60.271461f,
            viewportHeight = 75.631973f
        ).apply {
            group(
                scaleX = 1f,
                scaleY = 1f,
                translationX = -35.88046f,
                translationY = -38.078525f,
                pivotX = 0f,
                pivotY = 0f,
            ) {
                materialPath(
                    fillAlpha = 1.0f,
                    strokeAlpha = 1.0f,
                    pathFillType = PathFillType.NonZero
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
    }

    // https://nucleoapp.com/social-media-icons
    val deviantArt by lazy {
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
    }

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/discord.svg
    val discord by lazy {
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
    }

    // https://gamejolt.com/about
    val gameJolt by lazy {
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
    }

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/instagram.svg
    val instagram by lazy {
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
    }

    // https://more.ko-fi.com/brand-assets
    val koFi by lazy {
        ImageVector.Builder(
            name = "Ko-Fi",
            defaultWidth = 241.dp,
            defaultHeight = 194.dp,
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
    }

    // https://uxwing.com/linktree-logo-icon/
    val linktree by lazy {
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
    }

    // https://www.patreon.com/brand
    val patreon by lazy {
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
    }

    // https://github.com/twbs/icons/blob/main/icons/threads.svg
    val threads by lazy {
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
    }

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/tiktok.svg
    val tikTok by lazy {
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
    }

    // https://uxwing.com/tumblr-icon/
    val tumblr by lazy {
        ImageVector.Builder(
            name = "Tumblr",
            defaultWidth = 289.999.dp,
            defaultHeight = 512.184.dp,
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
    }

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/twitch.svg
    val twitch by lazy {
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
    }

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/twitter-x.svg
    val x by lazy {
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
    }

    // https://github.com/twbs/icons/blob/7ea4d7c9dc85433310fecc399f65a8fa3af5162f/icons/youtube.svg
    val youTube by lazy {
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
    }
}
