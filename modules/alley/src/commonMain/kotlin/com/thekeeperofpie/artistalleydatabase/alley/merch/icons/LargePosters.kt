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
val MerchIcons.LargePosters: ImageVector
    get() {
        if (_LargePosters != null) {
            return _LargePosters!!
        }
        _LargePosters = ImageVector.Builder(
            name = "LargePosters",
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
                moveTo(7.21f, 2.125f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.374f, 0.375f)
                verticalLineToRelative(42.06f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.375f, 0.376f)
                horizontalLineToRelative(25.898f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.26f, -0.106f)
                lineToRelative(7.24f, -6.97f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.004f, -0.159f)
                arcToRelative(0.4f, 0.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.112f, -0.111f)
                lineTo(40.725f, 2.5f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.375f, -0.375f)
                close()
                moveTo(7.586f, 2.875f)
                horizontalLineToRelative(32.389f)
                verticalLineToRelative(34.557f)
                lineToRelative(-7.016f, 6.754f)
                lineTo(7.586f, 44.186f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(33.11f, 37.215f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.376f, 0.375f)
                verticalLineToRelative(6.64f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.63f, 0.276f)
                lineToRelative(7.24f, -6.639f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.254f, -0.652f)
                close()
                moveTo(33.484f, 37.965f)
                horizontalLineToRelative(5.903f)
                lineToRelative(-5.903f, 5.414f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10.31f, 7.78f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, 0.25f)
                verticalLineTo(41.8f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.25f)
                horizontalLineToRelative(22.8f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, -0.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, -0.25f)
                horizontalLineTo(10.56f)
                verticalLineTo(8.28f)
                horizontalLineTo(37f)
                verticalLineToRelative(29.31f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, -0.25f)
                verticalLineTo(8.03f)
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
                moveTo(30.14f, 24.11f)
                horizontalLineTo(28.2f)
                curveToRelative(0.42f, -0.6f, 0.67f, -1.28f, 0.67f, -2.02f)
                curveToRelative(0f, -1.24f, -0.68f, -2.33f, -1.73f, -3.09f)
                curveToRelative(0.24f, -0.4f, 0.46f, -0.86f, 0.63f, -1.37f)
                curveToRelative(0.57f, -1.72f, 0.42f, -3.32f, -0.33f, -3.57f)
                reflectiveCurveToRelative(-1.82f, 0.95f, -2.39f, 2.67f)
                curveToRelative(-0.15f, 0.45f, -0.25f, 0.89f, -0.3f, 1.3f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.02f, 0.01f)
                curveToRelative(-0.05f, -0.41f, -0.15f, -0.86f, -0.3f, -1.31f)
                curveToRelative(-0.57f, -1.72f, -1.64f, -2.92f, -2.39f, -2.67f)
                reflectiveCurveToRelative(-0.9f, 1.84f, -0.33f, 3.57f)
                curveToRelative(0.18f, 0.54f, 0.41f, 1.01f, 0.65f, 1.42f)
                curveToRelative(-1.02f, 0.76f, -1.66f, 1.83f, -1.66f, 3.04f)
                curveToRelative(0f, 0.74f, 0.26f, 1.42f, 0.67f, 2.02f)
                horizontalLineToRelative(-1.53f)
                curveToRelative(-0.58f, 0f, -1.06f, 0.48f, -1.06f, 1.06f)
                reflectiveCurveToRelative(0.48f, 1.06f, 1.06f, 1.06f)
                horizontalLineToRelative(2.18f)
                curveToRelative(-0.16f, 0.4f, -0.26f, 0.83f, -0.26f, 1.28f)
                curveToRelative(0f, 0.96f, 0.4f, 1.82f, 1.05f, 2.44f)
                lineToRelative(-0.47f, 1.09f)
                curveToRelative(-0.23f, 0.53f, 0.02f, 1.16f, 0.55f, 1.39f)
                reflectiveCurveToRelative(1.16f, -0.02f, 1.39f, -0.55f)
                lineToRelative(0.43f, -1.01f)
                curveToRelative(0.14f, 0.02f, 0.29f, 0.04f, 0.44f, 0.04f)
                horizontalLineToRelative(1.67f)
                curveToRelative(0.2f, 0f, 0.39f, -0.03f, 0.58f, -0.06f)
                lineToRelative(0.44f, 1.02f)
                curveToRelative(0.23f, 0.53f, 0.85f, 0.78f, 1.39f, 0.55f)
                curveToRelative(0.53f, -0.23f, 0.78f, -0.85f, 0.55f, -1.39f)
                lineToRelative(-0.51f, -1.19f)
                curveToRelative(0.58f, -0.61f, 0.95f, -1.43f, 0.95f, -2.33f)
                curveToRelative(0f, -0.45f, -0.09f, -0.88f, -0.26f, -1.28f)
                horizontalLineToRelative(2.15f)
                curveToRelative(0.58f, 0f, 1.06f, -0.48f, 1.06f, -1.06f)
                reflectiveCurveToRelative(-0.48f, -1.06f, -1.06f, -1.06f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.96f, 13.822f)
                curveToRelative(-0.26f, 0.087f, -0.457f, 0.29f, -0.589f, 0.541f)
                reflectiveCurveToRelative(-0.208f, 0.559f, -0.24f, 0.905f)
                curveToRelative(-0.064f, 0.691f, 0.047f, 1.548f, 0.342f, 2.441f)
                curveToRelative(0.165f, 0.496f, 0.379f, 0.914f, 0.595f, 1.3f)
                curveToRelative(-0.969f, 0.79f, -1.619f, 1.855f, -1.619f, 3.08f)
                curveToRelative(0f, 0.655f, 0.28f, 1.225f, 0.586f, 1.77f)
                horizontalLineTo(17.84f)
                curveToRelative(-0.718f, 0f, -1.31f, 0.593f, -1.31f, 1.31f)
                curveToRelative(0f, 0.719f, 0.592f, 1.311f, 1.31f, 1.311f)
                horizontalLineToRelative(1.877f)
                curveToRelative(-0.102f, 0.332f, -0.207f, 0.665f, -0.207f, 1.03f)
                curveToRelative(0f, 0.96f, 0.4f, 1.812f, 1.017f, 2.465f)
                lineToRelative(-0.416f, 0.966f)
                arcToRelative(1.314f, 1.314f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.68f, 1.72f)
                arcToRelative(1.314f, 1.314f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.719f, -0.68f)
                verticalLineToRelative(-0.003f)
                lineToRelative(0.36f, -0.843f)
                curveToRelative(0.091f, 0.01f, 0.179f, 0.025f, 0.28f, 0.025f)
                horizontalLineToRelative(1.67f)
                curveToRelative(0.154f, 0f, 0.286f, -0.024f, 0.422f, -0.043f)
                lineToRelative(0.37f, 0.852f)
                arcToRelative(1.315f, 1.315f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.716f, 0.681f)
                lineToRelative(0.002f, -0.002f)
                arcToRelative(1.314f, 1.314f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.68f, -1.716f)
                lineToRelative(-0.46f, -1.073f)
                curveToRelative(0.551f, -0.637f, 0.92f, -1.446f, 0.92f, -2.35f)
                curveToRelative(0f, -0.36f, -0.106f, -0.695f, -0.21f, -1.029f)
                horizontalLineToRelative(1.85f)
                curveToRelative(0.717f, 0f, 1.31f, -0.592f, 1.31f, -1.31f)
                curveToRelative(0f, -0.706f, -0.578f, -1.277f, -1.28f, -1.297f)
                verticalLineToRelative(-0.014f)
                horizontalLineToRelative(-1.605f)
                curveToRelative(0.31f, -0.547f, 0.584f, -1.118f, 0.584f, -1.77f)
                curveToRelative(0f, -1.258f, -0.685f, -2.344f, -1.685f, -3.134f)
                curveToRelative(0.213f, -0.377f, 0.42f, -0.782f, 0.574f, -1.246f)
                curveToRelative(0.294f, -0.888f, 0.406f, -1.745f, 0.342f, -2.438f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.24f, -0.906f)
                curveToRelative(-0.132f, -0.252f, -0.33f, -0.456f, -0.59f, -0.543f)
                curveToRelative(-0.262f, -0.087f, -0.544f, -0.042f, -0.8f, 0.082f)
                curveToRelative(-0.255f, 0.124f, -0.498f, 0.327f, -0.73f, 0.586f)
                curveToRelative(-0.463f, 0.519f, -0.883f, 1.272f, -1.177f, 2.16f)
                arcToRelative(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.256f, 1.114f)
                curveToRelative(-0.251f, -0.032f, -0.503f, -0.065f, -0.768f, -0.065f)
                curveToRelative(-0.294f, 0f, -0.579f, 0.036f, -0.865f, 0.074f)
                arcToRelative(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.256f, -1.123f)
                curveToRelative(-0.294f, -0.888f, -0.716f, -1.641f, -1.18f, -2.16f)
                curveToRelative(-0.231f, -0.259f, -0.472f, -0.462f, -0.728f, -0.586f)
                reflectiveCurveToRelative(-0.538f, -0.169f, -0.799f, -0.082f)
                moveToRelative(0.16f, 0.475f)
                curveToRelative(0.113f, -0.038f, 0.247f, -0.026f, 0.421f, 0.058f)
                curveToRelative(0.174f, 0.085f, 0.373f, 0.243f, 0.574f, 0.467f)
                curveToRelative(0.402f, 0.45f, 0.803f, 1.154f, 1.078f, 1.987f)
                curveToRelative(0.145f, 0.434f, 0.242f, 0.87f, 0.29f, 1.261f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.288f, 0.217f)
                arcToRelative(5.787f, 5.787f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.94f, -0.01f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.287f, -0.216f)
                arcToRelative(6.5f, 6.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.29f, -1.252f)
                curveToRelative(0.275f, -0.833f, 0.674f, -1.538f, 1.075f, -1.987f)
                curveToRelative(0.201f, -0.224f, 0.403f, -0.382f, 0.576f, -0.467f)
                curveToRelative(0.174f, -0.084f, 0.309f, -0.096f, 0.422f, -0.058f)
                curveToRelative(0.114f, 0.038f, 0.215f, 0.129f, 0.305f, 0.3f)
                curveToRelative(0.09f, 0.173f, 0.158f, 0.42f, 0.186f, 0.72f)
                curveToRelative(0.055f, 0.6f, -0.043f, 1.402f, -0.319f, 2.234f)
                arcToRelative(6.7f, 6.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.607f, 1.32f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.068f, 0.332f)
                curveToRelative(1f, 0.724f, 1.625f, 1.738f, 1.625f, 2.887f)
                curveToRelative(0f, 0.684f, -0.227f, 1.311f, -0.623f, 1.877f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.203f, 0.392f)
                horizontalLineToRelative(1.91f)
                curveToRelative(0.442f, 0f, 0.81f, 0.369f, 0.81f, 0.81f)
                curveToRelative(0f, 0.443f, -0.368f, 0.811f, -0.81f, 0.811f)
                horizontalLineToRelative(-2.148f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.23f, 0.348f)
                curveToRelative(0.157f, 0.37f, 0.24f, 0.765f, 0.24f, 1.182f)
                curveToRelative(0f, 0.831f, -0.344f, 1.59f, -0.883f, 2.158f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.047f, 0.271f)
                lineToRelative(0.51f, 1.19f)
                arcToRelative(0.81f, 0.81f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.418f, 1.06f)
                horizontalLineToRelative(-0.002f)
                arcToRelative(0.81f, 0.81f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.06f, -0.42f)
                lineToRelative(-0.442f, -1.017f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.268f, -0.148f)
                curveToRelative(-0.19f, 0.03f, -0.366f, 0.056f, -0.54f, 0.056f)
                horizontalLineToRelative(-1.67f)
                curveToRelative(-0.128f, 0f, -0.267f, -0.017f, -0.405f, -0.037f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.266f, 0.148f)
                lineToRelative(-0.43f, 1.01f)
                arcToRelative(0.814f, 0.814f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.06f, 0.42f)
                arcToRelative(0.817f, 0.817f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.42f, -1.062f)
                lineToRelative(0.469f, -1.09f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.057f, -0.28f)
                arcToRelative(3.1f, 3.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.972f, -2.26f)
                curveToRelative(0f, -0.412f, 0.092f, -0.81f, 0.242f, -1.187f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.232f, -0.342f)
                horizontalLineToRelative(-2.18f)
                arcToRelative(0.82f, 0.82f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.81f, -0.81f)
                curveToRelative(0f, -0.442f, 0.368f, -0.81f, 0.81f, -0.81f)
                horizontalLineToRelative(1.53f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.206f, -0.391f)
                curveToRelative(-0.388f, -0.568f, -0.627f, -1.198f, -0.627f, -1.88f)
                curveToRelative(0f, -1.121f, 0.59f, -2.116f, 1.56f, -2.839f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.067f, -0.326f)
                arcToRelative(7.2f, 7.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.629f, -1.371f)
                verticalLineToRelative(-0.002f)
                curveToRelative(-0.275f, -0.837f, -0.374f, -1.64f, -0.318f, -2.239f)
                curveToRelative(0.028f, -0.299f, 0.094f, -0.545f, 0.183f, -0.716f)
                curveToRelative(0.09f, -0.171f, 0.193f, -0.261f, 0.307f, -0.3f)
            }
            path(fill = SolidColor(Color(0xFF231F20))) {
                moveTo(10.31f, 5.28f)
                moveToRelative(-0.78f, 0f)
                arcToRelative(0.78f, 0.78f, 0f, isMoreThanHalf = true, isPositiveArc = true, 1.56f, 0f)
                arcToRelative(0.78f, 0.78f, 0f, isMoreThanHalf = true, isPositiveArc = true, -1.56f, 0f)
            }
            path(fill = SolidColor(Color(0xFF231F20))) {
                moveTo(36.73f, 5.28f)
                moveToRelative(-0.78f, 0f)
                arcToRelative(0.78f, 0.78f, 0f, isMoreThanHalf = true, isPositiveArc = true, 1.56f, 0f)
                arcToRelative(0.78f, 0.78f, 0f, isMoreThanHalf = true, isPositiveArc = true, -1.56f, 0f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10.31f, 28.6f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, 0.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, 0.25f)
                horizontalLineToRelative(26.94f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.25f, -0.25f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, -0.25f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(13.465f, 10.646f)
                arcToRelative(2.98f, 2.98f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.738f, 1.188f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.058f, 0.35f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.348f, -0.059f)
                curveToRelative(0.751f, -1.054f, 2.076f, -1.352f, 2.972f, -0.71f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.348f, -0.06f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.058f, -0.35f)
                curveToRelative(-0.572f, -0.408f, -1.267f, -0.512f, -1.93f, -0.359f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.715f, 9.916f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.738f, 1.19f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.058f, 0.347f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.348f, -0.058f)
                curveToRelative(0.751f, -1.054f, 2.076f, -1.353f, 2.972f, -0.711f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.348f, -0.059f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.058f, -0.348f)
                curveToRelative(-0.572f, -0.409f, -1.267f, -0.515f, -1.93f, -0.361f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(31.648f, 15.457f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.097f, 0.123f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.131f, 0.33f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.33f, 0.13f)
                curveToRelative(1.181f, -0.514f, 2.472f, -0.089f, 2.91f, 0.911f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.33f, 0.127f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.13f, -0.328f)
                curveToRelative(-0.282f, -0.64f, -0.824f, -1.085f, -1.472f, -1.293f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(34.809f, 16.508f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.098f, 0.123f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.13f, 0.328f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.33f, 0.13f)
                curveToRelative(1.18f, -0.512f, 2.47f, -0.089f, 2.91f, 0.911f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.33f, 0.129f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.128f, -0.33f)
                curveToRelative(-0.28f, -0.64f, -0.823f, -1.083f, -1.47f, -1.291f)
            }
        }.build()

        return _LargePosters!!
    }

@Suppress("ObjectPropertyName")
private var _LargePosters: ImageVector? = null
