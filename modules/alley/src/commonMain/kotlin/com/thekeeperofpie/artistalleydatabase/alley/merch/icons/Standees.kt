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
val MerchIcons.Standees: ImageVector
    get() {
        if (_Standees != null) {
            return _Standees!!
        }
        _Standees = ImageVector.Builder(
            name = "Standees",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(15.53f, 12.27f)
                lineToRelative(-0.92f, -1.34f)
                reflectiveCurveToRelative(-0.18f, -3.48f, 3.49f, -4.49f)
                lineToRelative(1.16f, 1.24f)
                reflectiveCurveToRelative(-1.58f, 0.81f, -3.73f, 4.6f)
                verticalLineToRelative(0f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18.066f, 6.32f)
                curveToRelative(-1.87f, 0.515f, -2.778f, 1.673f, -3.203f, 2.696f)
                reflectiveCurveToRelative(-0.379f, 1.92f, -0.379f, 1.92f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.024f, 0.064f)
                lineToRelative(0.92f, 1.34f)
                lineToRelative(-0.02f, -0.06f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.23f, 0.062f)
                curveToRelative(1.071f, -1.887f, 1.998f, -3.028f, 2.653f, -3.696f)
                curveToRelative(0.655f, -0.667f, 1.025f, -0.855f, 1.025f, -0.855f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.036f, -0.197f)
                lineToRelative(-1.16f, -1.239f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.126f, -0.035f)
                moveToRelative(0.006f, 0.274f)
                lineTo(19.08f, 7.67f)
                curveToRelative(-0.082f, 0.046f, -0.372f, 0.193f, -0.969f, 0.8f)
                curveToRelative(-0.643f, 0.656f, -1.552f, 1.827f, -2.574f, 3.589f)
                lineToRelative(-0.797f, -1.16f)
                curveToRelative(0f, -0.024f, -0.043f, -0.832f, 0.354f, -1.788f)
                curveToRelative(0.396f, -0.955f, 1.234f, -2.012f, 2.978f, -2.517f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(24.047f, 5.92f)
                curveToRelative(-0.009f, 0f, -1.438f, -0.008f, -3.223f, 0.818f)
                curveToRelative(-1.79f, 0.83f, -3.954f, 2.507f, -5.443f, 5.84f)
                curveToRelative(-0.001f, 0.002f, -0.606f, 1.322f, -1.262f, 2.64f)
                curveToRelative(-0.328f, 0.66f, -0.668f, 1.32f, -0.95f, 1.81f)
                curveToRelative(-0.14f, 0.244f, -0.265f, 0.446f, -0.36f, 0.577f)
                curveToRelative(-0.048f, 0.066f, -0.091f, 0.114f, -0.114f, 0.135f)
                curveToRelative(-0.022f, 0.022f, -0.034f, -0.01f, 0.075f, -0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.067f, 0.49f)
                reflectiveCurveToRelative(0.065f, -0.004f, 0.068f, -0.003f)
                lineToRelative(-0.002f, 0.013f)
                curveToRelative(0.018f, 0f, 0.015f, -0.012f, 0.03f, -0.013f)
                curveToRelative(0.07f, 0.016f, 0.395f, 0.103f, 0.951f, -0.047f)
                curveToRelative(0.52f, -0.141f, 1.196f, -0.562f, 1.902f, -1.3f)
                curveToRelative(0.026f, 0.17f, 0.023f, 0.218f, 0.076f, 0.454f)
                curveToRelative(0.055f, 0.241f, 0.123f, 0.492f, 0.215f, 0.711f)
                curveToRelative(0.092f, 0.22f, 0.192f, 0.423f, 0.41f, 0.537f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.327f, -0.086f)
                reflectiveCurveToRelative(0.828f, -1.277f, 1.841f, -2.64f)
                arcToRelative(28f, 28f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.573f, -1.957f)
                curveToRelative(0.435f, -0.485f, 0.807f, -0.785f, 1.107f, -0.979f)
                curveToRelative(0.087f, 0.248f, 0.308f, 0.934f, 0.89f, 1.945f)
                curveToRelative(0.68f, 1.18f, 1.676f, 2.483f, 3.04f, 2.875f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.318f, -0.265f)
                reflectiveCurveToRelative(-0.127f, -1.26f, -0.16f, -2.473f)
                curveToRelative(-0.017f, -0.606f, -0.009f, -1.202f, 0.045f, -1.603f)
                curveToRelative(0.024f, -0.179f, 0.061f, -0.296f, 0.09f, -0.364f)
                curveToRelative(2.289f, 0.999f, 3.677f, 2.42f, 4.533f, 3.57f)
                curveToRelative(0.434f, 0.583f, 0.733f, 1.092f, 0.957f, 1.448f)
                curveToRelative(0.112f, 0.178f, 0.2f, 0.314f, 0.303f, 0.422f)
                arcToRelative(0.47f, 0.47f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.203f, 0.14f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.324f, -0.066f)
                curveToRelative(0.167f, -0.146f, 0.244f, -0.343f, 0.307f, -0.555f)
                curveToRelative(0.062f, -0.211f, 0.1f, -0.442f, 0.127f, -0.66f)
                curveToRelative(0.024f, -0.206f, 0.018f, -0.24f, 0.025f, -0.388f)
                curveToRelative(1.098f, 0.852f, 1.942f, 1.397f, 2.38f, 1.511f)
                curveToRelative(0.256f, 0.067f, 0.445f, 0.057f, 0.585f, 0f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.162f, -0.107f)
                curveToRelative(0.037f, -0.039f, 0.066f, -0.096f, 0.066f, -0.096f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.01f, -0.23f)
                reflectiveCurveToRelative(-1.25f, -2.159f, -1.925f, -3.073f)
                curveToRelative(-0.608f, -0.822f, -0.898f, -1.97f, -0.898f, -1.97f)
                lineToRelative(-0.004f, -0.014f)
                curveToRelative(-1.125f, -3.633f, -3.253f, -5.415f, -5.098f, -6.262f)
                curveToRelative(-1.832f, -0.841f, -3.387f, -0.776f, -3.408f, -0.775f)
                close()
                moveTo(24.047f, 6.42f)
                horizontalLineToRelative(0.016f)
                reflectiveCurveToRelative(1.451f, -0.068f, 3.189f, 0.73f)
                reflectiveCurveToRelative(3.744f, 2.446f, 4.83f, 5.954f)
                lineToRelative(-0.004f, -0.014f)
                reflectiveCurveToRelative(0.288f, 1.211f, 0.98f, 2.148f)
                curveToRelative(0.564f, 0.763f, 1.498f, 2.371f, 1.708f, 2.729f)
                curveToRelative(-0.03f, -0.005f, -0.017f, 0.006f, -0.055f, -0.004f)
                curveToRelative(-0.37f, -0.097f, -1.156f, -0.487f, -2.572f, -1.656f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.409f, 0.193f)
                reflectiveCurveToRelative(0f, 0.362f, -0.048f, 0.764f)
                curveToRelative(-0.024f, 0.2f, -0.06f, 0.41f, -0.11f, 0.578f)
                curveToRelative(-0.024f, 0.083f, -0.05f, 0.103f, -0.078f, 0.158f)
                curveToRelative(-0.047f, -0.064f, -0.086f, -0.113f, -0.156f, -0.225f)
                curveToRelative(-0.216f, -0.343f, -0.53f, -0.872f, -0.98f, -1.478f)
                arcToRelative(11.5f, 11.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.81f, -3.777f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.099f, -0.02f)
                horizontalLineToRelative(-0.01f)
                lineToRelative(0.1f, 0.02f)
                arcToRelative(0.42f, 0.42f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.34f, 0.01f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.205f, 0.214f)
                curveToRelative(-0.086f, 0.161f, -0.125f, 0.35f, -0.156f, 0.578f)
                curveToRelative(-0.062f, 0.458f, -0.066f, 1.067f, -0.049f, 1.686f)
                curveToRelative(0.027f, 0.993f, 0.097f, 1.65f, 0.13f, 2.012f)
                curveToRelative(-0.97f, -0.452f, -1.818f, -1.411f, -2.396f, -2.414f)
                curveToRelative(-0.65f, -1.131f, -0.996f, -2.174f, -0.996f, -2.174f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.345f, -0.147f)
                curveToRelative(-0.446f, 0.212f, -0.93f, 0.68f, -1.46f, 1.27f)
                arcToRelative(29f, 29f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.6f, 1.992f)
                curveToRelative(-0.916f, 1.231f, -1.51f, 2.149f, -1.663f, 2.383f)
                curveToRelative(-0.024f, -0.04f, -0.032f, -0.035f, -0.055f, -0.088f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.187f, -0.627f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.149f, -0.871f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.443f, -0.131f)
                curveToRelative(-0.788f, 0.971f, -1.5f, 1.337f, -2.006f, 1.474f)
                curveToRelative(-0.186f, 0.05f, -0.166f, 0.014f, -0.287f, 0.02f)
                curveToRelative(0.086f, -0.131f, 0.171f, -0.255f, 0.272f, -0.43f)
                curveToRelative(0.29f, -0.505f, 0.634f, -1.17f, 0.964f, -1.836f)
                curveToRelative(0.661f, -1.33f, 1.27f, -2.656f, 1.27f, -2.656f)
                verticalLineToRelative(-0.004f)
                curveToRelative(1.441f, -3.226f, 3.498f, -4.804f, 5.195f, -5.59f)
                reflectiveCurveToRelative(3.014f, -0.771f, 3.014f, -0.771f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(17.79f, 16.002f)
                lineToRelative(-1.61f, 2.17f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.024f, 0.262f)
                curveToRelative(0.23f, 0.454f, 0.627f, 1.089f, 1.139f, 1.666f)
                reflectiveCurveToRelative(1.139f, 1.102f, 1.861f, 1.293f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.297f, -0.33f)
                reflectiveCurveToRelative(-0.362f, -0.96f, -0.695f, -2.06f)
                arcToRelative(18f, 18f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.428f, -1.644f)
                curveToRelative(-0.102f, -0.51f, -0.138f, -0.958f, -0.096f, -1.17f)
                horizontalLineToRelative(-0.006f)
                lineToRelative(0.012f, -0.039f)
                verticalLineToRelative(-0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.48f, -0.046f)
                close()
                moveTo(17.746f, 16.131f)
                lineTo(17.748f, 16.141f)
                horizontalLineToRelative(-0.004f)
                close()
                moveTo(17.796f, 16.832f)
                curveToRelative(0.022f, 0.213f, -0.005f, 0.38f, 0.044f, 0.625f)
                curveToRelative(0.107f, 0.537f, 0.27f, 1.132f, 0.44f, 1.691f)
                curveToRelative(0.223f, 0.74f, 0.331f, 1.008f, 0.466f, 1.381f)
                curveToRelative(-0.373f, -0.219f, -0.76f, -0.404f, -1.078f, -0.761f)
                arcToRelative(7.5f, 7.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.982f, -1.442f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(30.33f, 16.64f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, 0.227f)
                curveToRelative(-0.059f, 0.645f, -0.452f, 1.714f, -0.834f, 2.594f)
                reflectiveCurveToRelative(-0.748f, 1.584f, -0.748f, 1.584f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.318f, 0.346f)
                curveToRelative(2.074f, -0.858f, 2.78f, -2.94f, 2.78f, -2.94f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.03f, -0.222f)
                lineToRelative(-1.021f, -1.481f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.205f, -0.107f)
                close()
                moveTo(30.412f, 17.434f)
                lineTo(31.07f, 18.389f)
                curveToRelative(-0.048f, 0.13f, -0.62f, 1.07f, -1.656f, 1.888f)
                curveToRelative(0.124f, -0.26f, 0.142f, -0.274f, 0.291f, -0.617f)
                curveToRelative(0.316f, -0.727f, 0.555f, -1.517f, 0.707f, -2.226f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(32.24f, 12.33f)
                lineToRelative(0.98f, -1.3f)
                reflectiveCurveToRelative(0.34f, -3.47f, -3.27f, -4.66f)
                lineToRelative(-1.22f, 1.18f)
                reflectiveCurveToRelative(1.54f, 0.88f, 3.51f, 4.77f)
                verticalLineToRelative(0f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(29.988f, 6.252f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, 0.027f)
                lineToRelative(-1.22f, 1.182f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.025f, 0.197f)
                reflectiveCurveToRelative(0.361f, 0.204f, 0.984f, 0.9f)
                curveToRelative(0.623f, 0.698f, 1.496f, 1.882f, 2.477f, 3.819f)
                lineToRelative(-0.012f, -0.047f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.223f, 0.074f)
                lineToRelative(0.98f, -1.299f)
                arcToRelative(0.13f, 0.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.024f, -0.062f)
                reflectiveCurveToRelative(0.089f, -0.896f, -0.287f, -1.94f)
                curveToRelative(-0.376f, -1.043f, -1.228f, -2.244f, -3.069f, -2.851f)
                moveToRelative(-0.017f, 0.271f)
                curveToRelative(1.713f, 0.591f, 2.498f, 1.692f, 2.85f, 2.666f)
                curveToRelative(0.351f, 0.977f, 0.273f, 1.784f, 0.27f, 1.805f)
                lineToRelative(-0.841f, 1.113f)
                curveToRelative(-0.94f, -1.816f, -1.796f, -3.028f, -2.41f, -3.714f)
                curveToRelative(-0.567f, -0.635f, -0.848f, -0.794f, -0.928f, -0.844f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.201f, 6f)
                arcToRelative(2.1f, 2.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.976f, 0.148f)
                curveToRelative(-0.532f, 0.214f, -0.934f, 0.616f, -1.325f, 1.26f)
                reflectiveCurveToRelative(-0.789f, 1.542f, -1.35f, 2.822f)
                curveToRelative(-1.112f, 2.543f, -3.13f, 6.962f, -3.152f, 7.008f)
                curveToRelative(-0.564f, 0.547f, -2.372f, 2.208f, -3.07f, 4.127f)
                curveToRelative(-0.353f, 0.972f, -0.41f, 2.035f, 0.176f, 2.998f)
                curveToRelative(0.585f, 0.964f, 1.777f, 1.799f, 3.834f, 2.416f)
                curveToRelative(0f, 0f, -0.023f, -0.01f, -0.018f, -0.006f)
                curveToRelative(0.006f, 0.005f, 0.02f, 0.016f, 0.035f, 0.051f)
                curveToRelative(0.032f, 0.071f, 0.071f, 0.269f, -0.082f, 0.703f)
                arcToRelative(0.85f, 0.85f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.05f, 0.412f)
                arcToRelative(0.54f, 0.54f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.191f, 0.334f)
                curveToRelative(0.197f, 0.156f, 0.434f, 0.158f, 0.662f, 0.125f)
                curveToRelative(0.228f, -0.032f, 0.46f, -0.113f, 0.666f, -0.224f)
                reflectiveCurveToRelative(0.39f, -0.245f, 0.498f, -0.446f)
                curveToRelative(0.05f, -0.093f, 0.117f, -0.162f, 0.203f, -0.273f)
                curveToRelative(0.087f, -0.11f, 0.183f, -0.261f, 0.245f, -0.473f)
                curveToRelative(0.122f, -0.423f, 0.125f, -1.068f, -0.092f, -2.382f)
                curveToRelative(-0.199f, -1.201f, 0.324f, -2.728f, 0.918f, -3.94f)
                curveToRelative(0.593f, -1.212f, 1.238f, -2.103f, 1.238f, -2.103f)
                lineToRelative(0.012f, -0.016f)
                lineToRelative(1.11f, -1.82f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.366f, -0.328f)
                reflectiveCurveToRelative(-0.552f, 0.423f, -1.172f, 0.779f)
                curveToRelative(-0.31f, 0.178f, -0.637f, 0.336f, -0.902f, 0.414f)
                curveToRelative(-0.137f, 0.04f, -0.166f, 0.01f, -0.248f, 0.008f)
                lineToRelative(2.142f, -3.883f)
                lineToRelative(0.01f, -0.018f)
                reflectiveCurveToRelative(0.26f, -0.572f, 0.55f, -1.18f)
                curveToRelative(0.146f, -0.303f, 0.298f, -0.617f, 0.426f, -0.869f)
                curveToRelative(0.13f, -0.25f, 0.26f, -0.464f, 0.27f, -0.476f)
                lineToRelative(0.008f, -0.02f)
                lineToRelative(1.894f, -2.404f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.047f, -0.213f)
                curveToRelative(-0.256f, -1.07f, -0.754f, -1.798f, -1.363f, -2.193f)
                arcTo(2.1f, 2.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16.2f, 6f)
                moveToRelative(-0.789f, 0.611f)
                horizontalLineToRelative(0.002f)
                arcToRelative(1.57f, 1.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.484f, 0.147f)
                curveToRelative(0.457f, 0.296f, 0.856f, 0.903f, 1.096f, 1.805f)
                lineToRelative(-1.8f, 2.283f)
                lineToRelative(-0.01f, 0.031f)
                curveToRelative(-0.1f, 0.126f, -0.186f, 0.294f, -0.313f, 0.541f)
                curveToRelative(-0.133f, 0.259f, -0.287f, 0.573f, -0.433f, 0.879f)
                curveToRelative(-0.289f, 0.603f, -0.54f, 1.155f, -0.547f, 1.172f)
                lineToRelative(-2.32f, 4.2f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.058f, 0.311f)
                curveToRelative(0.265f, 0.223f, 0.614f, 0.184f, 0.945f, 0.086f)
                curveToRelative(0.332f, -0.097f, 0.683f, -0.272f, 1.012f, -0.46f)
                curveToRelative(0.146f, -0.084f, 0.14f, -0.098f, 0.271f, -0.182f)
                lineToRelative(-0.521f, 0.855f)
                lineToRelative(0.012f, -0.015f)
                reflectiveCurveToRelative(-0.67f, 0.922f, -1.284f, 2.175f)
                curveToRelative(-0.614f, 1.254f, -1.192f, 2.843f, -0.96f, 4.243f)
                curveToRelative(0.212f, 1.285f, 0.186f, 1.875f, 0.103f, 2.162f)
                arcToRelative(0.8f, 0.8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.158f, 0.302f)
                curveToRelative(-0.068f, 0.087f, -0.164f, 0.19f, -0.248f, 0.346f)
                curveToRelative(-0.027f, 0.05f, -0.141f, 0.158f, -0.295f, 0.24f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.502f, 0.17f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.28f, -0.021f)
                curveToRelative(-0.006f, -0.005f, -0.004f, 0.002f, -0.007f, -0.02f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.03f, -0.168f)
                curveToRelative(0.177f, -0.505f, 0.167f, -0.843f, 0.066f, -1.072f)
                arcToRelative(0.6f, 0.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.19f, -0.244f)
                arcToRelative(0.6f, 0.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.14f, -0.076f)
                curveToRelative(-1.99f, -0.598f, -3.057f, -1.383f, -3.551f, -2.197f)
                reflectiveCurveToRelative(-0.454f, -1.688f, -0.133f, -2.569f)
                curveToRelative(0.64f, -1.762f, 2.426f, -3.432f, 2.986f, -3.976f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.053f, -0.075f)
                reflectiveCurveToRelative(2.05f, -4.49f, 3.172f, -7.054f)
                curveToRelative(0.56f, -1.28f, 0.957f, -2.166f, 1.318f, -2.762f)
                reflectiveCurveToRelative(0.667f, -0.89f, 1.084f, -1.057f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(30.988f, 6.313f)
                curveToRelative(-0.617f, 0.221f, -1.247f, 0.734f, -1.857f, 1.66f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.027f, 0.31f)
                reflectiveCurveToRelative(0.655f, 0.687f, 1.354f, 1.588f)
                reflectiveCurveToRelative(1.433f, 2.032f, 1.593f, 2.809f)
                lineToRelative(0.163f, -0.198f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.164f, 0.227f)
                reflectiveCurveToRelative(0.47f, 1.43f, 0.675f, 1.918f)
                curveToRelative(0.062f, 0.147f, 0.18f, 0.348f, 0.332f, 0.6f)
                curveToRelative(0.152f, 0.25f, 0.334f, 0.54f, 0.51f, 0.816f)
                curveToRelative(0.35f, 0.545f, 0.667f, 1.025f, 0.674f, 1.035f)
                lineTo(35f, 18.252f)
                curveToRelative(-0.093f, -0.01f, -0.144f, 0.026f, -0.266f, -0.022f)
                curveToRelative(-0.269f, -0.104f, -0.58f, -0.293f, -0.867f, -0.5f)
                curveToRelative(-0.573f, -0.413f, -1.05f, -0.888f, -1.05f, -0.888f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.417f, 0.25f)
                reflectiveCurveToRelative(0.137f, 0.455f, 0.368f, 1.004f)
                reflectiveCurveToRelative(0.546f, 1.195f, 0.964f, 1.619f)
                curveToRelative(0.32f, 0.324f, 0.733f, 1.048f, 1.041f, 1.676f)
                curveToRelative(0.309f, 0.627f, 0.526f, 1.164f, 0.526f, 1.164f)
                curveToRelative(0.486f, 1.212f, 0.46f, 1.791f, 0.336f, 2.328f)
                curveToRelative(-0.125f, 0.537f, -0.393f, 1.077f, -0.356f, 2.027f)
                curveToRelative(0.038f, 0.977f, 0.541f, 1.572f, 1.094f, 1.824f)
                curveToRelative(0.277f, 0.127f, 0.562f, 0.175f, 0.816f, 0.162f)
                curveToRelative(0.255f, -0.012f, 0.483f, -0.073f, 0.653f, -0.234f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.197f, -0.492f)
                arcToRelative(0.8f, 0.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.174f, -0.393f)
                curveToRelative(-0.075f, -0.104f, -0.152f, -0.193f, -0.205f, -0.267f)
                curveToRelative(-0.029f, -0.041f, -0.028f, -0.05f, -0.039f, -0.074f)
                curveToRelative(0.02f, -0.01f, 0.02f, -0.013f, 0.053f, -0.026f)
                curveToRelative(0.143f, -0.057f, 0.395f, -0.133f, 0.726f, -0.252f)
                curveToRelative(0.662f, -0.237f, 1.648f, -0.654f, 2.903f, -1.533f)
                horizontalLineToRelative(0.002f)
                curveToRelative(0.682f, -0.482f, 0.925f, -1.213f, 0.861f, -1.967f)
                curveToRelative(-0.063f, -0.754f, -0.4f, -1.553f, -0.84f, -2.318f)
                curveToRelative(-0.878f, -1.531f, -2.171f, -2.922f, -2.742f, -3.418f)
                curveToRelative(-0.085f, -0.074f, -0.238f, -0.282f, -0.398f, -0.576f)
                reflectiveCurveToRelative(-0.34f, -0.677f, -0.528f, -1.116f)
                curveToRelative(-0.377f, -0.877f, -0.794f, -1.985f, -1.209f, -3.117f)
                reflectiveCurveToRelative(-0.83f, -2.284f, -1.203f, -3.26f)
                curveToRelative(-0.373f, -0.975f, -0.7f, -1.762f, -0.975f, -2.205f)
                lineToRelative(-0.01f, -0.015f)
                reflectiveCurveTo(33.65f, 6.77f, 32.68f, 6.37f)
                curveToRelative(-0.485f, -0.2f, -1.074f, -0.28f, -1.692f, -0.059f)
                moveToRelative(0.17f, 0.47f)
                curveToRelative(0.489f, -0.175f, 0.932f, -0.114f, 1.332f, 0.051f)
                curveToRelative(0.798f, 0.329f, 1.36f, 1.085f, 1.364f, 1.09f)
                curveToRelative(0.224f, 0.365f, 0.557f, 1.145f, 0.925f, 2.107f)
                curveToRelative(0.37f, 0.968f, 0.783f, 2.12f, 1.2f, 3.254f)
                reflectiveCurveToRelative(0.836f, 2.248f, 1.22f, 3.143f)
                curveToRelative(0.192f, 0.447f, 0.376f, 0.842f, 0.549f, 1.158f)
                reflectiveCurveToRelative(0.326f, 0.554f, 0.508f, 0.713f)
                curveToRelative(0.5f, 0.434f, 1.8f, 1.832f, 2.637f, 3.29f)
                curveToRelative(0.418f, 0.73f, 0.72f, 1.477f, 0.773f, 2.11f)
                curveToRelative(0.053f, 0.634f, -0.108f, 1.133f, -0.65f, 1.516f)
                curveToRelative(-1.216f, 0.851f, -2.148f, 1.245f, -2.784f, 1.472f)
                curveToRelative(-0.317f, 0.114f, -0.559f, 0.184f, -0.744f, 0.258f)
                arcToRelative(0.9f, 0.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.252f, 0.139f)
                arcToRelative(0.42f, 0.42f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.146f, 0.32f)
                curveToRelative(0.01f, 0.17f, 0.088f, 0.291f, 0.162f, 0.395f)
                curveToRelative(0.074f, 0.103f, 0.152f, 0.194f, 0.207f, 0.271f)
                reflectiveCurveToRelative(0.08f, 0.137f, 0.082f, 0.157f)
                reflectiveCurveToRelative(0.016f, 0.016f, -0.043f, 0.072f)
                curveToRelative(-0.03f, 0.029f, -0.16f, 0.09f, -0.334f, 0.097f)
                arcToRelative(1.3f, 1.3f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.584f, -0.117f)
                curveToRelative(-0.4f, -0.182f, -0.769f, -0.565f, -0.8f, -1.388f)
                curveToRelative(-0.034f, -0.85f, 0.2f, -1.285f, 0.343f, -1.895f)
                curveToRelative(0.142f, -0.61f, 0.15f, -1.354f, -0.361f, -2.629f)
                verticalLineToRelative(-0.002f)
                reflectiveCurveToRelative(-0.223f, -0.548f, -0.54f, -1.193f)
                curveToRelative(-0.316f, -0.645f, -0.714f, -1.381f, -1.134f, -1.807f)
                curveToRelative(-0.322f, -0.326f, -0.64f, -0.94f, -0.86f, -1.463f)
                curveToRelative(-0.02f, -0.047f, -0.012f, -0.04f, -0.03f, -0.086f)
                curveToRelative(0.166f, 0.141f, 0.168f, 0.169f, 0.376f, 0.319f)
                curveToRelative(0.307f, 0.22f, 0.643f, 0.43f, 0.979f, 0.56f)
                reflectiveCurveToRelative(0.693f, 0.195f, 1.006f, 0.026f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.095f, -0.35f)
                lineToRelative(-0.93f, -1.549f)
                lineToRelative(-0.005f, -0.01f)
                reflectiveCurveToRelative(-0.328f, -0.492f, -0.678f, -1.039f)
                curveToRelative(-0.175f, -0.273f, -0.354f, -0.56f, -0.502f, -0.804f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.299f, -0.535f)
                curveToRelative(-0.17f, -0.405f, -0.633f, -1.795f, -0.65f, -1.848f)
                lineToRelative(0.006f, -0.006f)
                curveToRelative(-0.002f, -0.01f, -0.016f, -0.02f, -0.018f, -0.03f)
                curveToRelative(-0.208f, -0.956f, -0.96f, -2.071f, -1.67f, -2.986f)
                curveToRelative(-0.633f, -0.816f, -1.096f, -1.299f, -1.226f, -1.437f)
                curveToRelative(0.52f, -0.728f, 1.028f, -1.183f, 1.476f, -1.344f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15.598f, 16.14f)
                arcToRelative(0.44f, 0.44f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.155f, 0.1f)
                curveToRelative(-0.161f, 0.149f, -0.284f, 0.364f, -0.42f, 0.612f)
                curveToRelative(-0.27f, 0.495f, -0.523f, 1.09f, -0.523f, 1.09f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.018f, 0.07f)
                reflectiveCurveToRelative(-0.077f, 0.642f, 0.032f, 1.273f)
                curveToRelative(0.054f, 0.316f, 0.15f, 0.637f, 0.365f, 0.885f)
                reflectiveCurveToRelative(0.575f, 0.385f, 0.984f, 0.295f)
                lineToRelative(0.002f, -0.002f)
                lineToRelative(1.352f, -0.309f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.164f, -0.36f)
                reflectiveCurveToRelative(-0.317f, -0.607f, -0.643f, -1.323f)
                curveToRelative(-0.325f, -0.717f, -0.652f, -1.576f, -0.68f, -1.92f)
                arcToRelative(0.7f, 0.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.036f, -0.196f)
                arcToRelative(0.34f, 0.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.149f, -0.191f)
                arcToRelative(0.34f, 0.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.275f, -0.023f)
                moveToRelative(0.043f, 0.682f)
                curveToRelative(0.095f, 0.534f, 0.347f, 1.206f, 0.642f, 1.856f)
                curveToRelative(0.261f, 0.574f, 0.416f, 0.868f, 0.514f, 1.058f)
                lineToRelative(-1.041f, 0.24f)
                curveToRelative(-0.271f, 0.06f, -0.385f, -0.001f, -0.5f, -0.134f)
                reflectiveCurveToRelative(-0.204f, -0.374f, -0.25f, -0.64f)
                curveToRelative(-0.09f, -0.522f, -0.03f, -1.079f, -0.027f, -1.106f)
                curveToRelative(0.019f, -0.045f, 0.242f, -0.56f, 0.484f, -1.004f)
                curveToRelative(0.07f, -0.129f, 0.116f, -0.18f, 0.178f, -0.27f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(32.133f, 16.201f)
                arcToRelative(0.34f, 0.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.276f, 0.022f)
                arcToRelative(0.35f, 0.35f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.148f, 0.193f)
                arcToRelative(0.7f, 0.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.04f, 0.195f)
                curveToRelative(-0.026f, 0.344f, -0.353f, 1.204f, -0.679f, 1.92f)
                arcToRelative(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.642f, 1.323f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.166f, 0.359f)
                lineToRelative(1.35f, 0.31f)
                horizontalLineToRelative(0.001f)
                curveToRelative(0.41f, 0.09f, 0.772f, -0.045f, 0.987f, -0.293f)
                curveToRelative(0.214f, -0.247f, 0.31f, -0.569f, 0.365f, -0.884f)
                curveToRelative(0.108f, -0.631f, 0.031f, -1.274f, 0.031f, -1.274f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.017f, -0.07f)
                reflectiveCurveToRelative(-0.256f, -0.595f, -0.526f, -1.09f)
                curveToRelative(-0.135f, -0.247f, -0.256f, -0.463f, -0.418f, -0.611f)
                arcToRelative(0.44f, 0.44f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.154f, -0.1f)
                moveToRelative(-0.043f, 0.68f)
                curveToRelative(0.062f, 0.09f, 0.107f, 0.14f, 0.178f, 0.27f)
                curveToRelative(0.243f, 0.445f, 0.466f, 0.969f, 0.484f, 1.01f)
                curveToRelative(0.003f, 0.03f, 0.06f, 0.58f, -0.03f, 1.099f)
                curveToRelative(-0.045f, 0.267f, -0.134f, 0.51f, -0.25f, 0.642f)
                curveToRelative(-0.115f, 0.133f, -0.226f, 0.193f, -0.497f, 0.133f)
                lineToRelative(0.002f, 0.002f)
                lineToRelative(-1.045f, -0.24f)
                curveToRelative(0.097f, -0.19f, 0.254f, -0.486f, 0.515f, -1.06f)
                curveToRelative(0.296f, -0.65f, 0.548f, -1.322f, 0.643f, -1.856f)
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18.305f, 7.201f)
                curveToRelative(-1.133f, 0.354f, -1.942f, 1.316f, -2.48f, 2.17f)
                curveToRelative(-0.54f, 0.854f, -0.811f, 1.627f, -0.811f, 1.627f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.154f, 0.318f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.318f, -0.154f)
                reflectiveCurveToRelative(0.254f, -0.722f, 0.76f, -1.525f)
                curveToRelative(0.507f, -0.804f, 1.262f, -1.663f, 2.21f, -1.96f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.163f, -0.312f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.314f, -0.164f)
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(29.695f, 7.121f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.314f, 0.164f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.164f, 0.313f)
                curveToRelative(0.897f, 0.285f, 1.614f, 1.113f, 2.096f, 1.886f)
                curveToRelative(0.481f, 0.774f, 0.722f, 1.467f, 0.722f, 1.467f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.319f, 0.154f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.154f, -0.318f)
                reflectiveCurveToRelative(-0.258f, -0.744f, -0.772f, -1.568f)
                curveToRelative(-0.513f, -0.824f, -1.285f, -1.753f, -2.369f, -2.098f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20.5f, 15.23f)
                curveToRelative(-0.665f, 0f, -1.21f, 0.544f, -1.21f, 1.21f)
                reflectiveCurveToRelative(0.545f, 1.21f, 1.21f, 1.21f)
                reflectiveCurveToRelative(1.21f, -0.545f, 1.21f, -1.21f)
                curveToRelative(0f, -0.666f, -0.545f, -1.21f, -1.21f, -1.21f)
                moveToRelative(0f, 0.5f)
                curveToRelative(0.395f, 0f, 0.71f, 0.314f, 0.71f, 0.71f)
                reflectiveCurveToRelative(-0.315f, 0.71f, -0.71f, 0.71f)
                arcToRelative(0.71f, 0.71f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.71f, -0.71f)
                curveToRelative(0f, -0.396f, 0.315f, -0.71f, 0.71f, -0.71f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(27.61f, 15.23f)
                curveToRelative(-0.666f, 0f, -1.21f, 0.544f, -1.21f, 1.21f)
                reflectiveCurveToRelative(0.544f, 1.21f, 1.21f, 1.21f)
                reflectiveCurveToRelative(1.21f, -0.545f, 1.21f, -1.21f)
                curveToRelative(0f, -0.666f, -0.545f, -1.21f, -1.21f, -1.21f)
                moveToRelative(0f, 0.5f)
                curveToRelative(0.394f, 0f, 0.71f, 0.314f, 0.71f, 0.71f)
                reflectiveCurveToRelative(-0.316f, 0.71f, -0.71f, 0.71f)
                arcToRelative(0.706f, 0.706f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.71f, -0.71f)
                curveToRelative(0f, -0.396f, 0.314f, -0.71f, 0.71f, -0.71f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(22.37f, 18.87f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, 0.15f)
                curveToRelative(0f, 0.521f, 0.428f, 0.95f, 0.95f, 0.95f)
                reflectiveCurveToRelative(0.95f, -0.429f, 0.95f, -0.95f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, -0.15f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, 0.15f)
                curveToRelative(0f, 0.358f, -0.292f, 0.65f, -0.65f, 0.65f)
                arcToRelative(0.65f, 0.65f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.65f, -0.65f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, -0.15f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23.97f, 18.87f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, 0.15f)
                curveToRelative(0f, 0.521f, 0.428f, 0.95f, 0.95f, 0.95f)
                curveToRelative(0.521f, 0f, 0.95f, -0.429f, 0.95f, -0.95f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, -0.15f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, 0.15f)
                curveToRelative(0f, 0.358f, -0.292f, 0.65f, -0.65f, 0.65f)
                arcToRelative(0.65f, 0.65f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.65f, -0.65f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, -0.15f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.977f, 16.55f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.237f, 0.264f)
                reflectiveCurveToRelative(0.07f, 1.313f, 0.89f, 2.624f)
                curveToRelative(0.82f, 1.31f, 2.422f, 2.623f, 5.35f, 2.623f)
                curveToRelative(2.924f, 0f, 4.599f, -1.22f, 5.506f, -2.442f)
                curveToRelative(0.908f, -1.222f, 1.073f, -2.457f, 1.073f, -2.457f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.215f, -0.28f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.282f, 0.216f)
                reflectiveCurveToRelative(-0.146f, 1.105f, -0.976f, 2.222f)
                curveToRelative(-0.83f, 1.118f, -2.319f, 2.24f, -5.106f, 2.24f)
                curveToRelative(-2.781f, 0f, -4.176f, -1.193f, -4.923f, -2.388f)
                curveToRelative(-0.748f, -1.195f, -0.817f, -2.387f, -0.817f, -2.387f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.263f, -0.234f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21.143f, 21.225f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.262f, 0.38f)
                lineToRelative(1.42f, 2.2f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.398f, 0.029f)
                lineToRelative(1.381f, -1.602f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.139f, -0.406f)
                close()
                moveTo(21.629f, 21.842f)
                lineTo(23.426f, 22.227f)
                lineTo(22.539f, 23.254f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(27.463f, 21.047f)
                lineToRelative(-3.07f, 0.73f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.125f, 0.414f)
                lineToRelative(1.58f, 1.67f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.394f, -0.039f)
                lineToRelative(1.49f, -2.4f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.27f, -0.375f)
                moveToRelative(-0.477f, 0.627f)
                lineToRelative(-0.998f, 1.61f)
                lineToRelative(-1.058f, -1.12f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(27.553f, 20.959f)
                reflectiveCurveToRelative(-0.658f, 0.2f, -1.358f, 0.4f)
                curveToRelative(-0.35f, 0.1f, -0.709f, 0.2f, -1.002f, 0.276f)
                curveToRelative(-0.146f, 0.038f, -0.275f, 0.07f, -0.377f, 0.092f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.191f, 0.033f)
                curveToRelative(-0.012f, 0f, -0.102f, -0.005f, -0.215f, -0.02f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.424f, -0.064f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.297f, -0.434f)
                quadToRelative(-0.242f, -0.05f, -0.4f, -0.088f)
                curveToRelative(-0.104f, -0.024f, -0.187f, -0.051f, -0.185f, -0.05f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.272f, 0.214f)
                lineToRelative(0.39f, 2.674f)
                lineToRelative(-1.878f, 3.463f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.056f, 0.254f)
                lineToRelative(3.33f, 2.51f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.23f, 0.008f)
                lineToRelative(1.185f, -0.797f)
                lineToRelative(0.882f, 0.9f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.252f, 0.027f)
                lineToRelative(3.871f, -2.529f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.069f, -0.256f)
                lineTo(27.357f, 23.8f)
                lineToRelative(0.45f, -2.615f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.254f, -0.225f)
                moveToRelative(-0.196f, 0.475f)
                lineToRelative(-0.404f, 2.363f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.018f, 0.121f)
                lineToRelative(1.81f, 3.672f)
                lineToRelative(-3.584f, 2.344f)
                lineToRelative(-0.884f, -0.905f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.254f, -0.025f)
                lineToRelative(-1.202f, 0.81f)
                lineToRelative(-3.08f, -2.32f)
                lineToRelative(1.828f, -3.369f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.022f, -0.123f)
                lineToRelative(-0.356f, -2.441f)
                curveToRelative(0.099f, 0.022f, 0.204f, 0.046f, 0.336f, 0.074f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.315f, 0.435f)
                curveToRelative(0.168f, 0.028f, 0.318f, 0.051f, 0.437f, 0.067f)
                curveToRelative(0.12f, 0.015f, 0.2f, 0.025f, 0.276f, 0.023f)
                curveToRelative(0.078f, -0.002f, 0.156f, -0.02f, 0.265f, -0.043f)
                curveToRelative(0.11f, -0.024f, 0.244f, -0.055f, 0.393f, -0.094f)
                curveToRelative(0.299f, -0.077f, 0.662f, -0.178f, 1.014f, -0.279f)
                curveToRelative(0.542f, -0.156f, 0.843f, -0.248f, 1.05f, -0.31f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(20.766f, 21.33f)
                lineToRelative(-1.659f, 1.711f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.002f, 0.277f)
                lineToRelative(1.49f, 1.551f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.307f, -0.021f)
                lineToRelative(0.65f, -0.9f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.034f, -0.157f)
                lineToRelative(-0.48f, -2.361f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.34f, -0.1f)
                moveToRelative(0.023f, 0.55f)
                lineToRelative(0.387f, 1.903f)
                lineToRelative(-0.457f, 0.635f)
                lineToRelative(-1.192f, -1.236f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(19.3f, 23.36f)
                lineToRelative(-0.25f, -0.23f)
                lineToRelative(-0.34f, 0.42f)
                lineToRelative(1.84f, 1.98f)
                lineToRelative(0.36f, -0.6f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.186f, 22.982f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.291f, 0.022f)
                lineToRelative(-0.34f, 0.42f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.01f, 0.262f)
                lineToRelative(1.84f, 1.98f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.316f, -0.033f)
                lineToRelative(0.361f, -0.6f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.033f, -0.246f)
                lineToRelative(-1.61f, -1.57f)
                lineToRelative(-0.003f, -0.004f)
                close()
                moveTo(19.07f, 23.422f)
                lineTo(19.164f, 23.508f)
                lineTo(20.656f, 24.963f)
                lineTo(20.516f, 25.199f)
                lineTo(18.975f, 23.541f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(18.576f, 23.354f)
                lineToRelative(-2.67f, 2.119f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.015f, 0.3f)
                lineToRelative(2.82f, 2.74f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.31f, -0.04f)
                lineToRelative(1.7f, -2.84f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.024f, -0.238f)
                lineToRelative(-1.85f, -2.02f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.27f, -0.021f)
                moveToRelative(0.102f, 0.427f)
                lineToRelative(1.625f, 1.774f)
                lineToRelative(-1.494f, 2.494f)
                lineToRelative(-2.475f, -2.404f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(16.03f, 25.63f)
                lineToRelative(-0.43f, 0.35f)
                lineToRelative(3.02f, 2.88f)
                lineToRelative(0.23f, -0.49f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(15.904f, 25.475f)
                lineToRelative(-0.43f, 0.35f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.011f, 0.3f)
                lineToRelative(3.02f, 2.879f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.318f, -0.059f)
                lineToRelative(0.23f, -0.49f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.04f, -0.228f)
                lineToRelative(-2.821f, -2.74f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.266f, -0.012f)
                moveToRelative(0.114f, 0.421f)
                lineToRelative(2.59f, 2.518f)
                lineToRelative(-0.051f, 0.11f)
                lineToRelative(-2.655f, -2.532f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(32.164f, 26.313f)
                lineToRelative(-1.74f, 1.61f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.006f, 0.288f)
                lineToRelative(0.004f, 0.004f)
                curveToRelative(0.241f, 0.263f, 0.521f, 0.448f, 0.797f, 0.535f)
                curveToRelative(0.275f, 0.087f, 0.572f, 0.076f, 0.777f, -0.113f)
                lineToRelative(0.76f, -0.711f)
                curveToRelative(0.206f, -0.191f, 0.233f, -0.487f, 0.164f, -0.768f)
                arcToRelative(1.96f, 1.96f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.473f, -0.834f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.283f, -0.012f)
                moveToRelative(0.092f, 0.46f)
                curveToRelative(0.118f, 0.166f, 0.24f, 0.337f, 0.275f, 0.48f)
                curveToRelative(0.049f, 0.198f, 0.017f, 0.32f, -0.047f, 0.38f)
                verticalLineToRelative(0.002f)
                lineToRelative(-0.761f, 0.709f)
                curveToRelative(-0.066f, 0.06f, -0.192f, 0.083f, -0.383f, 0.023f)
                curveToRelative(-0.144f, -0.045f, -0.31f, -0.181f, -0.467f, -0.316f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(27.91f, 21.066f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.344f, 0.084f)
                lineToRelative(-0.609f, 2.381f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.031f, 0.166f)
                lineToRelative(0.65f, 0.9f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.305f, 0.022f)
                lineToRelative(1.5f, -1.55f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.006f, -0.272f)
                close()
                moveTo(27.861f, 21.611f)
                lineTo(29.027f, 22.924f)
                lineTo(27.822f, 24.17f)
                lineTo(27.367f, 23.54f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(29.25f, 23.07f)
                lineToRelative(0.24f, -0.23f)
                lineToRelative(0.34f, 0.42f)
                lineTo(28f, 25.23f)
                lineToRelative(-0.36f, -0.59f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(29.352f, 22.695f)
                lineToRelative(-0.24f, 0.23f)
                verticalLineToRelative(0.003f)
                lineTo(27.5f, 24.496f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.031f, 0.248f)
                lineToRelative(0.361f, 0.59f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.316f, 0.031f)
                lineToRelative(1.83f, -1.969f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.01f, -0.261f)
                lineToRelative(-0.341f, -0.42f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.293f, -0.02f)
                moveToRelative(0.119f, 0.442f)
                lineToRelative(0.093f, 0.115f)
                lineToRelative(-1.53f, 1.648f)
                lineToRelative(-0.14f, -0.228f)
                lineToRelative(1.495f, -1.46f)
                verticalLineToRelative(0.003f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(29.965f, 23.063f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.272f, 0.021f)
                lineToRelative(-1.841f, 2.012f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.028f, 0.23f)
                lineToRelative(1.65f, 3.04f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.319f, 0.044f)
                lineToRelative(2.86f, -2.93f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.018f, -0.296f)
                close()
                moveTo(29.861f, 23.494f)
                lineTo(32.213f, 25.359f)
                lineTo(29.697f, 27.937f)
                lineTo(28.244f, 25.26f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(32.51f, 25.34f)
                lineToRelative(0.43f, 0.35f)
                lineToRelative(-3.02f, 2.87f)
                lineToRelative(-0.23f, -0.48f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(32.637f, 25.186f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.266f, 0.011f)
                lineToRelative(-2.82f, 2.74f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.041f, 0.229f)
                lineToRelative(0.23f, 0.48f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.319f, 0.06f)
                lineToRelative(3.02f, -2.87f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.013f, -0.3f)
                close()
                moveTo(32.521f, 25.607f)
                lineTo(32.637f, 25.701f)
                lineTo(29.98f, 28.225f)
                lineTo(29.932f, 28.123f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(24.428f, 21.771f)
                lineToRelative(-0.56f, 0.05f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.169f, 0.087f)
                lineToRelative(-0.719f, 0.84f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.017f, 0.303f)
                lineToRelative(0.521f, 0.76f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.2f, 0.109f)
                lineToRelative(0.921f, 0.02f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.194f, -0.086f)
                lineToRelative(0.68f, -0.78f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.006f, -0.336f)
                lineToRelative(-0.842f, -0.89f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.203f, -0.077f)
                moveToRelative(-0.076f, 0.508f)
                lineToRelative(0.601f, 0.637f)
                lineToRelative(-0.455f, 0.521f)
                lineToRelative(-0.674f, -0.015f)
                lineToRelative(-0.34f, -0.496f)
                lineToRelative(0.53f, -0.615f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(24.707f, 23.51f)
                lineToRelative(-0.91f, 0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.238f, 0.185f)
                lineToRelative(-1.07f, 4.031f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.07f, 0.245f)
                lineToRelative(1.439f, 1.37f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.346f, 0f)
                lineToRelative(1.388f, -1.33f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.073f, -0.232f)
                lineToRelative(-0.85f, -4.08f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.248f, -0.2f)
                moveToRelative(-0.2f, 0.502f)
                lineToRelative(0.78f, 3.744f)
                lineToRelative(-1.117f, 1.068f)
                lineToRelative(-1.16f, -1.103f)
                lineToRelative(0.982f, -3.703f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.414f, 26.572f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.281f, 0.012f)
                curveToRelative(-0.24f, 0.262f, -0.403f, 0.555f, -0.469f, 0.836f)
                reflectiveCurveToRelative(-0.035f, 0.578f, 0.17f, 0.768f)
                horizontalLineToRelative(0.002f)
                lineToRelative(0.768f, 0.699f)
                curveToRelative(0.205f, 0.19f, 0.504f, 0.197f, 0.779f, 0.107f)
                arcToRelative(1.9f, 1.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.795f, -0.539f)
                arcToRelative(0.2f, 0.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.014f, -0.283f)
                close()
                moveTo(16.326f, 27.033f)
                lineTo(17.713f, 28.301f)
                curveToRelative(-0.154f, 0.13f, -0.313f, 0.267f, -0.453f, 0.312f)
                curveToRelative(-0.193f, 0.063f, -0.32f, 0.04f, -0.385f, -0.02f)
                verticalLineToRelative(-0.001f)
                lineToRelative(-0.77f, -0.7f)
                curveToRelative(-0.064f, -0.06f, -0.099f, -0.184f, -0.052f, -0.38f)
                curveToRelative(0.033f, -0.144f, 0.156f, -0.314f, 0.273f, -0.479f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.672f, 27.455f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.354f, 0.072f)
                lineToRelative(-2.37f, 3.82f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.167f, 0.38f)
                lineToRelative(2.192f, 0.388f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.279f, -0.164f)
                lineToRelative(1.201f, -3.5f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.094f, -0.287f)
                close()
                moveTo(19.604f, 28.015f)
                lineTo(20.252f, 28.467f)
                lineTo(19.184f, 31.586f)
                lineTo(17.566f, 31.299f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20.697f, 28.168f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.384f, 0.123f)
                lineToRelative(-1.5f, 4.51f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.218f, 0.328f)
                lineToRelative(2.908f, 0.23f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.268f, -0.21f)
                lineToRelative(0.53f, -3.319f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.1f, -0.242f)
                close()
                moveTo(20.68f, 28.775f)
                lineTo(22.219f, 29.902f)
                lineTo(21.75f, 32.842f)
                lineTo(19.389f, 32.656f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(24.03f, 28.963f)
                lineToRelative(-1.17f, 0.799f)
                lineToRelative(-0.217f, -0.17f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.4f, 0.172f)
                lineToRelative(-0.442f, 4.109f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.277f)
                horizontalLineToRelative(4.119f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.248f, -0.279f)
                lineToRelative(-0.469f, -4.06f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.387f, -0.18f)
                lineToRelative(-0.357f, 0.236f)
                lineToRelative(-0.857f, -0.873f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.319f, -0.031f)
                moveToRelative(0.109f, 0.531f)
                lineToRelative(0.853f, 0.871f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.317f, 0.033f)
                lineToRelative(0.189f, -0.125f)
                lineToRelative(0.39f, 3.377f)
                horizontalLineToRelative(-3.56f)
                lineToRelative(0.363f, -3.388f)
                lineToRelative(0.006f, 0.006f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.293f, 0.01f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(27.674f, 28.25f)
                lineToRelative(-2.111f, 1.38f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.11f, 0.245f)
                lineToRelative(0.469f, 3.271f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.27f, 0.213f)
                lineToRelative(3.13f, -0.27f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.215f, -0.33f)
                lineToRelative(-1.49f, -4.38f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.373f, -0.129f)
                moveToRelative(0.004f, 0.596f)
                lineToRelative(1.283f, 3.773f)
                lineToRelative(-2.576f, 0.223f)
                lineToRelative(-0.414f, -2.88f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(28.89f, 27.34f)
                lineToRelative(-1.23f, 0.92f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.082f, 0.295f)
                lineToRelative(1.41f, 3.41f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.295f, 0.146f)
                lineToRelative(1.98f, -0.52f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.155f, -0.364f)
                lineToRelative(-2.16f, -3.811f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.367f, -0.076f)
                moveToRelative(0.07f, 0.57f)
                lineToRelative(1.862f, 3.28f)
                lineToRelative(-1.455f, 0.382f)
                lineToRelative(-1.252f, -3.029f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.549f, 32.67f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.254f, 0.334f)
                lineToRelative(2.32f, 6.51f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.239f, 0.166f)
                lineToRelative(2.261f, -0.04f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.244f, -0.247f)
                lineToRelative(0.041f, -5.532f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.246f, -0.252f)
                lineToRelative(-1.883f, -0.035f)
                lineToRelative(-0.062f, -0.506f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.23f, -0.216f)
                close()
                moveTo(19.895f, 33.197f)
                lineTo(21.738f, 33.334f)
                lineTo(21.803f, 33.852f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.242f, 0.218f)
                lineToRelative(1.853f, 0.035f)
                lineToRelative(-0.037f, 5.04f)
                lineToRelative(-1.836f, 0.033f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(28.922f, 32.67f)
                lineToRelative(-2.44f, 0.182f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.23f, 0.216f)
                lineToRelative(-0.063f, 0.506f)
                lineToRelative(-1.744f, 0.035f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.246f, 0.246f)
                lineToRelative(-0.09f, 5.532f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.246f, 0.254f)
                lineToRelative(2.26f, 0.039f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.24f, -0.166f)
                lineToRelative(2.32f, -6.51f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.253f, -0.334f)
                moveToRelative(-0.346f, 0.527f)
                lineToRelative(-2.13f, 5.98f)
                lineToRelative(-1.833f, -0.032f)
                lineToRelative(0.082f, -5.04f)
                lineToRelative(1.721f, -0.035f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.242f, -0.218f)
                lineToRelative(0.065f, -0.518f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveToRelative(20.145f, 34.865f)
                lineToRelative(1.617f, 4.69f)
                horizontalLineToRelative(4.947f)
                lineToRelative(1.668f, -4.69f)
                horizontalLineToRelative(-4.406f)
                close()
                moveTo(20.495f, 35.115f)
                horizontalLineToRelative(7.526f)
                lineToRelative(-1.49f, 4.19f)
                lineTo(21.94f, 39.305f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(11.77f, 42.98f)
                verticalLineToRelative(3.58f)
                horizontalLineToRelative(24.92f)
                verticalLineToRelative(-3.58f)
                close()
                moveTo(12.17f, 43.38f)
                horizontalLineToRelative(24.12f)
                verticalLineToRelative(2.78f)
                lineTo(12.17f, 46.16f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.646f, 3.56f)
                lineTo(2.645f, 23.24f)
                lineTo(8.9f, 31.49f)
                horizontalLineToRelative(5.676f)
                lineToRelative(3.764f, 9.219f)
                verticalLineToRelative(2.672f)
                horizontalLineToRelative(11.91f)
                verticalLineToRelative(-2.678f)
                lineToRelative(3.123f, -8.963f)
                horizontalLineToRelative(5.25f)
                lineToRelative(6.492f, -7.668f)
                lineToRelative(-9.357f, -20.511f)
                horizontalLineToRelative(-4.612f)
                lineTo(27.98f, 5.37f)
                lineToRelative(-3.525f, -1.23f)
                lineToRelative(-3.879f, 1.113f)
                lineToRelative(-2.994f, -1.693f)
                close()
                moveTo(12.893f, 3.96f)
                horizontalLineToRelative(4.584f)
                lineToRelative(3.048f, 1.726f)
                lineToRelative(3.918f, -1.125f)
                lineToRelative(3.577f, 1.248f)
                lineToRelative(3.234f, -1.848f)
                lineTo(35.5f, 3.961f)
                lineToRelative(9.145f, 20.047f)
                lineToRelative(-6.208f, 7.332f)
                horizontalLineToRelative(-5.35f)
                lineToRelative(-3.237f, 9.297f)
                verticalLineToRelative(2.343f)
                lineTo(18.74f, 42.98f)
                verticalLineToRelative(-2.35f)
                lineToRelative(-3.896f, -9.54f)
                lineTo(9.1f, 31.09f)
                lineTo(3.115f, 23.2f)
                close()
            }
        }.build()

        return _Standees!!
    }

@Suppress("ObjectPropertyName")
private var _Standees: ImageVector? = null
