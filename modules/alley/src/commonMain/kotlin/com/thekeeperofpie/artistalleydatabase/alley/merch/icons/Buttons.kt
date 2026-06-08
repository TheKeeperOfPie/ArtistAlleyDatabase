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
val MerchIcons.Buttons: ImageVector
    get() {
        if (_Buttons != null) {
            return _Buttons!!
        }
        _Buttons = ImageVector.Builder(
            name = "Buttons",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.68f, 3.404f)
                curveToRelative(-9.073f, 0f, -16.436f, 7.363f, -16.436f, 16.436f)
                curveToRelative(0f, 9.072f, 7.363f, 16.435f, 16.436f, 16.435f)
                curveToRelative(9.072f, 0f, 16.435f, -7.363f, 16.435f, -16.435f)
                curveToRelative(0f, -9.073f, -7.363f, -16.436f, -16.435f, -16.436f)
                moveToRelative(0f, 0.75f)
                curveToRelative(8.667f, 0f, 15.685f, 7.019f, 15.685f, 15.686f)
                reflectiveCurveTo(26.347f, 35.525f, 17.68f, 35.525f)
                reflectiveCurveTo(1.994f, 28.507f, 1.994f, 19.84f)
                reflectiveCurveTo(9.013f, 4.154f, 17.68f, 4.154f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7.86f, 9.08f)
                curveToRelative(-5.94f, 1.64f, -5.65f, 7.27f, -5.65f, 7.27f)
                lineToRelative(1.41f, 2.85f)
                horizontalLineToRelative(0.01f)
                lineToRelative(0.26f, -0.57f)
                curveToRelative(3.5f, -6.15f, 6.18f, -7.49f, 6.18f, -7.49f)
                close()
                moveTo(8.31f, 10.48f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.249f, 0.176f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.164f, 0.313f)
                curveToRelative(-1.593f, 0.496f, -2.832f, 1.918f, -3.663f, 3.234f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.246f, 2.5f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.318f, 0.152f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.154f, -0.318f)
                reflectiveCurveToRelative(0.434f, -1.235f, 1.297f, -2.601f)
                curveToRelative(0.862f, -1.367f, 2.157f, -2.89f, 3.935f, -3.444f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.065f, -0.012f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.914f, 18.703f)
                curveToRelative(-0.681f, 0.32f, -1.471f, 1.069f, -2.326f, 2.018f)
                reflectiveCurveTo(8.83f, 22.82f, 8.004f, 23.934f)
                curveToRelative(-1.546f, 2.08f, -2.645f, 3.78f, -2.807f, 4.027f)
                curveToRelative(-0.076f, -0.095f, -0.149f, -0.154f, -0.226f, -0.342f)
                arcToRelative(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.31f, -1.045f)
                curveToRelative(-0.166f, -0.737f, -0.243f, -1.431f, -0.243f, -1.431f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.441f, -0.131f)
                curveToRelative(-0.446f, 0.545f, -0.72f, 0.836f, -1.127f, 1.175f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.032f, 0.354f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.352f, 0.031f)
                curveToRelative(0.303f, -0.252f, 0.562f, -0.562f, 0.836f, -0.873f)
                curveToRelative(0.036f, 0.27f, 0.052f, 0.474f, 0.166f, 0.985f)
                curveToRelative(0.086f, 0.385f, 0.195f, 0.783f, 0.336f, 1.125f)
                curveToRelative(0.14f, 0.34f, 0.297f, 0.638f, 0.584f, 0.79f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.328f, -0.083f)
                reflectiveCurveToRelative(1.341f, -2.071f, 2.984f, -4.284f)
                curveToRelative(0.822f, -1.106f, 1.72f, -2.248f, 2.557f, -3.177f)
                curveToRelative(0.761f, -0.846f, 1.442f, -1.424f, 1.969f, -1.727f)
                curveToRelative(0.092f, 0.268f, 0.527f, 1.591f, 1.523f, 3.318f)
                curveToRelative(1.092f, 1.894f, 2.694f, 3.967f, 4.828f, 4.584f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.319f, -0.265f)
                reflectiveCurveToRelative(-0.205f, -2.042f, -0.258f, -4.012f)
                curveToRelative(-0.027f, -0.985f, -0.016f, -1.954f, 0.074f, -2.621f)
                curveToRelative(0.045f, -0.333f, 0.115f, -0.592f, 0.18f, -0.713f)
                curveToRelative(0.029f, -0.054f, 0.049f, -0.066f, 0.05f, -0.068f)
                horizontalLineToRelative(0.008f)
                curveToRelative(3.817f, 1.635f, 6.098f, 4.001f, 7.514f, 5.9f)
                curveToRelative(0.708f, 0.95f, 1.197f, 1.78f, 1.557f, 2.352f)
                curveToRelative(0.18f, 0.286f, 0.324f, 0.506f, 0.47f, 0.662f)
                arcToRelative(0.6f, 0.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.26f, 0.185f)
                curveToRelative(0.114f, 0.04f, 0.283f, 0.011f, 0.377f, -0.07f)
                curveToRelative(0.23f, -0.197f, 0.352f, -0.492f, 0.45f, -0.82f)
                arcToRelative(6.6f, 6.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.2f, -1.047f)
                curveToRelative(0.054f, -0.443f, 0.05f, -0.595f, 0.057f, -0.838f)
                curveToRelative(0.49f, 0.392f, 0.97f, 0.773f, 1.365f, 1.059f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.348f, -0.057f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.057f, -0.35f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.716f, -1.33f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.409f, 0.192f)
                reflectiveCurveToRelative(-0.003f, 0.601f, -0.084f, 1.265f)
                curveToRelative(-0.04f, 0.332f, -0.099f, 0.68f, -0.183f, 0.965f)
                arcToRelative(1.4f, 1.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.25f, 0.494f)
                curveToRelative(-0.091f, -0.098f, -0.231f, -0.3f, -0.405f, -0.576f)
                curveToRelative(-0.352f, -0.56f, -0.854f, -1.412f, -1.58f, -2.385f)
                curveToRelative(-1.45f, -1.946f, -3.808f, -4.387f, -7.72f, -6.062f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.098f, -0.02f)
                horizontalLineToRelative(-0.01f)
                lineToRelative(0.098f, 0.02f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.416f, 0.008f)
                arcToRelative(0.65f, 0.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.266f, 0.285f)
                curveToRelative(-0.122f, 0.227f, -0.188f, 0.521f, -0.236f, 0.883f)
                curveToRelative(-0.097f, 0.722f, -0.105f, 1.703f, -0.078f, 2.7f)
                curveToRelative(0.047f, 1.752f, 0.189f, 3.143f, 0.228f, 3.555f)
                curveToRelative(-1.733f, -0.685f, -3.192f, -2.405f, -4.183f, -4.125f)
                curveToRelative(-1.064f, -1.844f, -1.63f, -3.544f, -1.63f, -3.544f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.343f, -0.149f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.477f, 8.42f)
                curveToRelative(-0.01f, 0f, -2.29f, -0.014f, -5.153f, 1.312f)
                curveToRelative(-2.867f, 1.33f, -6.334f, 4.012f, -8.722f, 9.366f)
                curveToRelative(-0.001f, 0.002f, -0.878f, 1.916f, -1.866f, 3.931f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.114f, 0.336f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.334f, -0.115f)
                curveToRelative(0.991f, -2.024f, 1.873f, -3.945f, 1.873f, -3.945f)
                lineToRelative(0.002f, -0.002f)
                curveToRelative(2.34f, -5.247f, 5.7f, -7.83f, 8.474f, -9.115f)
                curveTo(15.308f, 8.9f, 17.48f, 8.92f, 17.48f, 8.92f)
                horizontalLineToRelative(0.013f)
                reflectiveCurveToRelative(2.393f, -0.111f, 5.24f, 1.195f)
                reflectiveCurveToRelative(6.133f, 4.016f, 7.899f, 9.729f)
                curveToRelative(0.005f, 0.02f, 0.482f, 1.924f, 1.556f, 3.402f)
                lineToRelative(0.004f, 0.006f)
                curveToRelative(0.212f, 0.28f, 0.47f, 0.666f, 0.739f, 1.084f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.345f, 0.074f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.075f, -0.346f)
                curveToRelative(-0.27f, -0.42f, -0.53f, -0.81f, -0.758f, -1.11f)
                lineToRelative(-0.002f, -0.005f)
                curveToRelative(-0.998f, -1.374f, -1.477f, -3.24f, -1.477f, -3.24f)
                lineToRelative(-0.004f, -0.014f)
                curveToRelative(-1.804f, -5.837f, -5.213f, -8.677f, -8.168f, -10.033f)
                curveTo(20f, 8.312f, 17.503f, 8.42f, 17.482f, 8.42f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(7.479f, 24.682f)
                lineToRelative(-2.6f, 3.52f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.022f, 0.26f)
                curveToRelative(0.366f, 0.726f, 1f, 1.746f, 1.817f, 2.667f)
                reflectiveCurveToRelative(1.814f, 1.747f, 2.931f, 2.043f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.3f, -0.33f)
                reflectiveCurveToRelative(-0.589f, -1.556f, -1.13f, -3.342f)
                arcToRelative(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.695f, -2.678f)
                curveToRelative(-0.167f, -0.834f, -0.232f, -1.564f, -0.154f, -1.953f)
                horizontalLineToRelative(-0.008f)
                lineToRelative(0.012f, -0.039f)
                verticalLineToRelative(-0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.48f, -0.047f)
                close()
                moveTo(7.437f, 24.805f)
                quadToRelative(0.002f, 0.008f, 0.002f, 0.015f)
                horizontalLineToRelative(-0.005f)
                close()
                moveTo(7.488f, 25.51f)
                curveToRelative(0.01f, 0.441f, -0.01f, 0.845f, 0.102f, 1.41f)
                curveToRelative(0.172f, 0.86f, 0.434f, 1.822f, 0.707f, 2.725f)
                curveToRelative(0.453f, 1.495f, 0.79f, 2.381f, 0.943f, 2.794f)
                curveToRelative(-0.785f, -0.345f, -1.547f, -0.917f, -2.191f, -1.642f)
                arcToRelative(12.4f, 12.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.664f, -2.44f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(27.836f, 25.834f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.426f, 0.152f)
                curveToRelative(-0.103f, 1.091f, -0.746f, 2.82f, -1.367f, 4.248f)
                curveToRelative(-0.621f, 1.43f, -1.215f, 2.57f, -1.215f, 2.57f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.317f, 0.348f)
                curveToRelative(3.294f, -1.347f, 4.412f, -4.662f, 4.412f, -4.662f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.032f, -0.222f)
                lineToRelative(-1.65f, -2.39f)
                lineToRelative(-0.027f, -0.034f)
                close()
                moveTo(27.74f, 26.561f)
                lineTo(29.031f, 28.43f)
                curveToRelative(-0.055f, 0.155f, -0.992f, 2.473f, -3.394f, 3.834f)
                curveToRelative(0.215f, -0.429f, 0.423f, -0.811f, 0.865f, -1.828f)
                curveToRelative(0.553f, -1.274f, 1.028f, -2.699f, 1.238f, -3.875f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7.666f, 25.621f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.236f, 0.264f)
                reflectiveCurveToRelative(0.114f, 2.086f, 1.418f, 4.172f)
                reflectiveCurveToRelative(3.828f, 4.173f, 8.521f, 4.173f)
                curveToRelative(4.688f, 0f, 7.345f, -1.941f, 8.791f, -3.886f)
                reflectiveCurveToRelative(1.707f, -3.9f, 1.707f, -3.9f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.215f, -0.282f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.279f, 0.215f)
                reflectiveCurveToRelative(-0.245f, 1.828f, -1.613f, 3.668f)
                curveToRelative(-1.369f, 1.84f, -3.84f, 3.685f, -8.39f, 3.685f)
                curveToRelative(-4.548f, 0f, -6.867f, -1.967f, -8.099f, -3.937f)
                curveToRelative(-1.23f, -1.97f, -1.341f, -3.938f, -1.341f, -3.938f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.264f, -0.234f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(27.05f, 8.99f)
                lineToRelative(-1.98f, 1.9f)
                reflectiveCurveToRelative(2.49f, 1.43f, 5.68f, 7.72f)
                horizontalLineToRelative(-0.01f)
                verticalLineToRelative(0.02f)
                lineToRelative(1.59f, -2.1f)
                reflectiveCurveToRelative(0.55f, -5.62f, -5.28f, -7.54f)
                moveToRelative(-0.562f, 1.35f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.098f, 0.012f)
                curveToRelative(1.693f, 0.54f, 2.925f, 2.01f, 3.746f, 3.328f)
                curveToRelative(0.82f, 1.317f, 1.234f, 2.508f, 1.234f, 2.508f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.154f, 0.318f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.318f, -0.154f)
                reflectiveCurveToRelative(-0.397f, -1.14f, -1.186f, -2.407f)
                reflectiveCurveToRelative(-1.968f, -2.637f, -3.474f, -3.117f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.163f, -0.314f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.217f, -0.174f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.121f, 25.26f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.23f, 0.27f)
                curveToRelative(0.062f, 0.821f, 0.62f, 2.15f, 1.158f, 3.33f)
                curveToRelative(0.48f, 1.054f, 0.853f, 1.764f, 0.947f, 1.945f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.14f, 0.218f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.296f, 0.192f)
                lineToRelative(0.23f, -0.05f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.169f, -0.36f)
                reflectiveCurveToRelative(-0.517f, -0.987f, -1.047f, -2.153f)
                reflectiveCurveToRelative(-1.068f, -2.544f, -1.115f, -3.162f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.268f, -0.23f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(30.332f, 25.031f)
                arcToRelative(0.37f, 0.37f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.217f, 0.137f)
                curveToRelative(-0.094f, 0.123f, -0.112f, 0.25f, -0.125f, 0.412f)
                curveToRelative(-0.047f, 0.618f, -0.583f, 1.997f, -1.113f, 3.162f)
                reflectiveCurveToRelative(-1.049f, 2.153f, -1.049f, 2.153f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.166f, 0.359f)
                lineToRelative(0.95f, 0.219f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.3f, -0.186f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.187f, -0.3f)
                lineToRelative(-0.645f, -0.15f)
                curveToRelative(0.113f, -0.216f, 0.454f, -0.865f, 0.92f, -1.888f)
                curveToRelative(0.53f, -1.162f, 1.065f, -2.457f, 1.14f, -3.281f)
                curveToRelative(0.063f, 0.068f, 0.105f, 0.099f, 0.2f, 0.248f)
                curveToRelative(0.304f, 0.48f, 0.668f, 1.242f, 0.88f, 1.717f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.33f, 0.125f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.126f, -0.33f)
                curveToRelative(-0.218f, -0.486f, -0.579f, -1.253f, -0.914f, -1.782f)
                curveToRelative(-0.168f, -0.264f, -0.289f, -0.472f, -0.532f, -0.584f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.23f, -0.03f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.252f, 32.936f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.262f, 0.38f)
                lineToRelative(1.36f, 2.1f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.345f, 0.074f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.075f, -0.345f)
                lineToRelative(-1.034f, -1.596f)
                lineToRelative(3.528f, 0.748f)
                lineToRelative(-1.024f, 1.18f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.026f, 0.351f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.353f, -0.023f)
                lineToRelative(1.299f, -1.5f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.137f, -0.41f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(22.71f, 33.004f)
                lineToRelative(-5.108f, 0.809f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.143f, 0.42f)
                lineToRelative(1.658f, 1.76f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.354f, 0.009f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.011f, -0.354f)
                lineToRelative(-1.336f, -1.416f)
                lineToRelative(4.047f, -0.642f)
                lineToRelative(-1.214f, 1.674f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.054f, 0.347f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.35f, -0.054f)
                lineToRelative(1.57f, -2.16f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.242f, -0.393f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(28.557f, 8.451f)
                curveToRelative(-0.17f, 0.107f, -0.292f, 0.261f, -0.399f, 0.418f)
                curveToRelative(-0.213f, 0.314f, -0.35f, 0.656f, -0.35f, 0.656f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.137f, 0.327f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.326f, -0.14f)
                reflectiveCurveToRelative(0.129f, -0.31f, 0.301f, -0.564f)
                curveToRelative(0.086f, -0.127f, 0.186f, -0.233f, 0.25f, -0.273f)
                curveToRelative(0.065f, -0.04f, 0.038f, -0.045f, 0.08f, -0.014f)
                curveToRelative(0.067f, 0.05f, 0.29f, 0.278f, 0.532f, 0.56f)
                curveToRelative(0.242f, 0.284f, 0.524f, 0.632f, 0.79f, 0.97f)
                arcToRelative(73f, 73f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.006f, 1.308f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.35f, 0.051f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.05f, -0.35f)
                reflectiveCurveToRelative(-0.475f, -0.636f, -1.013f, -1.318f)
                curveToRelative(-0.269f, -0.34f, -0.553f, -0.693f, -0.803f, -0.984f)
                curveToRelative(-0.249f, -0.292f, -0.444f, -0.515f, -0.617f, -0.641f)
                curveToRelative(-0.197f, -0.144f, -0.47f, -0.113f, -0.64f, -0.006f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.643f, 8.51f)
                curveToRelative(-0.17f, -0.107f, -0.443f, -0.137f, -0.641f, 0.008f)
                curveToRelative(-0.173f, 0.126f, -0.368f, 0.347f, -0.617f, 0.638f)
                curveToRelative(-0.25f, 0.292f, -0.532f, 0.644f, -0.801f, 0.985f)
                curveToRelative(-0.537f, 0.681f, -1.014f, 1.32f, -1.014f, 1.32f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.051f, 0.35f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.35f, -0.051f)
                reflectiveCurveToRelative(0.473f, -0.633f, 1.006f, -1.309f)
                curveToRelative(0.266f, -0.338f, 0.547f, -0.686f, 0.789f, -0.969f)
                reflectiveCurveToRelative(0.464f, -0.511f, 0.53f, -0.56f)
                curveToRelative(0.043f, -0.031f, 0.017f, -0.029f, 0.081f, 0.012f)
                curveToRelative(0.064f, 0.04f, 0.166f, 0.148f, 0.252f, 0.275f)
                curveToRelative(0.172f, 0.254f, 0.299f, 0.564f, 0.299f, 0.564f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.326f, 0.14f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.139f, -0.327f)
                reflectiveCurveToRelative(-0.14f, -0.345f, -0.352f, -0.658f)
                arcToRelative(1.4f, 1.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.398f, -0.418f)
            }
            path(
                fill = SolidColor(Color(0xFF010202)),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.1f, 22.59f)
                horizontalLineToRelative(0.01f)
                curveToRelative(0.316f, 0f, 0.57f, 0.254f, 0.57f, 0.57f)
                verticalLineToRelative(2.96f)
                curveToRelative(0f, 0.316f, -0.254f, 0.57f, -0.57f, 0.57f)
                horizontalLineToRelative(-0.01f)
                arcToRelative(0.57f, 0.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.57f, -0.57f)
                verticalLineToRelative(-2.96f)
                curveToRelative(0f, -0.316f, 0.254f, -0.57f, 0.57f, -0.57f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.1f, 22.465f)
                arcToRelative(0.697f, 0.697f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.696f, 0.695f)
                verticalLineToRelative(2.96f)
                curveToRelative(0f, 0.382f, 0.313f, 0.694f, 0.696f, 0.694f)
                horizontalLineToRelative(0.01f)
                arcToRelative(0.697f, 0.697f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.695f, -0.695f)
                lineTo(12.805f, 23.16f)
                arcToRelative(0.697f, 0.697f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.696f, -0.695f)
                close()
                moveTo(12.1f, 22.715f)
                horizontalLineToRelative(0.01f)
                curveToRelative(0.248f, 0f, 0.445f, 0.196f, 0.445f, 0.445f)
                verticalLineToRelative(2.96f)
                arcToRelative(0.44f, 0.44f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.446f, 0.444f)
                horizontalLineToRelative(-0.01f)
                arcToRelative(0.44f, 0.44f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.445f, -0.445f)
                lineTo(11.654f, 23.16f)
                curveToRelative(0f, -0.249f, 0.197f, -0.445f, 0.446f, -0.445f)
            }
            path(
                fill = SolidColor(Color(0xFF010202)),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23f, 22.59f)
                horizontalLineToRelative(0.01f)
                curveToRelative(0.316f, 0f, 0.57f, 0.254f, 0.57f, 0.57f)
                verticalLineToRelative(2.96f)
                curveToRelative(0f, 0.316f, -0.254f, 0.57f, -0.57f, 0.57f)
                horizontalLineTo(23f)
                arcToRelative(0.57f, 0.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.57f, -0.57f)
                verticalLineToRelative(-2.96f)
                curveToRelative(0f, -0.316f, 0.254f, -0.57f, 0.57f, -0.57f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23f, 22.465f)
                arcToRelative(0.697f, 0.697f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.695f, 0.695f)
                verticalLineToRelative(2.96f)
                curveToRelative(0f, 0.382f, 0.312f, 0.694f, 0.695f, 0.694f)
                horizontalLineToRelative(0.01f)
                arcToRelative(0.697f, 0.697f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.695f, -0.695f)
                lineTo(23.705f, 23.16f)
                arcToRelative(0.697f, 0.697f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.695f, -0.695f)
                close()
                moveTo(23f, 22.715f)
                horizontalLineToRelative(0.01f)
                curveToRelative(0.248f, 0f, 0.445f, 0.196f, 0.445f, 0.445f)
                verticalLineToRelative(2.96f)
                arcToRelative(0.44f, 0.44f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.445f, 0.444f)
                lineTo(23f, 26.564f)
                arcToRelative(0.44f, 0.44f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.445f, -0.445f)
                lineTo(22.555f, 23.16f)
                curveToRelative(0f, -0.249f, 0.196f, -0.445f, 0.445f, -0.445f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14.9f, 28.72f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, 0.15f)
                curveToRelative(0f, 1.32f, 1.305f, 2.36f, 2.89f, 2.36f)
                reflectiveCurveToRelative(2.89f, -1.04f, 2.89f, -2.36f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, -0.15f)
                horizontalLineToRelative(-0.01f)
                close()
                moveTo(15.129f, 29.02f)
                horizontalLineToRelative(5.023f)
                curveToRelative(-0.11f, 1.042f, -1.132f, 1.91f, -2.511f, 1.91f)
                reflectiveCurveToRelative(-2.402f, -0.868f, -2.512f, -1.91f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveToRelative(30.146f, 9.773f)
                lineToRelative(0.499f, 0.637f)
                curveToRelative(0.559f, 0.717f, 1.066f, 1.469f, 1.494f, 2.276f)
                lineToRelative(0.088f, 0.166f)
                lineToRelative(0.185f, 0.029f)
                curveToRelative(6.44f, 1.002f, 11.383f, 6.563f, 11.383f, 13.289f)
                curveToRelative(0f, 7.447f, -6.027f, 13.475f, -13.475f, 13.475f)
                arcToRelative(13.43f, 13.43f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.535f, -3.95f)
                lineToRelative(-0.13f, -0.133f)
                lineToRelative(-0.186f, 0.026f)
                curveToRelative(-0.72f, 0.098f, -1.442f, 0.168f, -2.168f, 0.168f)
                curveToRelative(-0.186f, 0f, -0.381f, -0.02f, -0.604f, -0.031f)
                lineToRelative(-0.818f, -0.04f)
                lineToRelative(0.506f, 0.647f)
                curveToRelative(3.008f, 3.837f, 7.678f, 6.313f, 12.935f, 6.313f)
                curveToRelative(9.073f, 0f, 16.436f, -7.361f, 16.436f, -16.434f)
                curveToRelative(0f, -9.063f, -7.03f, -16.069f, -15.8f, -16.406f)
                close()
                moveTo(31.701f, 10.658f)
                curveToRelative(8.014f, 0.697f, 14.305f, 7.151f, 14.305f, 15.553f)
                curveToRelative(0f, 8.667f, -7.018f, 15.684f, -15.686f, 15.684f)
                curveToRelative(-4.711f, 0f, -8.868f, -2.129f, -11.742f, -5.41f)
                arcToRelative(17f, 17f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.822f, -0.14f)
                curveToRelative(2.566f, 2.5f, 6.057f, 4.05f, 9.92f, 4.05f)
                curveToRelative(7.853f, 0f, 14.225f, -6.372f, 14.225f, -14.225f)
                curveToRelative(0f, -7.024f, -5.129f, -12.826f, -11.83f, -13.967f)
                curveToRelative(-0.299f, -0.548f, -0.658f, -1.045f, -1.014f, -1.545f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(33.44f, 23.555f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.376f, 0.375f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.375f, 0.375f)
                horizontalLineToRelative(5.93f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.375f, -0.375f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.375f, -0.375f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF020202)),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(39.29f, 22.81f)
                horizontalLineToRelative(0.52f)
                curveToRelative(0.615f, 0f, 1.11f, 0.495f, 1.11f, 1.11f)
                verticalLineToRelative(0.01f)
                curveToRelative(0f, 0.615f, -0.495f, 1.11f, -1.11f, 1.11f)
                horizontalLineToRelative(-0.52f)
                curveToRelative(-0.615f, 0f, -1.11f, -0.495f, -1.11f, -1.11f)
                verticalLineToRelative(-0.01f)
                curveToRelative(0f, -0.615f, 0.495f, -1.11f, 1.11f, -1.11f)
            }
            path(
                fill = SolidColor(Color(0xFF020202)),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(39.29f, 22.56f)
                curveToRelative(-0.75f, 0f, -1.36f, 0.61f, -1.36f, 1.36f)
                verticalLineToRelative(0.01f)
                curveToRelative(0f, 0.749f, 0.61f, 1.36f, 1.36f, 1.36f)
                horizontalLineToRelative(0.52f)
                curveToRelative(0.75f, 0f, 1.36f, -0.611f, 1.36f, -1.36f)
                verticalLineToRelative(-0.01f)
                curveToRelative(0f, -0.75f, -0.61f, -1.36f, -1.36f, -1.36f)
                close()
                moveTo(39.29f, 23.06f)
                horizontalLineToRelative(0.52f)
                curveToRelative(0.481f, 0f, 0.86f, 0.38f, 0.86f, 0.86f)
                verticalLineToRelative(0.01f)
                curveToRelative(0f, 0.48f, -0.379f, 0.86f, -0.86f, 0.86f)
                horizontalLineToRelative(-0.52f)
                arcToRelative(0.85f, 0.85f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.86f, -0.86f)
                verticalLineToRelative(-0.01f)
                curveToRelative(0f, -0.48f, 0.378f, -0.86f, 0.86f, -0.86f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(40.05f, 35.28f)
                lineToRelative(0.86f, 3.12f)
                lineToRelative(2.28f, 1.16f)
                lineToRelative(-2.28f, 1.16f)
                lineToRelative(-0.82f, 3.14f)
                lineToRelative(-0.85f, -3.13f)
                lineToRelative(-2.28f, -1.15f)
                lineToRelative(2.26f, -1.16f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(40.291f, 35.213f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.482f, 0.004f)
                lineToRelative(-0.803f, 3.031f)
                lineToRelative(-2.16f, 1.11f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.002f, 0.445f)
                lineToRelative(2.18f, 1.1f)
                lineToRelative(0.822f, 3.023f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.482f, -0.002f)
                lineToRelative(0.791f, -3.031f)
                lineToRelative(2.18f, -1.11f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -0.445f)
                lineToRelative(-2.182f, -1.111f)
                close()
                moveTo(40.055f, 36.238f)
                lineTo(40.67f, 38.467f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.127f, 0.156f)
                lineToRelative(1.842f, 0.938f)
                lineToRelative(-1.842f, 0.937f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.129f, 0.158f)
                lineToRelative(-0.582f, 2.23f)
                lineToRelative(-0.606f, -2.222f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.126f, -0.158f)
                lineToRelative(-1.842f, -0.928f)
                lineToRelative(1.822f, -0.935f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.127f, -0.159f)
                close()
            }
        }.build()

        return _Buttons!!
    }

@Suppress("ObjectPropertyName")
private var _Buttons: ImageVector? = null
