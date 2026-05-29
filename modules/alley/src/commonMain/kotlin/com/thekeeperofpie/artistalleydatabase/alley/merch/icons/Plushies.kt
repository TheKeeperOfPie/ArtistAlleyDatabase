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
val MerchIcons.Plushies: ImageVector
    get() {
        if (_Plushies != null) {
            return _Plushies!!
        }
        _Plushies = ImageVector.Builder(
            name = "Plushies",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF010101)),
                stroke = SolidColor(Color(0xFF010101)),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(12.82f, 17.56f)
                lineToRelative(-1.2f, -1.75f)
                reflectiveCurveToRelative(-0.23f, -4.53f, 4.54f, -5.85f)
                lineToRelative(1.51f, 1.61f)
                reflectiveCurveToRelative(-2.05f, 1.05f, -4.86f, 5.99f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                stroke = SolidColor(Color(0xFF010101)),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.127f, 9.84f)
                curveToRelative(-2.421f, 0.67f, -3.59f, 2.168f, -4.14f, 3.492f)
                reflectiveCurveToRelative(-0.49f, 2.484f, -0.49f, 2.484f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.02f, 0.065f)
                lineToRelative(1.2f, 1.75f)
                lineToRelative(0.03f, -0.055f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.064f, 0.11f)
                horizontalLineToRelative(0.01f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.064f, -0.122f)
                lineToRelative(0.033f, 0.057f)
                curveToRelative(1.4f, -2.462f, 2.61f, -3.949f, 3.465f, -4.82f)
                reflectiveCurveToRelative(1.344f, -1.12f, 1.344f, -1.12f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.035f, -0.197f)
                lineToRelative(-1.51f, -1.609f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, -0.035f)
                moveToRelative(0.006f, 0.273f)
                lineToRelative(1.357f, 1.45f)
                curveToRelative(-0.084f, 0.046f, -0.49f, 0.251f, -1.285f, 1.062f)
                curveToRelative(-0.843f, 0.86f, -2.036f, 2.378f, -3.387f, 4.71f)
                lineToRelative(-1.07f, -1.556f)
                curveToRelative(0f, -0.021f, -0.054f, -1.093f, 0.469f, -2.351f)
                curveToRelative(0.522f, -1.257f, 1.621f, -2.655f, 3.916f, -3.315f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23.898f, 9.371f)
                curveToRelative(-0.02f, 0f, -1.852f, -0.008f, -4.154f, 1.059f)
                curveToRelative(-2.315f, 1.072f, -5.114f, 3.24f, -7.043f, 7.558f)
                curveToRelative(0f, 0.003f, -0.788f, 1.723f, -1.642f, 3.442f)
                curveToRelative(-0.428f, 0.86f, -0.873f, 1.719f, -1.24f, 2.357f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.477f, 0.758f)
                curveToRelative(-0.064f, 0.087f, -0.12f, 0.154f, -0.155f, 0.187f)
                curveToRelative(-0.034f, 0.034f, -0.056f, 0.008f, 0.043f, 0.008f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.068f, 0.49f)
                reflectiveCurveToRelative(0.067f, -0.004f, 0.07f, -0.003f)
                lineToRelative(-0.002f, 0.013f)
                curveToRelative(0.017f, 0f, 0.016f, -0.012f, 0.03f, -0.013f)
                curveToRelative(0.072f, 0.017f, 0.502f, 0.134f, 1.224f, -0.061f)
                curveToRelative(0.69f, -0.186f, 1.598f, -0.736f, 2.547f, -1.766f)
                curveToRelative(0.032f, 0.226f, 0.038f, 0.351f, 0.123f, 0.729f)
                curveToRelative(0.07f, 0.311f, 0.16f, 0.633f, 0.276f, 0.912f)
                curveToRelative(0.115f, 0.279f, 0.242f, 0.528f, 0.494f, 0.66f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.326f, -0.086f)
                reflectiveCurveToRelative(1.08f, -1.664f, 2.4f, -3.441f)
                curveToRelative(0.66f, -0.889f, 1.38f, -1.805f, 2.051f, -2.551f)
                curveToRelative(0.595f, -0.66f, 1.117f, -1.096f, 1.528f, -1.344f)
                curveToRelative(0.09f, 0.26f, 0.416f, 1.253f, 1.2f, 2.614f)
                curveToRelative(0.881f, 1.526f, 2.172f, 3.204f, 3.91f, 3.707f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.32f, -0.264f)
                reflectiveCurveToRelative(-0.164f, -1.642f, -0.208f, -3.225f)
                curveToRelative(-0.022f, -0.79f, -0.012f, -1.57f, 0.059f, -2.101f)
                curveToRelative(0.035f, -0.266f, 0.092f, -0.47f, 0.138f, -0.557f)
                lineToRelative(0.01f, -0.017f)
                curveToRelative(3.043f, 1.309f, 4.867f, 3.197f, 5.998f, 4.714f)
                curveToRelative(0.568f, 0.762f, 0.96f, 1.428f, 1.25f, 1.89f)
                curveToRelative(0.146f, 0.23f, 0.26f, 0.405f, 0.385f, 0.536f)
                arcToRelative(0.55f, 0.55f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.232f, 0.162f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.35f, -0.068f)
                curveToRelative(0.198f, -0.171f, 0.298f, -0.417f, 0.377f, -0.686f)
                curveToRelative(0.08f, -0.268f, 0.13f, -0.565f, 0.164f, -0.847f)
                curveToRelative(0.04f, -0.334f, 0.033f, -0.432f, 0.04f, -0.631f)
                curveToRelative(1.518f, 1.196f, 2.61f, 1.872f, 3.183f, 2.021f)
                curveToRelative(0.32f, 0.084f, 0.55f, 0.07f, 0.713f, 0.004f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.183f, -0.123f)
                curveToRelative(0.042f, -0.043f, 0.074f, -0.103f, 0.074f, -0.103f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.011f, -0.23f)
                reflectiveCurveToRelative(-1.631f, -2.81f, -2.494f, -3.993f)
                curveToRelative(-0.8f, -1.094f, -1.18f, -2.592f, -1.18f, -2.592f)
                lineToRelative(-0.004f, -0.013f)
                curveToRelative(-1.454f, -4.708f, -4.209f, -7.006f, -6.596f, -8.102f)
                reflectiveCurveToRelative(-4.424f, -1.004f, -4.424f, -1.004f)
                moveToRelative(0.01f, 0.498f)
                horizontalLineToRelative(0.014f)
                reflectiveCurveToRelative(1.913f, -0.088f, 4.193f, 0.96f)
                curveToRelative(2.28f, 1.046f, 4.91f, 3.212f, 6.326f, 7.794f)
                lineToRelative(-0.003f, -0.014f)
                reflectiveCurveToRelative(0.378f, 1.562f, 1.26f, 2.768f)
                curveToRelative(0.76f, 1.042f, 2.1f, 3.343f, 2.298f, 3.682f)
                curveToRelative(-0.064f, 0.004f, -0.085f, 0.024f, -0.232f, -0.014f)
                curveToRelative(-0.503f, -0.13f, -1.529f, -0.644f, -3.375f, -2.168f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.408f, 0.193f)
                reflectiveCurveToRelative(-0.002f, 0.477f, -0.065f, 1.006f)
                curveToRelative(-0.031f, 0.265f, -0.078f, 0.54f, -0.144f, 0.766f)
                curveToRelative(-0.049f, 0.163f, -0.108f, 0.244f, -0.163f, 0.326f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.279f, -0.397f)
                curveToRelative(-0.283f, -0.448f, -0.686f, -1.135f, -1.271f, -1.92f)
                curveToRelative(-1.17f, -1.57f, -3.073f, -3.54f, -6.23f, -4.89f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.098f, -0.022f)
                horizontalLineToRelative(-0.01f)
                lineToRelative(0.097f, 0.022f)
                arcToRelative(0.46f, 0.46f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.377f, 0.008f)
                arcToRelative(0.56f, 0.56f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.234f, 0.248f)
                curveToRelative(-0.103f, 0.193f, -0.154f, 0.433f, -0.193f, 0.726f)
                curveToRelative(-0.079f, 0.587f, -0.085f, 1.378f, -0.063f, 2.182f)
                curveToRelative(0.038f, 1.364f, 0.143f, 2.374f, 0.18f, 2.766f)
                curveToRelative(-1.342f, -0.567f, -2.488f, -1.895f, -3.268f, -3.248f)
                curveToRelative(-0.852f, -1.479f, -1.306f, -2.842f, -1.306f, -2.842f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.344f, -0.147f)
                curveToRelative(-0.56f, 0.265f, -1.194f, 0.87f, -1.883f, 1.635f)
                reflectiveCurveToRelative(-1.416f, 1.69f, -2.082f, 2.586f)
                curveToRelative(-1.223f, 1.646f, -2.063f, 2.944f, -2.22f, 3.186f)
                curveToRelative(-0.05f, -0.071f, -0.087f, -0.09f, -0.137f, -0.211f)
                arcToRelative(5.3f, 5.3f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.248f, -0.83f)
                curveToRelative(-0.133f, -0.59f, -0.194f, -1.147f, -0.194f, -1.147f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.443f, -0.13f)
                curveToRelative(-1.033f, 1.27f, -1.976f, 1.757f, -2.65f, 1.939f)
                curveToRelative(-0.338f, 0.09f, -0.35f, 0.04f, -0.53f, 0.035f)
                curveToRelative(0.13f, -0.19f, 0.267f, -0.403f, 0.426f, -0.68f)
                curveToRelative(0.377f, -0.655f, 0.824f, -1.522f, 1.254f, -2.387f)
                curveToRelative(0.86f, -1.73f, 1.652f, -3.457f, 1.652f, -3.457f)
                verticalLineToRelative(-0.002f)
                curveToRelative(1.881f, -4.211f, 4.573f, -6.278f, 6.795f, -7.308f)
                reflectiveCurveToRelative(3.955f, -1.014f, 3.955f, -1.014f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.23f, 20.895f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, 0.115f)
                reflectiveCurveToRelative(-0.086f, 1.101f, 0.163f, 2.209f)
                curveToRelative(0.124f, 0.553f, 0.332f, 1.112f, 0.689f, 1.539f)
                reflectiveCurveToRelative(0.873f, 0.715f, 1.564f, 0.707f)
                arcToRelative(1.99f, 1.99f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.563f, -0.733f)
                curveToRelative(0.357f, -0.429f, 0.565f, -0.985f, 0.69f, -1.535f)
                curveToRelative(0.249f, -1.1f, 0.162f, -2.187f, 0.162f, -2.187f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, -0.115f)
                close()
                moveTo(17.363f, 21.145f)
                horizontalLineToRelative(4.315f)
                curveToRelative(0.007f, 0.106f, 0.077f, 1f, -0.149f, 1.996f)
                curveToRelative(-0.119f, 0.526f, -0.32f, 1.048f, -0.638f, 1.431f)
                curveToRelative(-0.32f, 0.384f, -0.748f, 0.636f, -1.371f, 0.643f)
                curveToRelative(-0.624f, 0.007f, -1.053f, -0.237f, -1.372f, -0.617f)
                curveToRelative(-0.318f, -0.38f, -0.519f, -0.904f, -0.638f, -1.434f)
                curveToRelative(-0.226f, -1.003f, -0.154f, -1.913f, -0.147f, -2.02f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(25.85f, 20.895f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, 0.115f)
                reflectiveCurveToRelative(-0.087f, 1.101f, 0.162f, 2.209f)
                curveToRelative(0.124f, 0.553f, 0.332f, 1.112f, 0.69f, 1.539f)
                reflectiveCurveToRelative(0.872f, 0.715f, 1.564f, 0.707f)
                horizontalLineToRelative(0.002f)
                curveToRelative(0.57f, -0.008f, 1.02f, -0.211f, 1.359f, -0.526f)
                curveToRelative(0.338f, -0.314f, 0.566f, -0.734f, 0.723f, -1.18f)
                curveToRelative(0.314f, -0.89f, 0.345f, -1.888f, 0.34f, -2.4f)
                lineToRelative(-0.11f, 0.11f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.082f, -0.2f)
                lineToRelative(-0.35f, -0.34f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.087f, -0.034f)
                close()
                moveTo(25.982f, 21.145f)
                horizontalLineToRelative(4.067f)
                lineToRelative(0.26f, 0.251f)
                curveToRelative(0.003f, 0.505f, -0.027f, 1.453f, -0.319f, 2.28f)
                curveToRelative(-0.148f, 0.42f, -0.36f, 0.803f, -0.658f, 1.08f)
                reflectiveCurveToRelative(-0.679f, 0.452f, -1.193f, 0.459f)
                curveToRelative(-0.623f, 0.007f, -1.053f, -0.237f, -1.371f, -0.617f)
                reflectiveCurveToRelative(-0.518f, -0.904f, -0.637f, -1.434f)
                curveToRelative(-0.226f, -1.003f, -0.156f, -1.913f, -0.149f, -2.02f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23.213f, 27.72f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.002f, 0.178f)
                reflectiveCurveToRelative(0.17f, 0.174f, 0.432f, 0.262f)
                curveToRelative(0.261f, 0.088f, 0.637f, 0.078f, 0.976f, -0.262f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -0.177f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.178f, 0f)
                curveToRelative(-0.28f, 0.28f, -0.52f, 0.27f, -0.718f, 0.203f)
                reflectiveCurveToRelative(-0.334f, -0.201f, -0.334f, -0.201f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.176f, -0.002f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(15.818f, 22.47f)
                lineToRelative(-2.09f, 2.83f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.021f, 0.262f)
                curveToRelative(0.295f, 0.586f, 0.808f, 1.408f, 1.469f, 2.153f)
                curveToRelative(0.66f, 0.744f, 1.468f, 1.416f, 2.38f, 1.656f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.298f, -0.33f)
                reflectiveCurveToRelative(-0.472f, -1.251f, -0.907f, -2.686f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.558f, -2.146f)
                curveToRelative(-0.134f, -0.668f, -0.183f, -1.253f, -0.123f, -1.549f)
                horizontalLineToRelative(-0.01f)
                lineToRelative(0.013f, -0.04f)
                verticalLineToRelative(-0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.48f, -0.047f)
                close()
                moveTo(15.778f, 22.594f)
                curveToRelative(-0.001f, 0.004f, 0.002f, 0.011f, 0.001f, 0.015f)
                horizontalLineToRelative(-0.006f)
                close()
                moveTo(15.828f, 23.301f)
                curveToRelative(0.018f, 0.327f, -0.01f, 0.606f, 0.07f, 1.006f)
                curveToRelative(0.139f, 0.694f, 0.35f, 1.468f, 0.57f, 2.195f)
                curveToRelative(0.326f, 1.074f, 0.52f, 1.574f, 0.678f, 2.008f)
                curveToRelative(-0.559f, -0.287f, -1.13f, -0.601f, -1.597f, -1.127f)
                arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.315f, -1.926f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(32.305f, 23.438f)
                lineToRelative(0.04f, 0.132f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.493f, -0.023f)
                curveToRelative(-0.084f, 0.866f, -0.6f, 2.257f, -1.098f, 3.404f)
                curveToRelative(-0.499f, 1.148f, -0.977f, 2.063f, -0.977f, 2.063f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.319f, 0.347f)
                curveToRelative(2.664f, -1.097f, 3.57f, -3.78f, 3.57f, -3.78f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.031f, -0.224f)
                close()
                moveTo(32.178f, 24.131f)
                lineTo(33.141f, 25.52f)
                curveToRelative(-0.054f, 0.149f, -0.767f, 1.856f, -2.551f, 2.949f)
                curveToRelative(0.186f, -0.374f, 0.302f, -0.586f, 0.62f, -1.319f)
                curveToRelative(0.43f, -0.989f, 0.783f, -2.08f, 0.968f, -3.02f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.006f, 23.21f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.236f, 0.265f)
                reflectiveCurveToRelative(0.091f, 1.689f, 1.146f, 3.377f)
                reflectiveCurveToRelative(3.106f, 3.378f, 6.895f, 3.378f)
                curveToRelative(3.783f, 0f, 5.935f, -1.57f, 7.105f, -3.146f)
                reflectiveCurveToRelative(1.38f, -3.16f, 1.38f, -3.16f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.214f, -0.281f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.28f, 0.214f)
                reflectiveCurveToRelative(-0.194f, 1.458f, -1.286f, 2.928f)
                reflectiveCurveToRelative(-3.059f, 2.945f, -6.705f, 2.945f)
                curveToRelative(-3.642f, 0f, -5.489f, -1.57f, -6.471f, -3.142f)
                reflectiveCurveToRelative(-1.07f, -3.143f, -1.07f, -3.143f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.264f, -0.234f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                stroke = SolidColor(Color(0xFF010101)),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(34.57f, 17.64f)
                lineToRelative(1.28f, -1.69f)
                reflectiveCurveToRelative(0.44f, -4.52f, -4.25f, -6.06f)
                lineToRelative(-1.59f, 1.53f)
                reflectiveCurveToRelative(2f, 1.15f, 4.57f, 6.21f)
                verticalLineToRelative(0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                stroke = SolidColor(Color(0xFF010101)),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(31.639f, 9.771f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, 0.03f)
                lineToRelative(-1.59f, 1.53f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.023f, 0.198f)
                reflectiveCurveToRelative(0.478f, 0.271f, 1.291f, 1.182f)
                reflectiveCurveToRelative(1.95f, 2.455f, 3.23f, 4.977f)
                lineToRelative(0.018f, -0.11f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.172f, 0.15f)
                lineToRelative(0.01f, -0.01f)
                verticalLineToRelative(-0.005f)
                lineToRelative(0.002f, 0.002f)
                lineToRelative(1.28f, -1.69f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.025f, -0.062f)
                reflectiveCurveToRelative(0.113f, -1.158f, -0.373f, -2.508f)
                reflectiveCurveToRelative(-1.583f, -2.902f, -3.963f, -3.684f)
                moveToRelative(-0.018f, 0.274f)
                curveToRelative(2.253f, 0.766f, 3.283f, 2.214f, 3.744f, 3.494f)
                curveToRelative(0.462f, 1.282f, 0.36f, 2.35f, 0.358f, 2.373f)
                lineToRelative(-1.137f, 1.5f)
                curveToRelative(-1.239f, -2.396f, -2.36f, -3.968f, -3.162f, -4.867f)
                curveToRelative(-0.758f, -0.848f, -1.153f, -1.075f, -1.235f, -1.125f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(13.682f, 9.473f)
                arcToRelative(2.65f, 2.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.237f, 0.185f)
                curveToRelative(-0.676f, 0.273f, -1.186f, 0.781f, -1.69f, 1.612f)
                curveToRelative(-0.503f, 0.83f, -1.018f, 1.994f, -1.743f, 3.66f)
                curveToRelative(-1.442f, 3.312f, -4.083f, 9.092f, -4.104f, 9.138f)
                curveToRelative(-0.73f, 0.707f, -3.086f, 2.874f, -3.988f, 5.352f)
                curveToRelative(-0.455f, 1.25f, -0.528f, 2.608f, 0.22f, 3.84f)
                curveToRelative(0.749f, 1.232f, 2.281f, 2.309f, 4.948f, 3.11f)
                curveToRelative(0f, 0f, -0.014f, -0.007f, 0.002f, 0.005f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.068f, 0.094f)
                curveToRelative(0.051f, 0.116f, 0.1f, 0.394f, -0.103f, 0.969f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.065f, 0.498f)
                arcToRelative(0.62f, 0.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.223f, 0.386f)
                curveToRelative(0.229f, 0.18f, 0.516f, 0.187f, 0.803f, 0.147f)
                curveToRelative(0.286f, -0.04f, 0.584f, -0.143f, 0.843f, -0.283f)
                curveToRelative(0.26f, -0.14f, 0.486f, -0.309f, 0.612f, -0.55f)
                curveToRelative(0.067f, -0.129f, 0.157f, -0.225f, 0.265f, -0.364f)
                curveToRelative(0.11f, -0.14f, 0.23f, -0.324f, 0.307f, -0.588f)
                curveToRelative(0.155f, -0.53f, 0.165f, -1.36f, -0.117f, -3.065f)
                curveToRelative(-0.264f, -1.59f, 0.424f, -3.588f, 1.199f, -5.172f)
                arcToRelative(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.617f, -2.75f)
                lineToRelative(0.012f, -0.017f)
                lineToRelative(1.44f, -2.37f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.366f, -0.328f)
                reflectiveCurveToRelative(-0.72f, 0.555f, -1.533f, 1.02f)
                curveToRelative(-0.406f, 0.233f, -0.837f, 0.44f, -1.191f, 0.545f)
                curveToRelative(-0.238f, 0.07f, -0.337f, 0.025f, -0.463f, 0.002f)
                lineToRelative(2.847f, -5.158f)
                lineToRelative(0.01f, -0.016f)
                reflectiveCurveToRelative(0.342f, -0.748f, 0.72f, -1.539f)
                curveToRelative(0.19f, -0.396f, 0.389f, -0.802f, 0.557f, -1.13f)
                curveToRelative(0.17f, -0.33f, 0.33f, -0.6f, 0.36f, -0.638f)
                lineToRelative(0.002f, -0.003f)
                lineToRelative(2.47f, -3.141f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.045f, -0.213f)
                curveToRelative(-0.33f, -1.381f, -0.972f, -2.308f, -1.746f, -2.809f)
                arcToRelative(2.7f, 2.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.224f, -0.43f)
                moveToRelative(-0.047f, 0.494f)
                curveToRelative(0.34f, 0.03f, 0.678f, 0.145f, 1f, 0.353f)
                curveToRelative(0.622f, 0.403f, 1.174f, 1.19f, 1.49f, 2.407f)
                lineToRelative(-2.38f, 3.029f)
                curveToRelative(-0.12f, 0.148f, -0.243f, 0.386f, -0.415f, 0.72f)
                curveToRelative(-0.172f, 0.337f, -0.372f, 0.747f, -0.562f, 1.145f)
                curveToRelative(-0.377f, 0.787f, -0.71f, 1.51f, -0.717f, 1.527f)
                lineTo(9.03f, 24.62f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.057f, 0.31f)
                curveToRelative(0.31f, 0.265f, 0.735f, 0.222f, 1.156f, 0.098f)
                curveToRelative(0.42f, -0.123f, 0.874f, -0.348f, 1.299f, -0.591f)
                curveToRelative(0.342f, -0.196f, 0.373f, -0.246f, 0.639f, -0.426f)
                lineToRelative(-0.856f, 1.41f)
                lineToRelative(0.012f, -0.016f)
                reflectiveCurveToRelative(-0.867f, 1.199f, -1.662f, 2.825f)
                curveToRelative(-0.795f, 1.625f, -1.539f, 3.683f, -1.242f, 5.472f)
                curveToRelative(0.277f, 1.675f, 0.244f, 2.449f, 0.129f, 2.842f)
                curveToRelative(-0.058f, 0.197f, -0.13f, 0.305f, -0.221f, 0.422f)
                reflectiveCurveToRelative(-0.21f, 0.244f, -0.313f, 0.44f)
                curveToRelative(-0.044f, 0.084f, -0.201f, 0.23f, -0.408f, 0.341f)
                reflectiveCurveToRelative(-0.46f, 0.198f, -0.676f, 0.229f)
                curveToRelative(-0.215f, 0.03f, -0.378f, -0.01f, -0.424f, -0.045f)
                curveToRelative(-0.022f, -0.018f, -0.03f, -0.026f, -0.037f, -0.07f)
                arcToRelative(0.56f, 0.56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.041f, -0.256f)
                curveToRelative(0.228f, -0.646f, 0.212f, -1.064f, 0.09f, -1.338f)
                arcToRelative(0.74f, 0.74f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.222f, -0.29f)
                arcToRelative(0.6f, 0.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.16f, -0.085f)
                curveTo(3.633f, 35.11f, 2.225f, 34.082f, 1.567f, 33f)
                curveToRelative(-0.657f, -1.082f, -0.6f, -2.248f, -0.177f, -3.408f)
                curveToRelative(0.844f, -2.321f, 3.179f, -4.499f, 3.904f, -5.203f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.053f, -0.075f)
                reflectiveCurveToRelative(2.669f, -5.85f, 4.12f, -9.185f)
                curveToRelative(0.725f, -1.664f, 1.24f, -2.818f, 1.714f, -3.6f)
                reflectiveCurveToRelative(0.888f, -1.18f, 1.45f, -1.408f)
                horizontalLineToRelative(0.003f)
                curveToRelative(0.322f, -0.13f, 0.66f, -0.185f, 1f, -0.154f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(32.97f, 9.88f)
                curveToRelative(-0.783f, 0.283f, -1.59f, 0.936f, -2.378f, 2.132f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.027f, 0.31f)
                reflectiveCurveToRelative(0.855f, 0.898f, 1.768f, 2.074f)
                curveToRelative(0.912f, 1.177f, 1.872f, 2.647f, 2.088f, 3.684f)
                lineToRelative(0.033f, -0.111f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.035f, 0.15f)
                reflectiveCurveToRelative(0.622f, 1.872f, 0.877f, 2.486f)
                curveToRelative(0.075f, 0.182f, 0.225f, 0.444f, 0.421f, 0.77f)
                curveToRelative(0.197f, 0.326f, 0.433f, 0.704f, 0.663f, 1.063f)
                curveToRelative(0.459f, 0.717f, 0.888f, 1.36f, 0.888f, 1.36f)
                lineToRelative(0.983f, 1.641f)
                curveToRelative(-0.16f, 0.007f, -0.29f, 0.051f, -0.514f, -0.035f)
                curveToRelative(-0.36f, -0.138f, -0.77f, -0.387f, -1.146f, -0.658f)
                curveToRelative(-0.754f, -0.542f, -1.38f, -1.164f, -1.38f, -1.164f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.415f, 0.25f)
                reflectiveCurveToRelative(0.176f, 0.59f, 0.474f, 1.3f)
                curveToRelative(0.298f, 0.713f, 0.71f, 1.55f, 1.238f, 2.085f)
                curveToRelative(0.43f, 0.434f, 0.964f, 1.377f, 1.368f, 2.197f)
                curveToRelative(0.403f, 0.82f, 0.69f, 1.52f, 0.69f, 1.52f)
                horizontalLineToRelative(-0.003f)
                curveToRelative(0.638f, 1.59f, 0.609f, 2.367f, 0.443f, 3.078f)
                reflectiveCurveToRelative(-0.507f, 1.397f, -0.46f, 2.617f)
                curveToRelative(0.048f, 1.247f, 0.679f, 1.988f, 1.375f, 2.305f)
                curveToRelative(0.347f, 0.158f, 0.708f, 0.216f, 1.027f, 0.2f)
                reflectiveCurveToRelative(0.6f, -0.095f, 0.799f, -0.282f)
                arcToRelative(0.7f, 0.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.234f, -0.575f)
                curveToRelative(-0.022f, -0.193f, -0.117f, -0.343f, -0.21f, -0.474f)
                curveToRelative(-0.095f, -0.132f, -0.196f, -0.248f, -0.268f, -0.35f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.088f, -0.18f)
                arcToRelative(0.7f, 0.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.138f, -0.07f)
                curveToRelative(0.193f, -0.078f, 0.519f, -0.178f, 0.948f, -0.332f)
                curveToRelative(0.857f, -0.308f, 2.135f, -0.847f, 3.76f, -1.986f)
                curveToRelative(0.87f, -0.612f, 1.173f, -1.526f, 1.093f, -2.489f)
                reflectiveCurveToRelative(-0.511f, -1.992f, -1.08f, -2.982f)
                curveToRelative(-1.137f, -1.98f, -2.824f, -3.792f, -3.553f, -4.432f)
                curveToRelative(-0.125f, -0.11f, -0.324f, -0.388f, -0.535f, -0.775f)
                arcToRelative(18f, 18f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.69f, -1.457f)
                curveToRelative(-0.49f, -1.146f, -1.033f, -2.591f, -1.573f, -4.064f)
                curveToRelative(-0.54f, -1.474f, -1.08f, -2.977f, -1.565f, -4.245f)
                curveToRelative(-0.485f, -1.267f, -0.91f, -2.29f, -1.26f, -2.853f)
                lineToRelative(-0.01f, -0.016f)
                reflectiveCurveToRelative(-0.789f, -1.105f, -2.025f, -1.615f)
                curveToRelative(-0.618f, -0.255f, -1.362f, -0.358f, -2.146f, -0.076f)
                moveToRelative(0.169f, 0.472f)
                curveToRelative(0.655f, -0.236f, 1.254f, -0.154f, 1.787f, 0.066f)
                curveToRelative(1.064f, 0.439f, 1.805f, 1.44f, 1.808f, 1.445f)
                curveToRelative(0.3f, 0.485f, 0.731f, 1.504f, 1.211f, 2.758f)
                curveToRelative(0.483f, 1.26f, 1.02f, 2.76f, 1.56f, 4.236f)
                curveToRelative(0.542f, 1.476f, 1.086f, 2.925f, 1.585f, 4.088f)
                curveToRelative(0.25f, 0.582f, 0.488f, 1.093f, 0.71f, 1.502f)
                curveToRelative(0.224f, 0.41f, 0.424f, 0.716f, 0.645f, 0.91f)
                curveToRelative(0.662f, 0.58f, 2.354f, 2.4f, 3.45f, 4.307f)
                curveToRelative(0.547f, 0.954f, 0.945f, 1.932f, 1.015f, 2.774f)
                curveToRelative(0.07f, 0.84f, -0.155f, 1.523f, -0.885f, 2.037f)
                curveToRelative(-1.584f, 1.11f, -2.808f, 1.626f, -3.64f, 1.925f)
                curveToRelative(-0.416f, 0.15f, -0.732f, 0.243f, -0.967f, 0.338f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.307f, 0.172f)
                arcToRelative(0.46f, 0.46f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.16f, 0.356f)
                curveToRelative(0.013f, 0.195f, 0.105f, 0.345f, 0.197f, 0.476f)
                reflectiveCurveToRelative(0.195f, 0.247f, 0.27f, 0.352f)
                arcToRelative(0.6f, 0.6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.12f, 0.24f)
                curveToRelative(0.004f, 0.047f, 0.01f, 0.07f, -0.079f, 0.154f)
                curveToRelative(-0.06f, 0.057f, -0.245f, 0.137f, -0.482f, 0.149f)
                arcToRelative(1.76f, 1.76f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.795f, -0.158f)
                curveToRelative(-0.542f, -0.247f, -1.04f, -0.775f, -1.082f, -1.868f)
                curveToRelative(-0.043f, -1.12f, 0.265f, -1.702f, 0.447f, -2.486f)
                reflectiveCurveToRelative(0.197f, -1.729f, -0.465f, -3.379f)
                curveToRelative(0f, 0f, -0.291f, -0.715f, -0.703f, -1.553f)
                curveToRelative(-0.412f, -0.837f, -0.93f, -1.794f, -1.461f, -2.33f)
                curveToRelative(-0.431f, -0.435f, -0.846f, -1.238f, -1.133f, -1.924f)
                curveToRelative(-0.088f, -0.21f, -0.076f, -0.21f, -0.14f, -0.384f)
                curveToRelative(0.26f, 0.23f, 0.318f, 0.316f, 0.709f, 0.597f)
                curveToRelative(0.397f, 0.286f, 0.83f, 0.555f, 1.257f, 0.72f)
                curveToRelative(0.428f, 0.163f, 0.874f, 0.233f, 1.25f, 0.026f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.094f, -0.347f)
                lineToRelative(-1.21f, -2.02f)
                lineToRelative(-0.007f, -0.01f)
                reflectiveCurveToRelative(-0.427f, -0.64f, -0.883f, -1.353f)
                arcToRelative(47f, 47f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.656f, -1.05f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.388f, -0.704f)
                curveToRelative(-0.223f, -0.538f, -0.834f, -2.365f, -0.852f, -2.418f)
                lineToRelative(0.006f, -0.018f)
                curveToRelative(-0.255f, -1.223f, -1.254f, -2.69f, -2.184f, -3.888f)
                curveToRelative(-0.855f, -1.102f, -1.532f, -1.812f, -1.656f, -1.944f)
                curveToRelative(0.705f, -1.016f, 1.394f, -1.571f, 2.014f, -1.794f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.93f, 22.682f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.176f, 0.113f)
                curveToRelative(-0.193f, 0.177f, -0.354f, 0.456f, -0.527f, 0.775f)
                curveToRelative(-0.348f, 0.64f, -0.676f, 1.412f, -0.676f, 1.412f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.02f, 0.07f)
                reflectiveCurveToRelative(-0.095f, 0.829f, 0.043f, 1.635f)
                curveToRelative(0.07f, 0.404f, 0.192f, 0.812f, 0.455f, 1.118f)
                curveToRelative(0.264f, 0.305f, 0.696f, 0.472f, 1.205f, 0.36f)
                horizontalLineToRelative(0.002f)
                lineToRelative(1.76f, -0.401f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.166f, -0.36f)
                reflectiveCurveToRelative(-0.414f, -0.793f, -0.84f, -1.728f)
                curveToRelative(-0.425f, -0.935f, -0.855f, -2.047f, -0.892f, -2.526f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.045f, -0.232f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.16f, -0.213f)
                arcToRelative(0.36f, 0.36f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.295f, -0.023f)
                moveToRelative(0.05f, 0.648f)
                curveToRelative(0.089f, 0.682f, 0.476f, 1.653f, 0.885f, 2.553f)
                curveToRelative(0.36f, 0.792f, 0.604f, 1.255f, 0.711f, 1.463f)
                lineToRelative(-1.451f, 0.33f)
                curveToRelative(-0.37f, 0.082f, -0.553f, -0.007f, -0.717f, -0.197f)
                curveToRelative(-0.164f, -0.191f, -0.28f, -0.52f, -0.342f, -0.875f)
                curveToRelative(-0.12f, -0.698f, -0.04f, -1.441f, -0.037f, -1.47f)
                curveToRelative(0.02f, -0.044f, 0.317f, -0.738f, 0.637f, -1.325f)
                arcToRelative(3.4f, 3.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.314f, -0.479f)
            }
            path(
                fill = SolidColor(Color(0xFF010101)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(34.41f, 22.762f)
                arcToRelative(0.36f, 0.36f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.295f, 0.023f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.16f, 0.213f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.045f, 0.232f)
                curveToRelative(-0.037f, 0.48f, -0.465f, 1.59f, -0.89f, 2.526f)
                curveToRelative(-0.426f, 0.935f, -0.842f, 1.728f, -0.842f, 1.728f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.166f, 0.36f)
                lineToRelative(1.761f, 0.4f)
                curveToRelative(0.51f, 0.113f, 0.942f, -0.054f, 1.206f, -0.36f)
                curveToRelative(0.263f, -0.305f, 0.386f, -0.713f, 0.455f, -1.116f)
                curveToRelative(0.138f, -0.807f, 0.043f, -1.635f, 0.043f, -1.635f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.02f, -0.07f)
                reflectiveCurveToRelative(-0.328f, -0.774f, -0.676f, -1.413f)
                curveToRelative(-0.174f, -0.319f, -0.334f, -0.598f, -0.527f, -0.775f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.176f, -0.113f)
                moveToRelative(-0.049f, 0.65f)
                curveToRelative(0.093f, 0.122f, 0.191f, 0.254f, 0.313f, 0.477f)
                curveToRelative(0.32f, 0.589f, 0.62f, 1.289f, 0.638f, 1.33f)
                curveToRelative(0.004f, 0.031f, 0.08f, 0.769f, -0.039f, 1.465f)
                curveToRelative(-0.06f, 0.355f, -0.177f, 0.682f, -0.341f, 0.873f)
                reflectiveCurveToRelative(-0.347f, 0.281f, -0.717f, 0.199f)
                lineToRelative(-1.451f, -0.33f)
                curveToRelative(0.107f, -0.208f, 0.35f, -0.671f, 0.71f, -1.463f)
                curveToRelative(0.41f, -0.9f, 0.799f, -1.869f, 0.887f, -2.55f)
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.465f, 11.041f)
                curveToRelative(-1.448f, 0.454f, -2.496f, 1.69f, -3.194f, 2.795f)
                arcToRelative(11.6f, 11.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.046f, 2.102f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.152f, 0.318f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.318f, -0.152f)
                reflectiveCurveToRelative(0.335f, -0.95f, 1f, -2.002f)
                curveToRelative(0.666f, -1.054f, 1.658f, -2.188f, 2.92f, -2.584f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.164f, -0.313f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.314f, -0.164f)
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(31.236f, 10.932f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.314f, 0.162f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.162f, 0.314f)
                curveToRelative(1.197f, 0.38f, 2.14f, 1.472f, 2.771f, 2.487f)
                curveToRelative(0.632f, 1.014f, 0.95f, 1.927f, 0.95f, 1.927f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.316f, 0.155f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.154f, -0.319f)
                reflectiveCurveToRelative(-0.332f, -0.962f, -0.996f, -2.027f)
                reflectiveCurveToRelative(-1.66f, -2.26f, -3.043f, -2.7f)
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.71f, 29.086f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.26f, 0.379f)
                lineToRelative(1.85f, 2.861f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.4f, 0.028f)
                lineToRelative(1.79f, -2.092f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.138f, -0.407f)
                close()
                moveTo(20.195f, 29.699f)
                lineTo(22.835f, 30.256f)
                lineTo(21.539f, 31.773f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(28.102f, 29.143f)
                lineToRelative(-4.102f, 0.65f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.143f, 0.418f)
                lineToRelative(2.051f, 2.18f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.385f, -0.024f)
                lineToRelative(2.049f, -2.83f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.24f, -0.394f)
                moveToRelative(-0.518f, 0.587f)
                lineToRelative(-1.52f, 2.098f)
                lineToRelative(-1.517f, -1.615f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23.1f, 30.34f)
                verticalLineToRelative(1.48f)
                horizontalLineToRelative(1.14f)
                lineToRelative(0.03f, -1.48f)
                lineToRelative(-0.46f, -0.36f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(23.697f, 29.758f)
                lineToRelative(-0.71f, 0.36f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.137f, 0.222f)
                verticalLineToRelative(1.48f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.25f)
                horizontalLineToRelative(1.14f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, -0.246f)
                lineToRelative(0.03f, -1.478f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.096f, -0.203f)
                lineToRelative(-0.46f, -0.36f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.267f, -0.025f)
                moveToRelative(0.084f, 0.517f)
                lineToRelative(0.237f, 0.184f)
                lineToRelative(-0.024f, 1.111f)
                horizontalLineToRelative(-0.644f)
                verticalLineToRelative(-1.076f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(28.107f, 29.184f)
                reflectiveCurveToRelative(-0.855f, 0.141f, -1.765f, 0.285f)
                curveToRelative(-0.455f, 0.072f, -0.923f, 0.144f, -1.305f, 0.199f)
                reflectiveCurveToRelative(-0.695f, 0.09f, -0.771f, 0.092f)
                curveToRelative(-0.09f, 0.002f, -0.423f, -0.01f, -0.85f, -0.031f)
                curveToRelative(-0.427f, -0.023f, -0.96f, -0.054f, -1.486f, -0.09f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.46f, -0.118f)
                curveToRelative(-0.204f, -0.02f, -0.38f, -0.038f, -0.511f, -0.056f)
                curveToRelative(-0.132f, -0.018f, -0.24f, -0.047f, -0.225f, -0.041f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.273f, 0.185f)
                verticalLineToRelative(0.01f)
                lineToRelative(0.002f, 0.03f)
                lineToRelative(0.459f, 3.19f)
                lineToRelative(-2.475f, 4.311f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.067f, 0.268f)
                lineToRelative(4.449f, 2.82f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.22f, -0.004f)
                lineToRelative(1.118f, -0.761f)
                lineToRelative(1.117f, 0.761f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.201f, 0.016f)
                lineToRelative(5.469f, -2.691f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.086f, -0.278f)
                lineToRelative(-2.768f, -4.836f)
                lineToRelative(0.926f, -3.006f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.225f, -0.255f)
                moveToRelative(-0.253f, 0.445f)
                lineToRelative(-0.856f, 2.783f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.018f, 0.158f)
                lineToRelative(2.705f, 4.725f)
                lineToRelative(-5.174f, 2.545f)
                lineToRelative(-1.135f, -0.776f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.224f, 0f)
                lineToRelative(-1.122f, 0.766f)
                lineToRelative(-4.177f, -2.646f)
                lineToRelative(2.414f, -4.204f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.025f, -0.128f)
                lineToRelative(-0.432f, -2.993f)
                lineToRelative(0.008f, 0.002f)
                curveToRelative(0.141f, 0.02f, 0.322f, 0.039f, 0.53f, 0.059f)
                curveToRelative(0.415f, 0.04f, 0.939f, 0.08f, 1.468f, 0.117f)
                curveToRelative(0.53f, 0.036f, 1.063f, 0.068f, 1.493f, 0.09f)
                reflectiveCurveToRelative(0.745f, 0.036f, 0.88f, 0.033f)
                curveToRelative(0.15f, -0.003f, 0.433f, -0.042f, 0.819f, -0.097f)
                curveToRelative(0.385f, -0.056f, 0.854f, -0.128f, 1.31f, -0.2f)
                curveToRelative(0.748f, -0.118f, 1.217f, -0.196f, 1.45f, -0.234f)
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(19.434f, 29.8f)
                lineToRelative(-1.9f, 0.92f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.09f, 0.274f)
                lineToRelative(1.32f, 2.47f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.3f, 0.063f)
                lineToRelative(1.131f, -0.9f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.07f, -0.2f)
                lineToRelative(-0.55f, -2.49f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.281f, -0.136f)
                moveToRelative(-0.055f, 0.471f)
                lineToRelative(0.469f, 2.12f)
                lineToRelative(-0.844f, 0.672f)
                lineToRelative(-1.11f, -2.073f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(28.248f, 30.072f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.291f, 0.088f)
                lineToRelative(-0.86f, 1.95f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.01f, 0.183f)
                lineToRelative(0.862f, 1.44f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.344f, -0.003f)
                lineToRelative(1.43f, -2.47f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.065f, -0.268f)
                close()
                moveTo(28.228f, 30.535f)
                lineTo(29.301f, 31.225f)
                lineTo(28.137f, 33.235f)
                lineTo(27.504f, 32.175f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(17.62f, 31.14f)
                lineToRelative(-0.22f, -0.37f)
                lineToRelative(-0.58f, 0.4f)
                lineToRelative(1.59f, 3.12f)
                lineToRelative(0.66f, -0.61f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(17.287f, 30.605f)
                lineToRelative(-0.58f, 0.4f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.064f, 0.257f)
                lineToRelative(1.59f, 3.119f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.312f, 0.056f)
                lineToRelative(0.66f, -0.61f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.04f, -0.247f)
                lineToRelative(-1.452f, -2.539f)
                lineToRelative(-0.002f, -0.004f)
                lineToRelative(-0.219f, -0.369f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.285f, -0.063f)
                moveToRelative(0.05f, 0.452f)
                lineToRelative(0.11f, 0.185f)
                verticalLineToRelative(-0.004f)
                lineToRelative(1.371f, 2.403f)
                lineToRelative(-0.35f, 0.322f)
                lineToRelative(-1.39f, -2.729f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(29.57f, 31.16f)
                lineToRelative(0.43f, -0.26f)
                lineToRelative(0.45f, 0.26f)
                lineToRelative(-1.97f, 3.57f)
                lineToRelative(-0.34f, -1.1f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(30.1f, 30.727f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.204f, 0.002f)
                lineToRelative(-0.43f, 0.26f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.07f, 0.072f)
                lineToRelative(-1.43f, 2.468f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.017f, 0.16f)
                lineToRelative(0.34f, 1.1f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.365f, 0.037f)
                lineToRelative(1.971f, -3.57f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.074f, -0.27f)
                close()
                moveTo(30.002f, 31.133f)
                lineTo(30.18f, 31.234f)
                lineTo(28.533f, 34.221f)
                lineTo(28.357f, 33.654f)
                lineTo(29.715f, 31.307f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(16.744f, 30.936f)
                lineToRelative(-4.101f, 1.699f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.096f, 0.285f)
                lineToRelative(2.56f, 4.43f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.311f, 0.045f)
                lineToRelative(3.129f, -2.96f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.04f, -0.234f)
                lineToRelative(-1.589f, -3.17f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.254f, -0.095f)
                moveToRelative(-0.02f, 0.441f)
                lineToRelative(1.44f, 2.87f)
                lineToRelative(-2.838f, 2.685f)
                lineToRelative(-2.32f, -4.014f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(12.72f, 32.82f)
                lineToRelative(-0.66f, 0.28f)
                lineToRelative(2.75f, 4.68f)
                lineToRelative(0.47f, -0.53f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(12.643f, 32.637f)
                lineToRelative(-0.66f, 0.279f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.096f, 0.285f)
                lineToRelative(2.75f, 4.68f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.322f, 0.031f)
                lineToRelative(0.47f, -0.53f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.024f, -0.232f)
                lineToRelative(-2.56f, -4.43f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, -0.083f)
                moveToRelative(-0.008f, 0.435f)
                lineToRelative(2.4f, 4.153f)
                lineToRelative(-0.191f, 0.216f)
                lineToRelative(-2.496f, -4.246f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(30.543f, 30.982f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.268f, 0.08f)
                lineToRelative(-1.97f, 3.57f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.043f, 0.247f)
                lineToRelative(3.92f, 3.5f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.302f, -0.045f)
                lineToRelative(2.782f, -4.6f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.079f, -0.28f)
                close()
                moveTo(30.529f, 31.428f)
                lineTo(34.9f, 33.709f)
                lineTo(32.354f, 37.919f)
                lineTo(28.732f, 34.688f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(34f, 33.01f)
                lineToRelative(-2.28f, 4.61f)
                lineToRelative(0.68f, 0.61f)
                lineToRelative(2.78f, -4.6f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(34.094f, 32.832f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.274f, 0.09f)
                lineToRelative(-2.279f, 4.61f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.045f, 0.238f)
                lineToRelative(0.68f, 0.609f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.304f, -0.045f)
                lineToRelative(2.782f, -4.6f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.079f, -0.28f)
                close()
                moveTo(34.088f, 33.283f)
                lineTo(34.9f, 33.709f)
                lineTo(32.353f, 37.919f)
                lineTo(31.967f, 37.572f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.17f, 33.637f)
                curveToRelative(-0.392f, 0.225f, -0.698f, 0.522f, -0.88f, 0.836f)
                curveToRelative(-0.184f, 0.313f, -0.252f, 0.67f, -0.083f, 0.959f)
                lineToRelative(0.691f, 1.17f)
                curveToRelative(0.17f, 0.287f, 0.512f, 0.4f, 0.873f, 0.392f)
                curveToRelative(0.363f, -0.008f, 0.77f, -0.13f, 1.16f, -0.361f)
                lineToRelative(-0.222f, -0.03f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.303f, -0.234f)
                lineToRelative(-1.57f, -2.66f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.272f, -0.072f)
                moveToRelative(0.05f, 0.484f)
                lineToRelative(1.315f, 2.227f)
                curveToRelative(-0.273f, 0.128f, -0.553f, 0.243f, -0.773f, 0.248f)
                curveToRelative(-0.276f, 0.006f, -0.449f, -0.076f, -0.52f, -0.198f)
                lineToRelative(-0.69f, -1.17f)
                curveToRelative(-0.07f, -0.12f, -0.057f, -0.315f, 0.083f, -0.554f)
                curveToRelative(0.111f, -0.19f, 0.343f, -0.38f, 0.586f, -0.553f)
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(35.09f, 34.336f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.272f, 0.072f)
                lineToRelative(-1.57f, 2.66f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.303f, 0.235f)
                lineToRelative(-0.223f, 0.029f)
                curveToRelative(0.391f, 0.23f, 0.798f, 0.355f, 1.16f, 0.363f)
                reflectiveCurveToRelative(0.704f, -0.107f, 0.873f, -0.394f)
                lineToRelative(0.692f, -1.17f)
                curveToRelative(0.169f, -0.29f, 0.1f, -0.643f, -0.082f, -0.957f)
                curveToRelative(-0.183f, -0.314f, -0.489f, -0.612f, -0.881f, -0.838f)
                moveToRelative(-0.053f, 0.486f)
                curveToRelative(0.244f, 0.174f, 0.478f, 0.362f, 0.59f, 0.553f)
                curveToRelative(0.14f, 0.239f, 0.15f, 0.434f, 0.08f, 0.555f)
                lineToRelative(-0.69f, 1.168f)
                verticalLineToRelative(0.002f)
                curveToRelative(-0.07f, 0.12f, -0.243f, 0.201f, -0.519f, 0.195f)
                curveToRelative(-0.22f, -0.005f, -0.502f, -0.117f, -0.775f, -0.246f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(23.1f, 31.82f)
                lineToRelative(-0.06f, 7.59f)
                lineToRelative(0.26f, -0.17f)
                lineToRelative(0.97f, 0.65f)
                lineToRelative(-0.03f, -8.07f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                stroke = SolidColor(Color(0xFF231F20)),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23.1f, 31.62f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.2f, 0.198f)
                lineToRelative(-0.06f, 7.59f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.31f, 0.17f)
                lineToRelative(0.149f, -0.098f)
                lineToRelative(0.86f, 0.577f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.312f, -0.168f)
                lineToRelative(-0.032f, -8.07f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.199f, -0.2f)
                close()
                moveTo(23.299f, 32.02f)
                horizontalLineToRelative(0.742f)
                lineToRelative(0.027f, 7.496f)
                lineToRelative(-0.656f, -0.442f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.17f, -0.002f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20.77f, 32.695f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, 0.125f)
                verticalLineToRelative(1.94f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.125f, 0.125f)
                horizontalLineToRelative(1.41f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.125f, -0.125f)
                verticalLineToRelative(-1.94f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, -0.125f)
                close()
                moveTo(20.895f, 32.945f)
                horizontalLineToRelative(1.16f)
                verticalLineToRelative(1.69f)
                horizontalLineToRelative(-1.16f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF231F20)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(25.13f, 32.695f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.124f, 0.125f)
                verticalLineToRelative(1.94f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.125f, 0.125f)
                horizontalLineToRelative(1.408f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.125f, -0.125f)
                verticalLineToRelative(-1.94f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, -0.125f)
                close()
                moveTo(25.256f, 32.945f)
                horizontalLineToRelative(1.158f)
                verticalLineToRelative(1.69f)
                horizontalLineToRelative(-1.158f)
                close()
            }
        }.build()

        return _Plushies!!
    }

@Suppress("ObjectPropertyName")
private var _Plushies: ImageVector? = null
