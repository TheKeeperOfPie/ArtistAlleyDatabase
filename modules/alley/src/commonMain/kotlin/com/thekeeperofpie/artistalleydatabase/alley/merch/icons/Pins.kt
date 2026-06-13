package com.thekeeperofpie.artistalleydatabase.alley.merch.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchIcons

@Suppress("UnusedReceiverParameter")
val MerchIcons.Pins: ImageVector
    get() {
        if (_Pins != null) {
            return _Pins!!
        }
        _Pins = ImageVector.Builder(
            name = "Pins",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(44.7f, 38.18f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, 0.25f)
                verticalLineToRelative(8.58f)
                horizontalLineTo(4.8f)
                verticalLineToRelative(-8.28f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, -0.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, 0.25f)
                verticalLineToRelative(8.53f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.25f)
                horizontalLineTo(44.7f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, -0.25f)
                verticalLineToRelative(-8.83f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, -0.25f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(23.82f, 10.775f)
                curveToRelative(-1.44f, 0.112f, -3.033f, 0.559f, -4.716f, 1.55f)
                lineToRelative(0.144f, 0.243f)
                lineToRelative(-0.19f, -0.248f)
                curveToRelative(-1.055f, 0.797f, -2.011f, 2.187f, -2.72f, 3.518f)
                arcToRelative(15f, 15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.846f, 1.871f)
                curveToRelative(-0.201f, 0.556f, -0.322f, 1f, -0.312f, 1.338f)
                curveToRelative(0.032f, 1.105f, 0.523f, 3.877f, 0.523f, 3.877f)
                lineToRelative(0.006f, 0.025f)
                lineToRelative(0.01f, 0.026f)
                reflectiveCurveToRelative(0.86f, 2.081f, 3.4f, 2.511f)
                lineToRelative(0.39f, 0.067f)
                lineToRelative(-0.784f, -2.75f)
                lineToRelative(2.38f, -3.088f)
                curveToRelative(0.136f, 0.22f, 0.45f, 0.754f, 1.131f, 1.535f)
                curveToRelative(0.81f, 0.927f, 1.892f, 1.915f, 3.096f, 2.049f)
                lineToRelative(0.152f, 0.015f)
                lineToRelative(2.752f, -4.185f)
                lineToRelative(2.286f, 3.584f)
                lineToRelative(-0.74f, 2.894f)
                lineToRelative(0.415f, -0.117f)
                curveToRelative(1.986f, -0.558f, 2.966f, -2.193f, 3.434f, -3.646f)
                reflectiveCurveToRelative(0.45f, -2.76f, 0.45f, -2.764f)
                curveToRelative(0f, -0.003f, 0.002f, -0.659f, -0.37f, -1.707f)
                curveToRelative(-0.373f, -1.05f, -1.123f, -2.516f, -2.623f, -4.209f)
                lineToRelative(-0.01f, -0.01f)
                lineToRelative(-0.008f, -0.008f)
                reflectiveCurveToRelative(-1.306f, -1.207f, -3.449f, -1.908f)
                arcToRelative(9.8f, 9.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.8f, -0.463f)
                moveToRelative(0.043f, 0.495f)
                arcToRelative(9.3f, 9.3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.604f, 0.445f)
                curveToRelative(2.028f, 0.663f, 3.24f, 1.778f, 3.254f, 1.79f)
                curveToRelative(1.453f, 1.644f, 2.168f, 3.049f, 2.52f, 4.036f)
                curveToRelative(0.351f, 0.99f, 0.34f, 1.535f, 0.34f, 1.535f)
                verticalLineToRelative(0.008f)
                reflectiveCurveToRelative(0.015f, 1.241f, -0.425f, 2.607f)
                curveToRelative(-0.39f, 1.212f, -1.225f, 2.392f, -2.629f, 3.016f)
                lineToRelative(0.532f, -2.08f)
                lineToRelative(-2.815f, -4.416f)
                lineToRelative(-2.984f, 4.533f)
                curveToRelative(-0.92f, -0.165f, -1.905f, -0.975f, -2.647f, -1.824f)
                arcToRelative(13.6f, 13.6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.289f, -1.748f)
                lineToRelative(-0.187f, -0.317f)
                lineToRelative(-2.961f, 3.842f)
                lineToRelative(0.613f, 2.15f)
                curveToRelative(-1.917f, -0.532f, -2.591f, -2.03f, -2.6f, -2.05f)
                curveToRelative(-0.01f, -0.057f, -0.481f, -2.8f, -0.51f, -3.764f)
                curveToRelative(-0.005f, -0.183f, 0.09f, -0.624f, 0.282f, -1.152f)
                curveToRelative(0.192f, -0.529f, 0.474f, -1.16f, 0.818f, -1.807f)
                curveToRelative(0.685f, -1.284f, 1.63f, -2.62f, 2.569f, -3.336f)
                lineToRelative(0.01f, 0.018f)
                curveToRelative(1.621f, -0.954f, 3.138f, -1.381f, 4.505f, -1.486f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(27.95f, 18.514f)
                lineToRelative(-2.862f, 4.422f)
                curveToRelative(-0.21f, 0.05f, -0.551f, -0.042f, -0.97f, -0.336f)
                curveToRelative(-0.443f, -0.31f, -0.93f, -0.783f, -1.378f, -1.272f)
                arcToRelative(23f, 23f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.625f, -2.021f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.41f, 0.002f)
                lineToRelative(-2.371f, 3.43f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.035f, 0.21f)
                lineToRelative(0.53f, 1.856f)
                lineToRelative(-0.12f, -0.098f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.406f, 0.162f)
                reflectiveCurveToRelative(-0.126f, 1.029f, 0.597f, 2.031f)
                reflectiveCurveToRelative(2.275f, 1.96f, 5.46f, 1.96f)
                curveToRelative(3.165f, 0f, 4.792f, -0.569f, 5.654f, -1.323f)
                reflectiveCurveToRelative(0.892f, -1.688f, 0.925f, -2.158f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.072f, -0.195f)
                lineToRelative(-0.01f, -0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.177f, -0.074f)
                horizontalLineToRelative(-0.223f)
                lineToRelative(0.498f, -2.399f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.035f, -0.185f)
                lineToRelative(-2.549f, -4f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.422f, -0.002f)
                moveToRelative(0.208f, 0.597f)
                lineToRelative(2.287f, 3.588f)
                lineToRelative(-0.54f, 2.6f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.245f, 0.3f)
                horizontalLineToRelative(0.254f)
                curveToRelative(-0.036f, 0.47f, -0.108f, 1.027f, -0.718f, 1.561f)
                curveToRelative(-0.719f, 0.629f, -2.222f, 1.2f, -5.327f, 1.2f)
                curveToRelative(-3.085f, 0f, -4.438f, -0.898f, -5.052f, -1.75f)
                arcToRelative(2.7f, 2.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.485f, -1.16f)
                lineToRelative(0.309f, 0.253f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.4f, -0.262f)
                lineToRelative(-0.719f, -2.515f)
                lineToRelative(2.114f, -3.059f)
                curveToRelative(0.162f, 0.227f, 0.646f, 0.922f, 1.445f, 1.797f)
                curveToRelative(0.46f, 0.504f, 0.965f, 0.997f, 1.46f, 1.344f)
                curveToRelative(0.493f, 0.347f, 0.995f, 0.582f, 1.47f, 0.418f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.129f, -0.1f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.277f, 11.318f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.275f, 0.102f)
                lineToRelative(-2.85f, 4.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.035f, 0.2f)
                lineToRelative(0.88f, 3.638f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.482f, 0.013f)
                lineToRelative(0.998f, -3.294f)
                lineToRelative(3.011f, -3.934f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.13f, -0.395f)
                close()
                moveTo(17.317f, 11.85f)
                lineTo(18.867f, 12.281f)
                lineTo(16.051f, 15.957f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.041f, 0.08f)
                lineToRelative(-0.744f, 2.46f)
                lineToRelative(-0.637f, -2.64f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(32.172f, 11.318f)
                lineToRelative(-2.078f, 0.58f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.133f, 0.395f)
                lineToRelative(3.006f, 3.924f)
                lineToRelative(0.709f, 3.285f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.482f, 0.025f)
                lineToRelative(1.17f, -3.64f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.031f, -0.217f)
                lineToRelative(-2.85f, -4.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.275f, -0.102f)
                moveToRelative(-0.04f, 0.532f)
                lineToRelative(2.682f, 4f)
                lineToRelative(-0.847f, 2.634f)
                lineToRelative(-0.522f, -2.427f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.047f, -0.1f)
                lineToRelative(-2.814f, -3.676f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(16.14f, 23.33f)
                lineToRelative(-0.47f, 0.92f)
                reflectiveCurveToRelative(0.36f, 3.15f, 3.34f, 3.15f)
                curveToRelative(0f, 0f, -0.5f, -2.06f, -0.47f, -2.29f)
                reflectiveCurveToRelative(-2.01f, -0.44f, -2.41f, -1.78f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.13f, 23.08f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.161f, 0.219f)
                lineToRelative(-0.051f, -0.082f)
                lineToRelative(-0.47f, 0.92f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.026f, 0.142f)
                reflectiveCurveToRelative(0.094f, 0.834f, 0.586f, 1.672f)
                reflectiveCurveToRelative(1.424f, 1.7f, 3.002f, 1.7f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.244f, -0.31f)
                reflectiveCurveToRelative(-0.126f, -0.513f, -0.246f, -1.054f)
                curveToRelative(-0.06f, -0.27f, -0.12f, -0.549f, -0.162f, -0.77f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.047f, -0.277f)
                curveToRelative(-0.01f, -0.071f, -0.007f, -0.137f, -0.012f, -0.097f)
                arcToRelative(0.32f, 0.32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.082f, -0.247f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.121f, -0.091f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.264f, -0.116f)
                curveToRelative(-0.2f, -0.075f, -0.457f, -0.162f, -0.722f, -0.28f)
                curveToRelative(-0.53f, -0.238f, -1.061f, -0.59f, -1.229f, -1.151f)
                lineToRelative(-0.064f, 0.088f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.164f, -0.266f)
                close()
                moveTo(16.172f, 23.818f)
                arcToRelative(2.9f, 2.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.223f, 1.047f)
                curveToRelative(0.29f, 0.13f, 0.563f, 0.221f, 0.75f, 0.291f)
                curveToRelative(0.075f, 0.029f, 0.12f, 0.051f, 0.156f, 0.069f)
                curveToRelative(0.003f, 0.025f, 0f, 0.053f, 0.004f, 0.084f)
                quadToRelative(0.017f, 0.129f, 0.05f, 0.302f)
                curveToRelative(0.044f, 0.23f, 0.104f, 0.512f, 0.165f, 0.785f)
                curveToRelative(0.075f, 0.341f, 0.106f, 0.463f, 0.154f, 0.663f)
                curveToRelative(-1.14f, -0.125f, -1.848f, -0.698f, -2.237f, -1.36f)
                curveToRelative(-0.416f, -0.71f, -0.493f, -1.372f, -0.5f, -1.424f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(33.04f, 23.2f)
                lineToRelative(0.47f, 0.92f)
                reflectiveCurveToRelative(-0.36f, 3.15f, -3.34f, 3.15f)
                curveToRelative(0f, 0f, 0.5f, -2.06f, 0.47f, -2.29f)
                reflectiveCurveToRelative(2.01f, -0.44f, 2.41f, -1.78f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(33.04f, 22.95f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.163f, 0.267f)
                lineToRelative(-0.066f, -0.088f)
                curveToRelative(-0.168f, 0.562f, -0.699f, 0.911f, -1.229f, 1.148f)
                curveToRelative(-0.265f, 0.119f, -0.522f, 0.206f, -0.723f, 0.282f)
                curveToRelative(-0.1f, 0.037f, -0.185f, 0.072f, -0.263f, 0.117f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.121f, 0.092f)
                arcToRelative(0.32f, 0.32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.082f, 0.244f)
                curveToRelative(-0.006f, -0.04f, -0.002f, 0.026f, -0.012f, 0.097f)
                reflectiveCurveToRelative(-0.026f, 0.17f, -0.047f, 0.28f)
                curveToRelative(-0.042f, 0.22f, -0.102f, 0.497f, -0.162f, 0.767f)
                curveToRelative(-0.12f, 0.541f, -0.244f, 1.055f, -0.244f, 1.055f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.242f, 0.309f)
                curveToRelative(1.578f, 0f, 2.512f, -0.86f, 3.004f, -1.698f)
                arcToRelative(4.7f, 4.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.584f, -1.674f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.026f, -0.142f)
                lineToRelative(-0.47f, -0.92f)
                lineToRelative(-0.05f, 0.082f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.161f, -0.219f)
                close()
                moveTo(33.01f, 23.691f)
                lineTo(33.242f, 24.146f)
                curveToRelative(-0.006f, 0.052f, -0.083f, 0.713f, -0.5f, 1.422f)
                curveToRelative(-0.389f, 0.662f, -1.095f, 1.235f, -2.236f, 1.36f)
                curveToRelative(0.047f, -0.2f, 0.078f, -0.322f, 0.154f, -0.662f)
                curveToRelative(0.061f, -0.274f, 0.12f, -0.555f, 0.164f, -0.786f)
                quadToRelative(0.034f, -0.173f, 0.053f, -0.302f)
                curveToRelative(0.004f, -0.032f, 0f, -0.06f, 0.004f, -0.086f)
                curveToRelative(0.036f, -0.018f, 0.08f, -0.037f, 0.154f, -0.065f)
                curveToRelative(0.186f, -0.07f, 0.459f, -0.162f, 0.75f, -0.293f)
                curveToRelative(0.45f, -0.201f, 0.907f, -0.557f, 1.225f, -1.043f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.104f, 12.93f)
                reflectiveCurveToRelative(-0.504f, 0.034f, -1.08f, 0.187f)
                reflectiveCurveToRelative(-1.254f, 0.412f, -1.588f, 0.975f)
                curveToRelative(-0.084f, 0.14f, -0.282f, 0.524f, -0.588f, 1.138f)
                arcToRelative(165.088f, 165.088f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.379f, 9.596f)
                curveToRelative(-1.1f, 2.642f, -2.127f, 5.36f, -2.768f, 7.637f)
                curveToRelative(-0.32f, 1.139f, -0.545f, 2.168f, -0.633f, 3.03f)
                curveToRelative(-0.087f, 0.86f, -0.049f, 1.557f, 0.223f, 2.056f)
                curveToRelative(0.916f, 1.68f, 2.3f, 3.66f, 3.582f, 5.236f)
                curveToRelative(0.642f, 0.788f, 1.256f, 1.474f, 1.781f, 1.975f)
                curveToRelative(0.263f, 0.25f, 0.502f, 0.454f, 0.717f, 0.603f)
                curveToRelative(0.215f, 0.15f, 0.4f, 0.253f, 0.613f, 0.266f)
                horizontalLineToRelative(0.004f)
                curveToRelative(0.308f, 0.015f, 0.57f, -0.107f, 0.713f, -0.303f)
                curveToRelative(0.142f, -0.195f, 0.176f, -0.423f, 0.18f, -0.633f)
                curveToRelative(0.006f, -0.348f, -0.074f, -0.573f, -0.104f, -0.673f)
                curveToRelative(0.047f, -0.07f, 0.635f, -0.953f, 1.272f, -2.012f)
                curveToRelative(0.33f, -0.55f, 0.662f, -1.128f, 0.914f, -1.627f)
                curveToRelative(0.126f, -0.25f, 0.232f, -0.48f, 0.308f, -0.682f)
                curveToRelative(0.077f, -0.202f, 0.13f, -0.368f, 0.13f, -0.539f)
                curveToRelative(0f, -0.41f, 0.04f, -3.839f, 0.124f, -7.248f)
                curveToRelative(0.043f, -1.704f, 0.097f, -3.407f, 0.162f, -4.73f)
                curveToRelative(0.033f, -0.662f, 0.068f, -1.23f, 0.106f, -1.653f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.059f, -0.517f)
                curveToRelative(0.02f, -0.13f, 0.049f, -0.224f, 0.046f, -0.219f)
                curveToRelative(0.38f, -0.844f, 0.413f, -1.582f, 0.413f, -1.582f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.006f, -0.066f)
                lineToRelative(-0.952f, -4.25f)
                verticalLineToRelative(-0.002f)
                lineToRelative(-0.724f, -3.026f)
                lineToRelative(1.7f, -2.539f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.04f, -0.139f)
                verticalLineToRelative(-0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.266f, -0.25f)
                moveToRelative(-0.508f, 0.593f)
                lineToRelative(-1.444f, 2.159f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.035f, 0.197f)
                lineToRelative(0.75f, 3.127f)
                lineToRelative(0.94f, 4.201f)
                curveToRelative(-0.001f, 0.016f, -0.03f, 0.635f, -0.366f, 1.38f)
                curveToRelative(-0.047f, 0.106f, -0.061f, 0.204f, -0.084f, 0.35f)
                arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.062f, 0.547f)
                curveToRelative(-0.04f, 0.435f, -0.075f, 1.007f, -0.108f, 1.672f)
                arcToRelative(207f, 207f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.162f, 4.744f)
                curveToRelative(-0.085f, 3.414f, -0.125f, 6.82f, -0.125f, 7.26f)
                curveToRelative(0f, 0.042f, -0.03f, 0.185f, -0.097f, 0.361f)
                arcToRelative(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.287f, 0.635f)
                curveToRelative(-0.242f, 0.48f, -0.568f, 1.05f, -0.895f, 1.594f)
                curveToRelative(-0.654f, 1.088f, -1.309f, 2.072f, -1.309f, 2.072f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.03f, 0.215f)
                reflectiveCurveToRelative(0.104f, 0.342f, 0.099f, 0.647f)
                curveToRelative(-0.003f, 0.152f, -0.036f, 0.281f, -0.084f, 0.347f)
                reflectiveCurveToRelative(-0.1f, 0.109f, -0.283f, 0.1f)
                curveToRelative(-0.032f, -0.002f, -0.177f, -0.055f, -0.358f, -0.18f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.656f, -0.553f)
                curveToRelative(-0.5f, -0.477f, -1.108f, -1.152f, -1.74f, -1.93f)
                curveToRelative(-1.265f, -1.553f, -2.636f, -3.518f, -3.53f, -5.157f)
                curveToRelative(-0.18f, -0.332f, -0.246f, -0.953f, -0.164f, -1.768f)
                curveToRelative(0.083f, -0.815f, 0.302f, -1.824f, 0.618f, -2.945f)
                curveToRelative(0.63f, -2.244f, 1.651f, -4.949f, 2.746f, -7.58f)
                arcToRelative(165f, 165f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.365f, -9.565f)
                curveToRelative(0.305f, -0.61f, 0.519f, -1.019f, 0.57f, -1.105f)
                curveToRelative(0.206f, -0.347f, 0.764f, -0.61f, 1.287f, -0.748f)
                curveToRelative(0.236f, -0.063f, 0.27f, -0.05f, 0.444f, -0.077f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(33.521f, 12.932f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.271f, 0.248f)
                verticalLineToRelative(0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.03f, 0.117f)
                lineToRelative(1.355f, 2.547f)
                lineToRelative(-0.729f, 3.039f)
                verticalLineToRelative(0.002f)
                lineToRelative(-0.95f, 4.25f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.005f, 0.066f)
                reflectiveCurveToRelative(0.03f, 0.738f, 0.41f, 1.582f)
                curveToRelative(-0.002f, -0.005f, 0.028f, 0.089f, 0.047f, 0.219f)
                curveToRelative(0.02f, 0.13f, 0.04f, 0.306f, 0.059f, 0.517f)
                curveToRelative(0.038f, 0.423f, 0.074f, 0.99f, 0.107f, 1.653f)
                curveToRelative(0.066f, 1.324f, 0.118f, 3.026f, 0.16f, 4.73f)
                curveToRelative(0.085f, 3.41f, 0.125f, 6.839f, 0.125f, 7.248f)
                curveToRelative(0f, 0.171f, 0.054f, 0.337f, 0.131f, 0.54f)
                curveToRelative(0.077f, 0.201f, 0.181f, 0.431f, 0.307f, 0.68f)
                curveToRelative(0.251f, 0.5f, 0.583f, 1.078f, 0.914f, 1.628f)
                curveToRelative(0.636f, 1.059f, 1.225f, 1.942f, 1.271f, 2.012f)
                curveToRelative(-0.029f, 0.1f, -0.106f, 0.327f, -0.1f, 0.677f)
                curveToRelative(0.005f, 0.21f, 0.039f, 0.438f, 0.18f, 0.633f)
                curveToRelative(0.142f, 0.195f, 0.407f, 0.318f, 0.713f, 0.299f)
                curveToRelative(0.215f, -0.013f, 0.398f, -0.117f, 0.614f, -0.266f)
                curveToRelative(0.215f, -0.148f, 0.454f, -0.353f, 0.716f, -0.603f)
                curveToRelative(0.526f, -0.5f, 1.14f, -1.187f, 1.782f, -1.975f)
                curveToRelative(1.283f, -1.576f, 2.666f, -3.556f, 3.582f, -5.236f)
                curveToRelative(0.272f, -0.499f, 0.31f, -1.195f, 0.222f, -2.057f)
                curveToRelative(-0.087f, -0.861f, -0.31f, -1.89f, -0.63f, -3.029f)
                curveToRelative(-0.64f, -2.278f, -1.668f, -4.995f, -2.766f, -7.637f)
                arcToRelative(164f, 164f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.379f, -9.595f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.592f, -1.141f)
                curveToRelative(-0.326f, -0.54f, -0.913f, -0.813f, -1.4f, -0.969f)
                curveToRelative(-0.488f, -0.155f, -0.903f, -0.19f, -0.903f, -0.19f)
                moveToRelative(0.446f, 0.601f)
                curveToRelative(0.129f, 0.026f, 0.14f, 0.012f, 0.306f, 0.065f)
                curveToRelative(0.43f, 0.137f, 0.9f, 0.382f, 1.123f, 0.752f)
                curveToRelative(0.051f, 0.084f, 0.268f, 0.494f, 0.573f, 1.105f)
                arcToRelative(163.22f, 163.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.365f, 9.563f)
                curveToRelative(1.094f, 2.631f, 2.114f, 5.336f, 2.744f, 7.58f)
                curveToRelative(0.315f, 1.121f, 0.533f, 2.13f, 0.615f, 2.945f)
                curveToRelative(0.083f, 0.815f, 0.019f, 1.436f, -0.162f, 1.768f)
                curveToRelative(-0.894f, 1.639f, -2.266f, 3.604f, -3.531f, 5.158f)
                curveToRelative(-0.632f, 0.777f, -1.24f, 1.452f, -1.74f, 1.93f)
                curveToRelative(-0.25f, 0.238f, -0.475f, 0.427f, -0.656f, 0.552f)
                reflectiveCurveToRelative(-0.33f, 0.178f, -0.36f, 0.18f)
                curveToRelative(-0.183f, 0.011f, -0.23f, -0.029f, -0.277f, -0.094f)
                reflectiveCurveToRelative(-0.081f, -0.196f, -0.084f, -0.35f)
                curveToRelative(-0.006f, -0.306f, 0.096f, -0.652f, 0.096f, -0.652f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.032f, -0.213f)
                reflectiveCurveToRelative(-0.655f, -0.984f, -1.308f, -2.072f)
                arcToRelative(26f, 26f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.895f, -1.594f)
                curveToRelative(-0.12f, -0.24f, -0.22f, -0.458f, -0.287f, -0.635f)
                arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.098f, -0.36f)
                curveToRelative(0f, -0.442f, -0.04f, -3.847f, -0.125f, -7.26f)
                arcToRelative(208f, 208f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.162f, -4.745f)
                arcToRelative(44f, 44f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.107f, -1.672f)
                arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.063f, -0.547f)
                curveToRelative(-0.022f, -0.146f, -0.036f, -0.244f, -0.084f, -0.35f)
                curveToRelative(-0.335f, -0.745f, -0.364f, -1.364f, -0.365f, -1.38f)
                lineToRelative(0.942f, -4.201f)
                lineToRelative(0.748f, -3.127f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.022f, -0.176f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(26.459f, 25.523f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.176f, 0.018f)
                lineToRelative(-1.777f, 2.146f)
                lineToRelative(-1.535f, -2.052f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.176f, -0.026f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.025f, 0.176f)
                lineToRelative(1.63f, 2.18f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.196f, 0.006f)
                lineToRelative(1.88f, -2.272f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.017f, -0.176f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(25.879f, 26.164f)
                lineToRelative(-2.51f, 0.012f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, 0.125f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.127f, 0.125f)
                lineToRelative(2.51f, -0.012f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.125f, -0.125f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.127f, -0.125f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21.85f, 23.58f)
                arcToRelative(0.48f, 0.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.48f, 0.48f)
                arcToRelative(0.48f, 0.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.48f, -0.48f)
                arcToRelative(0.48f, 0.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.48f, -0.48f)
                arcToRelative(0.48f, 0.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.48f, 0.48f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21.37f, 22.975f)
                arcToRelative(0.606f, 0.606f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 1.21f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.605f, -0.605f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.606f, -0.605f)
                moveToRelative(0f, 0.25f)
                curveToRelative(0.197f, 0f, 0.355f, 0.158f, 0.355f, 0.355f)
                arcToRelative(0.354f, 0.354f, 0f, isMoreThanHalf = true, isPositiveArc = true, -0.71f, 0f)
                curveToRelative(0f, -0.197f, 0.157f, -0.355f, 0.354f, -0.355f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(28.5f, 23.58f)
                arcToRelative(0.48f, 0.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.48f, 0.48f)
                arcToRelative(0.48f, 0.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.48f, -0.48f)
                arcToRelative(0.48f, 0.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.48f, -0.48f)
                arcToRelative(0.48f, 0.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.48f, 0.48f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(28.02f, 22.975f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.606f, 0.605f)
                curveToRelative(0f, 0.333f, 0.273f, 0.606f, 0.606f, 0.606f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.605f, -0.606f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.605f, -0.605f)
                moveToRelative(0f, 0.25f)
                curveToRelative(0.197f, 0f, 0.355f, 0.158f, 0.355f, 0.355f)
                arcToRelative(0.354f, 0.354f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.355f, 0.356f)
                arcToRelative(0.354f, 0.354f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.356f, -0.356f)
                curveToRelative(0f, -0.197f, 0.158f, -0.355f, 0.356f, -0.355f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(25.203f, 8.701f)
                arcToRelative(9.7f, 9.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.037f, 0.127f)
                curveToRelative(-1.543f, 0.32f, -2.291f, 0.894f, -2.42f, 0.986f)
                lineToRelative(-3.2f, -0.058f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.269f, 0.105f)
                lineToRelative(-1.402f, 1.375f)
                lineToRelative(-2.38f, 0.703f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.233f, 0.2f)
                reflectiveCurveToRelative(-2.268f, 4.782f, -4.53f, 10.039f)
                curveToRelative(-1.13f, 2.628f, -2.26f, 5.375f, -3.107f, 7.709f)
                reflectiveCurveToRelative(-1.425f, 4.202f, -1.41f, 5.299f)
                curveToRelative(0.014f, 1.057f, 0.493f, 2.28f, 1.176f, 3.562f)
                reflectiveCurveToRelative(1.578f, 2.617f, 2.47f, 3.832f)
                arcToRelative(61f, 61f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.56f, 4.383f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.167f, 0.105f)
                reflectiveCurveToRelative(1.067f, 0.342f, 2.152f, 0.282f)
                curveToRelative(0.543f, -0.03f, 1.113f, -0.16f, 1.55f, -0.545f)
                curveToRelative(0.405f, -0.36f, 0.612f, -0.955f, 0.57f, -1.705f)
                lineToRelative(1.613f, -4.493f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.021f, -0.103f)
                lineToRelative(0.742f, -11.67f)
                curveToRelative(0.412f, 0.38f, 1.008f, 0.772f, 1.76f, 0.82f)
                lineToRelative(-0.115f, -0.025f)
                reflectiveCurveToRelative(6.166f, 2.5f, 11.437f, -0.01f)
                curveToRelative(0.875f, -0.41f, 1.256f, -0.774f, 1.608f, -1.058f)
                lineToRelative(0.57f, 11.937f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.05f, 0.174f)
                lineToRelative(2.661f, 4.48f)
                lineToRelative(-0.05f, -0.232f)
                curveToRelative(-0.088f, 0.817f, 0.08f, 1.441f, 0.425f, 1.867f)
                reflectiveCurveToRelative(0.832f, 0.624f, 1.291f, 0.707f)
                curveToRelative(0.918f, 0.165f, 1.816f, -0.092f, 1.816f, -0.092f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.182f, -0.113f)
                reflectiveCurveToRelative(1.765f, -1.996f, 3.54f, -4.469f)
                curveToRelative(0.886f, -1.236f, 1.778f, -2.593f, 2.456f, -3.892f)
                curveToRelative(0.678f, -1.3f, 1.154f, -2.535f, 1.168f, -3.594f)
                curveToRelative(0.015f, -1.1f, -0.566f, -2.96f, -1.418f, -5.281f)
                curveToRelative(-0.851f, -2.321f, -1.987f, -5.05f, -3.125f, -7.66f)
                curveToRelative(-2.274f, -5.22f, -4.554f, -9.965f, -4.554f, -9.965f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.235f, -0.198f)
                lineToRelative(-2.387f, -0.685f)
                lineToRelative(-1.384f, -1.877f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.332f, -0.152f)
                lineToRelative(-3.534f, 0.289f)
                curveToRelative(-1.42f, -0.626f, -2.725f, -0.98f, -3.863f, -1.104f)
                moveToRelative(-0.08f, 0.742f)
                curveToRelative(1.103f, 0.12f, 2.37f, 0.441f, 3.791f, 1.088f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.51f, -0.328f)
                lineToRelative(-0.324f, 0.352f)
                lineToRelative(3.353f, -0.274f)
                lineToRelative(1.336f, 1.81f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.197f, 0.14f)
                lineToRelative(2.346f, 0.673f)
                curveToRelative(0.068f, 0.142f, 2.24f, 4.663f, 4.473f, 9.787f)
                curveToRelative(1.134f, 2.603f, 2.266f, 5.323f, 3.11f, 7.62f)
                curveToRelative(0.842f, 2.297f, 1.38f, 4.223f, 1.37f, 5.013f)
                curveToRelative(-0.01f, 0.83f, -0.432f, 2.006f, -1.084f, 3.256f)
                reflectiveCurveToRelative(-1.526f, 2.586f, -2.4f, 3.805f)
                curveToRelative(-1.706f, 2.377f, -3.324f, 4.205f, -3.406f, 4.299f)
                curveToRelative(-0.094f, 0.022f, -0.743f, 0.188f, -1.39f, 0.072f)
                curveToRelative(-0.345f, -0.062f, -0.642f, -0.196f, -0.84f, -0.442f)
                curveToRelative(-0.2f, -0.245f, -0.335f, -0.631f, -0.263f, -1.314f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.05f, -0.232f)
                lineToRelative(-2.612f, -4.399f)
                lineToRelative(-0.595f, -12.496f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.622f, -0.266f)
                reflectiveCurveToRelative(-0.959f, 0.835f, -2.023f, 1.334f)
                horizontalLineToRelative(-0.002f)
                curveToRelative(-4.888f, 2.329f, -10.84f, -0.01f, -10.84f, -0.01f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.113f, -0.025f)
                curveToRelative(-0.989f, -0.064f, -1.81f, -1.222f, -1.81f, -1.222f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.69f, 0.183f)
                lineToRelative(-0.795f, 12.535f)
                lineToRelative(-1.604f, 4.461f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.019f, 0.166f)
                curveToRelative(0.071f, 0.669f, -0.088f, 0.997f, -0.334f, 1.215f)
                reflectiveCurveToRelative(-0.646f, 0.33f, -1.094f, 0.356f)
                curveToRelative(-0.852f, 0.047f, -1.711f, -0.211f, -1.799f, -0.237f)
                curveToRelative(-0.073f, -0.08f, -1.714f, -1.885f, -3.433f, -4.226f)
                curveToRelative(-0.88f, -1.197f, -1.759f, -2.508f, -2.414f, -3.74f)
                reflectiveCurveToRelative(-1.077f, -2.394f, -1.088f, -3.221f)
                curveToRelative(-0.01f, -0.788f, 0.527f, -2.724f, 1.365f, -5.033f)
                reflectiveCurveToRelative(1.962f, -5.048f, 3.09f, -7.668f)
                curveToRelative(2.221f, -5.163f, 4.385f, -9.723f, 4.451f, -9.864f)
                lineToRelative(2.305f, -0.681f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.156f, -0.092f)
                lineToRelative(1.357f, -1.33f)
                lineToRelative(3.084f, 0.056f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.235f, -0.076f)
                reflectiveCurveToRelative(0.786f, -0.609f, 2.31f, -0.926f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.805f, -0.119f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.55f, 1.31f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, 0.25f)
                verticalLineToRelative(29.28f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, -0.25f)
                verticalLineTo(1.81f)
                horizontalLineToRelative(39.65f)
                verticalLineToRelative(29.76f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, -0.25f)
                verticalLineTo(1.56f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, -0.25f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(31.48f, 4.45f)
                horizontalLineToRelative(-4.17f)
                reflectiveCurveToRelative(0.03f, -0.06f, 0.03f, -0.09f)
                curveToRelative(0f, -0.39f, -1.2f, -1.06f, -2.69f, -1.06f)
                reflectiveCurveToRelative(-2.69f, 0.67f, -2.69f, 1.06f)
                curveToRelative(0f, 0.03f, 0.02f, 0.06f, 0.03f, 0.09f)
                horizontalLineToRelative(-4.24f)
                curveToRelative(-0.6f, 0f, -1.09f, 0.49f, -1.09f, 1.09f)
                reflectiveCurveToRelative(0.49f, 1.09f, 1.09f, 1.09f)
                horizontalLineToRelative(13.71f)
                curveToRelative(0.6f, 0f, 1.09f, -0.49f, 1.09f, -1.09f)
                reflectiveCurveToRelative(-0.49f, -1.09f, -1.09f, -1.09f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(24.65f, 3.05f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.005f, 0.405f)
                curveToRelative(-0.26f, 0.116f, -0.475f, 0.246f, -0.637f, 0.387f)
                curveToRelative(-0.112f, 0.097f, -0.155f, 0.228f, -0.205f, 0.357f)
                horizontalLineTo(17.75f)
                curveToRelative(-0.736f, 0f, -1.34f, 0.604f, -1.34f, 1.34f)
                reflectiveCurveToRelative(0.604f, 1.342f, 1.34f, 1.342f)
                horizontalLineToRelative(13.71f)
                curveToRelative(0.737f, 0f, 1.34f, -0.606f, 1.34f, -1.342f)
                curveToRelative(0f, -0.728f, -0.594f, -1.32f, -1.32f, -1.332f)
                verticalLineToRelative(-0.008f)
                horizontalLineToRelative(-3.982f)
                curveToRelative(-0.05f, -0.129f, -0.093f, -0.26f, -0.205f, -0.357f)
                arcToRelative(2.6f, 2.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.637f, -0.387f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.006f, -0.404f)
                moveToRelative(0f, 0.5f)
                curveToRelative(0.708f, 0f, 1.351f, 0.161f, 1.803f, 0.362f)
                curveToRelative(0.226f, 0.1f, 0.403f, 0.212f, 0.512f, 0.307f)
                curveToRelative(0.11f, 0.094f, 0.125f, 0.171f, 0.125f, 0.14f)
                curveToRelative(0f, -0.026f, 0f, -0.008f, 0.002f, -0.013f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.219f, 0.353f)
                horizontalLineToRelative(4.15f)
                arcToRelative(0.84f, 0.84f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 1.682f)
                horizontalLineTo(17.75f)
                arcToRelative(0.841f, 0.841f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -1.682f)
                horizontalLineToRelative(4.24f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.237f, -0.328f)
                curveToRelative(-0.026f, -0.077f, -0.016f, -0.093f, -0.016f, -0.012f)
                curveToRelative(0f, 0.031f, 0.014f, -0.046f, 0.123f, -0.14f)
                arcToRelative(2.2f, 2.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.514f, -0.307f)
                arcToRelative(4.6f, 4.6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.802f, -0.361f)
            }
        }.build()

        return _Pins!!
    }

@Suppress("ObjectPropertyName")
private var _Pins: ImageVector? = null
