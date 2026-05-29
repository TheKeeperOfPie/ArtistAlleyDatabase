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
val MerchIcons.Shirts: ImageVector
    get() {
        if (_Shirts != null) {
            return _Shirts!!
        }
        _Shirts = ImageVector.Builder(
            name = "Shirts",
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
                moveTo(24.021f, 19.158f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.324f, 0.14f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.14f, 0.325f)
                curveToRelative(0.8f, 0.315f, 1.404f, 1.47f, 1.413f, 2.37f)
                curveToRelative(0.01f, 0.92f, -0.43f, 1.839f, -1.059f, 2.564f)
                horizontalLineToRelative(-0.002f)
                curveToRelative(-0.638f, 0.746f, -1.395f, 1.474f, -2.193f, 2.039f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.06f, 0.35f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.35f, 0.058f)
                curveToRelative(0.84f, -0.595f, 1.621f, -1.347f, 2.282f, -2.12f)
                curveToRelative(0.69f, -0.794f, 1.193f, -1.816f, 1.182f, -2.896f)
                curveToRelative(-0.011f, -1.101f, -0.648f, -2.404f, -1.729f, -2.83f)
                moveTo(7.629f, 8.227f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.344f, 0.084f)
                curveToRelative(-2.27f, 3.778f, -4.428f, 7.298f, -6.7f, 11.08f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.022f, 0.289f)
                arcToRelative(28.2f, 28.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.516f, 6.89f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.338f, -0.103f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.106f, -0.338f)
                arcToRelative(27.7f, 27.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8.24f, -6.649f)
                curveToRelative(2.232f, -3.713f, 4.363f, -7.188f, 6.6f, -10.912f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.086f, -0.341f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(11.678f, 10.576f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.301f, 0.186f)
                arcToRelative(95.5f, 95.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.715f, 35.572f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.283f, 0.213f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.213f, -0.281f)
                arcToRelative(95f, 95f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.705f, -35.387f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.185f, -0.303f)
                moveTo(15.432f, 1.686f)
                curveToRelative(-2.59f, 1.56f, -5.192f, 3.13f, -7.782f, 4.689f)
                verticalLineToRelative(0.002f)
                arcToRelative(2.57f, 2.57f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.175f, 2.629f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.29f, 0.2f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.2f, -0.292f)
                curveToRelative(-0.15f, -0.79f, 0.25f, -1.69f, 0.945f, -2.111f)
                curveToRelative(2.59f, -1.56f, 5.19f, -3.13f, 7.78f, -4.69f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.083f, -0.342f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.341f, -0.085f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7.96f, 7.91f)
                curveToRelative(-0.548f, 0.06f, -1.074f, 0.346f, -1.366f, 0.842f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.09f, 0.344f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.341f, -0.088f)
                curveToRelative(0.395f, -0.669f, 1.56f, -0.791f, 2.082f, -0.229f)
                verticalLineToRelative(0.002f)
                arcToRelative(24.76f, 24.76f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.457f, 7.405f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.291f, -0.2f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.199f, -0.293f)
                arcTo(24.27f, 24.27f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.473f, 8.44f)
                curveToRelative(-0.39f, -0.418f, -0.963f, -0.588f, -1.512f, -0.529f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15.414f, 1.861f)
                curveToRelative(-0.742f, 0.73f, -0.874f, 1.958f, -0.303f, 2.827f)
                curveToRelative(2.44f, 3.77f, 4.89f, 7.53f, 7.328f, 11.298f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.346f, 0.075f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.074f, -0.346f)
                curveToRelative(-2.44f, -3.77f, -4.89f, -7.531f, -7.33f, -11.3f)
                verticalLineToRelative(-0.003f)
                curveToRelative(-0.428f, -0.651f, -0.321f, -1.644f, 0.237f, -2.193f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.002f, -0.354f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.354f, -0.004f)
                moveTo(39.996f, 8.12f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.078f, 0.343f)
                curveToRelative(2.353f, 3.721f, 4.706f, 7.443f, 7.049f, 11.164f)
                curveToRelative(-2.406f, 2.703f, -5.514f, 4.848f, -8.838f, 6.51f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.113f, 0.336f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.336f, 0.111f)
                curveToRelative(3.42f, -1.71f, 6.648f, -3.917f, 9.127f, -6.75f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.023f, -0.297f)
                curveToRelative(-2.38f, -3.78f, -4.77f, -7.56f, -7.16f, -11.34f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.346f, -0.078f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(35.668f, 10.578f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.18f, 0.303f)
                arcToRelative(90.7f, 90.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.805f, 35.392f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.21f, 0.284f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.284f, -0.211f)
                arcToRelative(91.2f, 91.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.814f, -35.588f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.305f, -0.18f)
                moveTo(31.703f, 1.684f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.34f, 0.091f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.092f, 0.342f)
                curveToRelative(2.73f, 1.56f, 5.47f, 3.13f, 8.2f, 4.69f)
                curveToRelative(0.736f, 0.426f, 1.147f, 1.308f, 0.99f, 2.093f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.197f, 0.295f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.293f, -0.195f)
                curveToRelative(0.203f, -1.015f, -0.308f, -2.093f, -1.23f, -2.627f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(39.584f, 7.918f)
                curveToRelative(-0.566f, -0.06f, -1.167f, 0.095f, -1.582f, 0.516f)
                verticalLineToRelative(0.002f)
                curveToRelative(-3.651f, 3.73f, -8.608f, 6.316f, -13.897f, 7.257f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.2f, 0.291f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.288f, 0.202f)
                curveToRelative(5.391f, -0.96f, 10.436f, -3.592f, 14.164f, -7.4f)
                curveToRelative(0.286f, -0.29f, 0.735f, -0.416f, 1.174f, -0.37f)
                curveToRelative(0.44f, 0.046f, 0.847f, 0.262f, 1.057f, 0.598f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.346f, 0.078f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.078f, -0.344f)
                curveToRelative(-0.31f, -0.495f, -0.862f, -0.77f, -1.428f, -0.83f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(31.72f, 1.857f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.353f, 0.012f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.012f, 0.354f)
                curveToRelative(0.586f, 0.552f, 0.699f, 1.535f, 0.254f, 2.185f)
                verticalLineToRelative(0.002f)
                curveToRelative(-2.57f, 3.77f, -5.149f, 7.529f, -7.719f, 11.299f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.065f, 0.348f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.347f, -0.067f)
                curveToRelative(2.57f, -3.77f, 5.15f, -7.528f, 7.72f, -11.299f)
                curveToRelative(0.596f, -0.87f, 0.448f, -2.106f, -0.325f, -2.834f)
                moveTo(8.375f, 6.914f)
                curveToRelative(-0.45f, -0.03f, -0.907f, 0.019f, -1.355f, 0.17f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.157f, 0.316f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.317f, 0.157f)
                curveToRelative(1.525f, -0.515f, 3.263f, 0.38f, 4.463f, 1.58f)
                curveToRelative(2.982f, 2.972f, 6.505f, 4.356f, 10.025f, 5.736f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.324f, -0.143f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.14f, -0.322f)
                curveToRelative(-3.52f, -1.38f, -6.958f, -2.737f, -9.856f, -5.625f)
                curveToRelative(-0.96f, -0.96f, -2.27f, -1.776f, -3.621f, -1.869f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(39.275f, 6.521f)
                arcToRelative(4.7f, 4.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.101f, 0.346f)
                curveToRelative(-0.74f, 0.323f, -1.442f, 0.774f, -1.819f, 1.145f)
                lineToRelative(-0.002f, 0.002f)
                curveToRelative(-3.69f, 3.67f, -6.811f, 4.991f, -11.453f, 6.558f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.158f, 0.319f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.319f, 0.156f)
                curveToRelative(4.657f, -1.573f, 7.896f, -2.952f, 11.644f, -6.68f)
                curveToRelative(0.293f, -0.289f, 0.981f, -0.741f, 1.668f, -1.04f)
                curveToRelative(0.343f, -0.15f, 0.69f, -0.264f, 0.982f, -0.311f)
                reflectiveCurveToRelative(0.524f, -0.022f, 0.65f, 0.05f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.343f, -0.091f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.094f, -0.342f)
                curveToRelative(-0.283f, -0.163f, -0.624f, -0.169f, -0.979f, -0.112f)
                moveTo(22.55f, 15.654f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.316f, 0.155f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.155f, 0.318f)
                arcToRelative(2.98f, 2.98f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.877f, 0.021f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.162f, -0.314f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.315f, -0.162f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.562f, -0.018f)
                moveTo(31.549f, 1.652f)
                curveToRelative(-5.167f, 0.659f, -10.854f, 0.964f, -15.942f, 0.002f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.293f, 0.2f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.2f, 0.292f)
                curveToRelative(5.173f, 0.979f, 10.904f, 0.664f, 16.097f, 0.002f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.217f, -0.279f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.28f, -0.217f)
                moveTo(21.902f, 42.477f)
                curveToRelative(-1.415f, 0f, -2.88f, 0.366f, -4.173f, 0.88f)
                curveToRelative(-2.564f, 1.02f, -5.116f, 2.47f, -7.77f, 2.694f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.229f, 0.27f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.272f, 0.228f)
                curveToRelative(2.806f, -0.237f, 5.394f, -1.726f, 7.91f, -2.727f)
                curveToRelative(2.495f, -0.992f, 5.575f, -1.359f, 7.535f, 0.336f)
                curveToRelative(3.272f, 2.832f, 8.045f, 3.717f, 12.17f, 2.38f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.16f, -0.315f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.314f, -0.16f)
                curveToRelative(-3.955f, 1.282f, -8.561f, 0.426f, -11.69f, -2.282f)
                curveToRelative(-1.09f, -0.942f, -2.455f, -1.306f, -3.87f, -1.304f)
                moveTo(18.674f, 14.834f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.121f, 0.332f)
                arcToRelative(7.1f, 7.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.717f, 3.027f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.343f, -0.084f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.082f, -0.341f)
                arcToRelative(6.6f, 6.6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.525f, -2.813f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.332f, -0.121f)
                moveTo(29.023f, 14.748f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.335f, 0.107f)
                arcToRelative(6.46f, 6.46f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.551f, 2.657f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.094f, 0.342f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.34f, 0.093f)
                arcToRelative(6.96f, 6.96f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.75f, -2.863f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.11f, -0.336f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(25.15f, 15.75f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.17f, 0.31f)
                curveToRelative(0.116f, 0.395f, 0.346f, 0.704f, 0.532f, 0.987f)
                curveToRelative(0.183f, 0.28f, 0.334f, 0.564f, 0.35f, 0.838f)
                curveToRelative(0.014f, 0.259f, -0.12f, 0.528f, -0.317f, 0.6f)
                lineToRelative(-1.56f, 0.57f)
                curveToRelative(-0.668f, 0.238f, -1.51f, -0.02f, -1.909f, -0.598f)
                curveToRelative(-0.402f, -0.581f, -0.366f, -1.458f, 0.094f, -1.994f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.027f, -0.354f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.352f, 0.028f)
                curveToRelative(-0.62f, 0.723f, -0.665f, 1.826f, -0.127f, 2.605f)
                curveToRelative(0.54f, 0.782f, 1.598f, 1.104f, 2.49f, 0.783f)
                horizontalLineToRelative(0.002f)
                lineToRelative(1.559f, -0.57f)
                curveToRelative(0.463f, -0.168f, 0.67f, -0.659f, 0.644f, -1.1f)
                curveToRelative(-0.024f, -0.426f, -0.233f, -0.782f, -0.43f, -1.082f)
                curveToRelative(-0.194f, -0.296f, -0.386f, -0.567f, -0.47f, -0.853f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.309f, -0.17f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23.553f, 17.12f)
                curveToRelative(-0.4f, 0.167f, -0.684f, 0.55f, -0.723f, 0.987f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.229f, 0.272f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.27f, -0.227f)
                arcToRelative(0.72f, 0.72f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.417f, -0.572f)
                arcToRelative(0.75f, 0.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.707f, 0.104f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.352f, -0.038f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.037f, -0.351f)
                arcToRelative(1.21f, 1.21f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.215f, -0.176f)
                moveTo(21.918f, 18.88f)
                arcToRelative(3.78f, 3.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.176f, 2.821f)
                arcToRelative(3.81f, 3.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.18f, 3.354f)
                lineToRelative(0.002f, 0.002f)
                curveToRelative(0.514f, 0.459f, 0.858f, 1.108f, 0.969f, 1.793f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.287f, 0.207f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.207f, -0.287f)
                arcToRelative(3.57f, 3.57f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.131f, -2.086f)
                lineToRelative(0.004f, 0.002f)
                curveToRelative(-0.786f, -0.722f, -1.19f, -1.854f, -1.024f, -2.907f)
                curveToRelative(0.166f, -1.051f, 0.906f, -2.007f, 1.885f, -2.441f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.127f, -0.33f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.33f, -0.127f)
                moveTo(25.266f, 18.693f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.291f, 0.202f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.199f, 0.29f)
                curveToRelative(0.933f, 0.174f, 1.781f, 0.854f, 2.146f, 1.721f)
                horizontalLineToRelative(-0.002f)
                arcToRelative(2.91f, 2.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.293f, 2.721f)
                lineToRelative(-0.002f, 0.002f)
                arcToRelative(5.25f, 5.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.923f, 3.244f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.263f, 0.236f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.237f, -0.263f)
                arcToRelative(4.76f, 4.76f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.835f, -2.934f)
                curveToRelative(0.64f, -0.922f, 0.78f, -2.156f, 0.346f, -3.2f)
                arcToRelative(3.46f, 3.46f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.515f, -2.019f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(24.12f, 25.09f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.26f, 0.254f)
                curveToRelative(0.07f, 5.83f, 0.15f, 11.649f, 0.22f, 17.478f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.252f, 0.248f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.248f, -0.254f)
                curveToRelative(-0.069f, -5.714f, -0.148f, -11.417f, -0.217f, -17.13f)
                curveToRelative(0.77f, 0.138f, 1.414f, 0.63f, 1.74f, 1.34f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.33f, 0.12f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.124f, -0.33f)
                curveToRelative(-0.434f, -0.942f, -1.323f, -1.682f, -2.438f, -1.726f)
                moveTo(23.154f, 27.072f)
                arcToRelative(0.62f, 0.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.617f, 0.076f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.264f, 0.555f)
                curveToRelative(0.02f, 0.238f, 0.181f, 0.41f, 0.364f, 0.496f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.613f, -0.035f)
                arcToRelative(0.62f, 0.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.256f, -0.562f)
                arcToRelative(0.61f, 0.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.342f, -0.52f)
                lineToRelative(-0.006f, 0.002f)
                close()
                moveTo(22.951f, 27.525f)
                lineTo(22.955f, 27.537f)
                curveToRelative(0.022f, 0.01f, 0.048f, 0.045f, 0.053f, 0.102f)
                reflectiveCurveToRelative(-0.017f, 0.1f, -0.04f, 0.115f)
                lineToRelative(-0.003f, 0.002f)
                curveToRelative(-0.014f, 0.01f, -0.06f, 0.018f, -0.113f, -0.008f)
                reflectiveCurveToRelative(-0.081f, -0.068f, -0.082f, -0.088f)
                lineToRelative(-0.002f, -0.01f)
                curveToRelative(-0.003f, -0.02f, 0.015f, -0.062f, 0.062f, -0.095f)
                curveToRelative(0.047f, -0.034f, 0.094f, -0.038f, 0.115f, -0.028f)
                close()
                moveTo(22.918f, 37.78f)
                arcToRelative(0.63f, 0.63f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.588f, 0.148f)
                arcToRelative(0.62f, 0.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.217f, 0.562f)
                lineToRelative(0.002f, 0.008f)
                curveToRelative(0.088f, 0.455f, 0.63f, 0.67f, 1.002f, 0.39f)
                arcToRelative(0.633f, 0.633f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.095f, -1.072f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.07f, 0.024f)
                arcToRelative(0.3f, 0.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.034f, -0.06f)
                moveToRelative(-0.123f, 0.474f)
                lineToRelative(0.004f, 0.01f)
                curveToRelative(0.058f, 0.029f, 0.064f, 0.195f, 0.025f, 0.222f)
                lineToRelative(-0.006f, 0.004f)
                curveToRelative(-0.046f, 0.036f, -0.188f, -0.024f, -0.207f, -0.086f)
                curveToRelative(-0.001f, -0.023f, 0.01f, -0.065f, 0.051f, -0.101f)
                curveToRelative(0.044f, -0.039f, 0.094f, -0.05f, 0.121f, -0.043f)
                close()
                moveTo(22.896f, 32.215f)
                arcToRelative(0.64f, 0.64f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.578f, 0.201f)
                arcToRelative(0.63f, 0.63f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.172f, 0.582f)
                arcToRelative(0.63f, 0.63f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.418f, 0.441f)
                curveToRelative(0.19f, 0.067f, 0.422f, 0.062f, 0.608f, -0.08f)
                arcToRelative(0.65f, 0.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.078f, -1.076f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.127f, 0.035f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.07f, -0.103f)
                moveToRelative(-0.058f, 0.467f)
                lineToRelative(0.01f, 0.035f)
                curveToRelative(0.06f, 0.034f, 0.074f, 0.203f, 0.021f, 0.244f)
                curveToRelative(-0.024f, 0.018f, -0.078f, 0.028f, -0.137f, 0.008f)
                curveToRelative(-0.058f, -0.02f, -0.093f, -0.064f, -0.1f, -0.088f)
                curveToRelative(-0.005f, -0.025f, 0.008f, -0.077f, 0.05f, -0.121f)
                curveToRelative(0.042f, -0.045f, 0.09f, -0.06f, 0.12f, -0.055f)
                close()
                moveTo(22.818f, 19.48f)
                curveToRelative(-0.658f, 0.793f, -1.127f, 1.638f, -1.588f, 2.479f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.1f, 0.34f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.34f, -0.1f)
                curveToRelative(0.348f, -0.636f, 0.746f, -1.23f, 1.162f, -1.818f)
                lineToRelative(0.2f, 2.033f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.273f, 0.225f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.224f, -0.274f)
                lineToRelative(-0.27f, -2.75f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.44f, -0.135f)
                moveTo(25.104f, 20.559f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.106f, 0.337f)
                curveToRelative(0.366f, 0.701f, 0.855f, 1.52f, 1.22f, 2.202f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.34f, 0.103f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.102f, -0.34f)
                curveToRelative(-0.374f, -0.698f, -0.864f, -1.517f, -1.219f, -2.197f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.337f, -0.105f)
                moveTo(46.727f, 18.783f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.354f, 0.02f)
                arcToRelative(27.1f, 27.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8.252f, 6.232f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.115f, 0.334f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.334f, 0.115f)
                arcToRelative(27.6f, 27.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.406f, -6.347f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.02f, -0.354f)
                moveTo(1.154f, 18.691f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.021f, 0.354f)
                arcToRelative(28.9f, 28.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.816f, 6.273f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.34f, -0.097f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.1f, -0.34f)
                arcToRelative(28.4f, 28.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7.681f, -6.166f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.354f, -0.024f)
            }
        }.build()

        return _Shirts!!
    }

@Suppress("ObjectPropertyName")
private var _Shirts: ImageVector? = null
