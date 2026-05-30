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
val MerchIcons.Puzzles: ImageVector
    get() {
        if (_Puzzles != null) {
            return _Puzzles!!
        }
        _Puzzles = ImageVector.Builder(
            name = "Puzzles",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.38f, 12.06f)
                horizontalLineToRelative(13.76f)
                curveToRelative(0.897f, 0f, 1.62f, 0.723f, 1.62f, 1.62f)
                reflectiveCurveToRelative(-0.723f, 1.62f, -1.62f, 1.62f)
                horizontalLineTo(17.38f)
                curveToRelative(-0.897f, 0f, -1.62f, -0.723f, -1.62f, -1.62f)
                reflectiveCurveToRelative(0.723f, -1.62f, 1.62f, -1.62f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.38f, 11.81f)
                arcToRelative(1.87f, 1.87f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.87f, 1.87f)
                curveToRelative(0f, 1.031f, 0.84f, 1.87f, 1.87f, 1.87f)
                horizontalLineToRelative(13.76f)
                arcToRelative(1.87f, 1.87f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -3.74f)
                close()
                moveTo(17.38f, 12.31f)
                horizontalLineToRelative(13.76f)
                curveToRelative(0.764f, 0f, 1.37f, 0.606f, 1.37f, 1.37f)
                curveToRelative(0f, 0.763f, -0.606f, 1.37f, -1.37f, 1.37f)
                lineTo(17.38f, 15.05f)
                arcToRelative(1.363f, 1.363f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.37f, -1.37f)
                curveToRelative(0f, -0.764f, 0.608f, -1.37f, 1.37f, -1.37f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.88f, 5.82f)
                horizontalLineToRelative(9.18f)
                verticalLineTo(15f)
                horizontalLineToRelative(-9.18f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.88f, 5.57f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, 0.25f)
                lineTo(19.63f, 15f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.25f)
                horizontalLineToRelative(9.18f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, -0.25f)
                lineTo(29.31f, 5.82f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, -0.25f)
                close()
                moveTo(20.13f, 6.07f)
                horizontalLineToRelative(8.68f)
                verticalLineToRelative(8.68f)
                horizontalLineToRelative(-8.68f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(36.064f, 5.967f)
                curveToRelative(-1.791f, 0.217f, -2.997f, 0.82f, -3.953f, 2.135f)
                curveToRelative(-0.925f, 1.271f, -1.666f, 3.207f, -2.728f, 6.103f)
                lineTo(18.609f, 14.205f)
                curveToRelative(0.106f, -0.543f, 0.346f, -1.927f, 0.164f, -3.613f)
                curveToRelative(-0.11f, -1.03f, -0.39f, -2.085f, -1.023f, -2.914f)
                reflectiveCurveToRelative(-1.64f, -1.396f, -3.045f, -1.412f)
                curveToRelative(-2.62f, -0.03f, -4.643f, -0.055f, -6.094f, 0.289f)
                curveToRelative(-0.725f, 0.171f, -1.321f, 0.44f, -1.746f, 0.884f)
                curveToRelative(-0.424f, 0.445f, -0.643f, 1.053f, -0.66f, 1.782f)
                curveToRelative(-0.03f, 1.304f, -0.168f, 2.318f, -0.113f, 3.12f)
                curveToRelative(0.027f, 0.402f, 0.104f, 0.767f, 0.316f, 1.071f)
                reflectiveCurveToRelative(0.555f, 0.507f, 0.975f, 0.596f)
                curveToRelative(1.323f, 0.28f, 2.58f, 0.184f, 2.941f, 0.164f)
                verticalLineToRelative(4.033f)
                lineTo(4.69f, 18.205f)
                reflectiveCurveToRelative(-0.25f, 0.018f, -0.523f, 0.164f)
                reflectiveCurveToRelative(-0.61f, 0.42f, -0.926f, 0.912f)
                curveToRelative(-0.633f, 0.984f, -1.205f, 2.819f, -1.205f, 6.36f)
                curveToRelative(0f, 3.524f, 0.333f, 5.32f, 0.7f, 6.287f)
                curveToRelative(0.183f, 0.483f, 0.377f, 0.763f, 0.562f, 0.931f)
                arcToRelative(0.9f, 0.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.262f, 0.17f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.185f, 0.045f)
                lineTo(8.14f, 33.074f)
                lineToRelative(-0.068f, -0.006f)
                reflectiveCurveToRelative(0.214f, 0.033f, 0.444f, -0.039f)
                curveToRelative(0.23f, -0.071f, 0.51f, -0.243f, 0.761f, -0.572f)
                curveToRelative(0.502f, -0.658f, 0.907f, -1.907f, 0.887f, -4.45f)
                verticalLineToRelative(-0.032f)
                curveToRelative(-0.02f, -0.213f, -0.039f, -0.397f, -0.039f, -0.514f)
                curveToRelative(0f, -0.711f, 0.098f, -0.987f, 0.203f, -1.08f)
                reflectiveCurveToRelative(0.414f, -0.156f, 1.041f, -0.156f)
                curveToRelative(0.593f, 0f, 0.826f, 0.103f, 0.953f, 0.248f)
                curveToRelative(0.127f, 0.144f, 0.213f, 0.451f, 0.266f, 0.974f)
                verticalLineToRelative(0.004f)
                reflectiveCurveToRelative(0.206f, 1.876f, 0.316f, 3.79f)
                curveToRelative(0.056f, 0.956f, 0.086f, 1.924f, 0.057f, 2.655f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.09f, 0.877f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.055f, 0.184f)
                curveToRelative(-0.11f, 0.041f, -0.362f, 0.089f, -0.695f, 0.107f)
                curveToRelative(-0.356f, 0.02f, -0.786f, 0.02f, -1.219f, 0.026f)
                reflectiveCurveToRelative(-0.87f, 0.015f, -1.26f, 0.058f)
                reflectiveCurveToRelative(-0.734f, 0.1f, -1.02f, 0.307f)
                curveToRelative(-0.54f, 0.387f, -0.851f, 1.018f, -1.056f, 1.715f)
                curveToRelative(-0.204f, 0.697f, -0.3f, 1.475f, -0.34f, 2.209f)
                curveToRelative(-0.08f, 1.468f, 0.061f, 2.771f, 0.061f, 2.771f)
                lineToRelative(0.002f, 0.014f)
                reflectiveCurveToRelative(0.066f, 0.469f, 0.371f, 0.936f)
                curveToRelative(0.305f, 0.466f, 0.902f, 0.955f, 1.82f, 0.955f)
                horizontalLineToRelative(10.24f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.202f, -0.059f)
                reflectiveCurveToRelative(2.286f, -1.452f, 3.178f, -4.408f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.036f, -0.295f)
                arcToRelative(3.9f, 3.9f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.5f, -1.943f)
                curveToRelative(0f, -0.913f, 0.286f, -1.732f, 0.727f, -2.309f)
                curveToRelative(0.44f, -0.577f, 1.017f, -0.906f, 1.639f, -0.906f)
                curveToRelative(0.491f, 0f, 0.956f, 0.21f, 1.347f, 0.594f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.077f, 0.056f)
                reflectiveCurveToRelative(-0.034f, -0.024f, -0.028f, -0.017f)
                quadToRelative(0.012f, 0.008f, 0.059f, 0.076f)
                lineToRelative(0.033f, 0.043f)
                curveToRelative(0.513f, 0.584f, 0.857f, 1.458f, 0.857f, 2.453f)
                curveToRelative(0f, 0.914f, -0.28f, 1.727f, -0.714f, 2.305f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.077f, 0.238f)
                lineToRelative(0.032f, 0.947f)
                verticalLineToRelative(0.014f)
                reflectiveCurveToRelative(0.05f, 0.825f, 0.629f, 1.63f)
                curveToRelative(0.578f, 0.806f, 1.71f, 1.59f, 3.685f, 1.59f)
                horizontalLineToRelative(6.29f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.06f, -0.003f)
                reflectiveCurveToRelative(0.847f, -0.135f, 1.705f, -0.584f)
                curveToRelative(0.857f, -0.45f, 1.79f, -1.278f, 1.79f, -2.596f)
                curveToRelative(0f, -1.06f, 0.419f, -2.013f, 0.186f, -3.033f)
                curveToRelative(-0.116f, -0.51f, -0.426f, -1.005f, -0.99f, -1.442f)
                curveToRelative(-0.507f, -0.392f, -1.307f, -0.75f, -2.312f, -1.107f)
                lineToRelative(0.109f, -5.45f)
                lineToRelative(5.568f, 0.055f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.266f, -0.63f)
                lineToRelative(0.098f, 0.304f)
                reflectiveCurveToRelative(0.475f, -3.023f, 0.595f, -7.047f)
                curveToRelative(0.031f, -1.036f, -0.094f, -1.81f, -0.345f, -2.394f)
                reflectiveCurveToRelative(-0.642f, -0.973f, -1.077f, -1.184f)
                curveToRelative(-0.868f, -0.422f, -1.822f, -0.186f, -2.396f, -0.037f)
                curveToRelative(-0.43f, 0.111f, -0.718f, 0.45f, -0.934f, 0.822f)
                reflectiveCurveToRelative(-0.37f, 0.804f, -0.488f, 1.223f)
                curveToRelative(-0.19f, 0.666f, -0.23f, 1.063f, -0.262f, 1.295f)
                lineTo(37.37f, 22.256f)
                lineToRelative(-0.053f, -3.211f)
                curveToRelative(0.164f, -0.109f, 1.657f, -1.09f, 3.188f, -2.6f)
                curveToRelative(0.806f, -0.795f, 1.584f, -1.701f, 2.1f, -2.658f)
                curveToRelative(0.515f, -0.956f, 0.776f, -1.99f, 0.453f, -2.975f)
                curveToRelative(-0.576f, -1.751f, -1.392f, -3.08f, -2.551f, -3.914f)
                quadToRelative(-1.74f, -1.25f, -4.44f, -0.931f)
                close()
                moveTo(36.154f, 6.713f)
                curveToRelative(1.67f, -0.197f, 2.924f, 0.084f, 3.912f, 0.795f)
                curveToRelative(0.988f, 0.71f, 1.733f, 1.88f, 2.278f, 3.539f)
                curveToRelative(0.237f, 0.72f, 0.059f, 1.533f, -0.4f, 2.385f)
                curveToRelative(-0.46f, 0.852f, -1.194f, 1.717f, -1.967f, 2.48f)
                curveToRelative(-1.548f, 1.527f, -3.243f, 2.645f, -3.243f, 2.645f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.17f, 0.32f)
                lineToRelative(0.061f, 3.76f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.375f, 0.369f)
                horizontalLineToRelative(2f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.373f, -0.334f)
                reflectiveCurveToRelative(0.086f, -0.74f, 0.303f, -1.506f)
                curveToRelative(0.108f, -0.383f, 0.251f, -0.77f, 0.416f, -1.053f)
                reflectiveCurveToRelative(0.343f, -0.437f, 0.472f, -0.47f)
                curveToRelative(0.546f, -0.142f, 1.325f, -0.284f, 1.881f, -0.014f)
                curveToRelative(0.278f, 0.135f, 0.524f, 0.36f, 0.715f, 0.805f)
                curveToRelative(0.191f, 0.444f, 0.314f, 1.11f, 0.285f, 2.074f)
                curveToRelative(-0.114f, 3.797f, -0.522f, 6.39f, -0.56f, 6.642f)
                lineToRelative(-5.58f, -0.054f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.38f, 0.367f)
                lineToRelative(-0.12f, 6.06f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.256f, 0.364f)
                curveToRelative(1.147f, 0.382f, 1.892f, 0.756f, 2.351f, 1.111f)
                reflectiveCurveToRelative(0.638f, 0.67f, 0.717f, 1.016f)
                curveToRelative(0.158f, 0.692f, -0.203f, 1.637f, -0.203f, 2.867f)
                curveToRelative(0f, 0.971f, -0.658f, 1.548f, -1.39f, 1.931f)
                curveToRelative(-0.725f, 0.38f, -1.442f, 0.5f, -1.46f, 0.502f)
                lineTo(30.83f, 43.314f)
                curveToRelative(-1.785f, 0f, -2.624f, -0.647f, -3.076f, -1.277f)
                curveToRelative(-0.45f, -0.628f, -0.488f, -1.232f, -0.488f, -1.236f)
                lineToRelative(-0.032f, -0.942f)
                verticalLineToRelative(-0.002f)
                curveToRelative(0.462f, -0.697f, 0.791f, -1.544f, 0.791f, -2.517f)
                curveToRelative(0f, -1.16f, -0.394f, -2.198f, -1.035f, -2.934f)
                arcToRelative(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.144f, -0.18f)
                curveToRelative(-0.021f, -0.02f, -0.028f, -0.021f, -0.05f, -0.037f)
                curveToRelative(-0.507f, -0.494f, -1.16f, -0.804f, -1.866f, -0.804f)
                curveToRelative(-0.889f, 0f, -1.683f, 0.476f, -2.235f, 1.199f)
                reflectiveCurveToRelative(-0.88f, 1.698f, -0.88f, 2.766f)
                curveToRelative(0f, 0.773f, 0.231f, 1.463f, 0.537f, 2.072f)
                curveToRelative(-0.799f, 2.56f, -2.652f, 3.808f, -2.766f, 3.883f)
                lineTo(9.48f, 43.305f)
                curveToRelative(-0.671f, 0f, -0.985f, -0.297f, -1.193f, -0.616f)
                curveToRelative(-0.206f, -0.316f, -0.253f, -0.626f, -0.254f, -0.63f)
                curveToRelative(-0.001f, -0.011f, -0.133f, -1.249f, -0.056f, -2.64f)
                curveToRelative(0.038f, -0.697f, 0.13f, -1.428f, 0.308f, -2.038f)
                curveToRelative(0.18f, -0.61f, 0.448f, -1.083f, 0.774f, -1.317f)
                curveToRelative(0.067f, -0.048f, 0.328f, -0.134f, 0.668f, -0.171f)
                reflectiveCurveToRelative(0.758f, -0.048f, 1.185f, -0.053f)
                reflectiveCurveToRelative(0.862f, -0.004f, 1.25f, -0.026f)
                curveToRelative(0.389f, -0.021f, 0.722f, -0.05f, 1.014f, -0.183f)
                arcToRelative(0.62f, 0.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.306f, -0.322f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.123f, -0.38f)
                curveToRelative(0.06f, -0.276f, 0.09f, -0.614f, 0.106f, -1.003f)
                curveToRelative(0.031f, -0.779f, -0.003f, -1.76f, -0.059f, -2.729f)
                curveToRelative(-0.112f, -1.939f, -0.32f, -3.828f, -0.32f, -3.828f)
                lineToRelative(0.002f, 0.002f)
                curveToRelative(-0.057f, -0.557f, -0.129f, -1.027f, -0.45f, -1.392f)
                curveToRelative(-0.32f, -0.366f, -0.828f, -0.504f, -1.515f, -0.504f)
                curveToRelative(-0.653f, 0f, -1.154f, 0.004f, -1.539f, 0.345f)
                curveToRelative(-0.384f, 0.342f, -0.455f, 0.872f, -0.455f, 1.64f)
                curveToRelative(0f, 0.175f, 0.02f, 0.36f, 0.04f, 0.556f)
                curveToRelative(0.018f, 2.456f, -0.4f, 3.55f, -0.733f, 3.986f)
                curveToRelative(-0.167f, 0.218f, -0.303f, 0.284f, -0.389f, 0.31f)
                reflectiveCurveToRelative(-0.086f, 0.02f, -0.086f, 0.02f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.066f, -0.008f)
                lineTo(3.828f, 32.324f)
                curveToRelative(-0.006f, -0.004f, -0.006f, -0.002f, -0.027f, -0.021f)
                curveToRelative(-0.068f, -0.062f, -0.211f, -0.234f, -0.365f, -0.64f)
                curveToRelative(-0.31f, -0.815f, -0.65f, -2.546f, -0.65f, -6.022f)
                curveToRelative(0f, -3.46f, 0.573f, -5.158f, 1.085f, -5.953f)
                curveToRelative(0.256f, -0.398f, 0.492f, -0.575f, 0.649f, -0.659f)
                curveToRelative(0.156f, -0.084f, 0.189f, -0.074f, 0.189f, -0.074f)
                horizontalLineToRelative(5.99f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.375f, -0.375f)
                verticalLineToRelative(-4.76f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.398f, -0.375f)
                reflectiveCurveToRelative(-1.802f, 0.11f, -3.139f, -0.172f)
                curveToRelative(-0.29f, -0.061f, -0.418f, -0.154f, -0.514f, -0.29f)
                curveToRelative(-0.095f, -0.138f, -0.16f, -0.36f, -0.183f, -0.692f)
                curveToRelative(-0.045f, -0.664f, 0.085f, -1.707f, 0.115f, -3.053f)
                curveToRelative(0.014f, -0.596f, 0.168f, -0.982f, 0.453f, -1.281f)
                reflectiveCurveToRelative(0.733f, -0.522f, 1.375f, -0.674f)
                curveToRelative(1.285f, -0.304f, 3.293f, -0.297f, 5.912f, -0.267f)
                curveToRelative(1.216f, 0.014f, 1.95f, 0.451f, 2.46f, 1.117f)
                curveToRelative(0.508f, 0.665f, 0.77f, 1.59f, 0.872f, 2.539f)
                curveToRelative(0.204f, 1.896f, -0.222f, 3.826f, -0.222f, 3.826f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.365f, 0.457f)
                horizontalLineToRelative(11.47f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.352f, -0.246f)
                curveToRelative(1.12f, -3.058f, 1.873f, -4.995f, 2.725f, -6.166f)
                reflectiveCurveToRelative(1.759f, -1.627f, 3.437f, -1.83f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.06f, 18.625f)
                verticalLineToRelative(0.75f)
                horizontalLineToRelative(6.29f)
                verticalLineToRelative(-0.75f)
                horizontalLineToRelative(-3.14f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.506f, 19.18f)
                verticalLineTo(22f)
                horizontalLineToRelative(0.75f)
                verticalLineToRelative(-2.82f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(25.91f, 18.625f)
                verticalLineToRelative(0.75f)
                horizontalLineToRelative(6.3f)
                verticalLineToRelative(-0.75f)
                horizontalLineToRelative(-3.15f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(30.365f, 19.18f)
                verticalLineTo(22f)
                horizontalLineToRelative(0.75f)
                verticalLineToRelative(-2.82f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(21.35f, 24.215f)
                verticalLineToRelative(0.75f)
                horizontalLineToRelative(4.24f)
                verticalLineToRelative(-0.75f)
                close()
            }
        }.build()

        return _Puzzles!!
    }

@Suppress("ObjectPropertyName")
private var _Puzzles: ImageVector? = null
