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
val MerchIcons.MusicBoxes: ImageVector
    get() {
        if (_MusicBoxes != null) {
            return _MusicBoxes!!
        }
        _MusicBoxes = ImageVector.Builder(
            name = "MusicBoxes",
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
                moveTo(4.29f, 27.346f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.376f, 0.375f)
                lineTo(3.914f, 39.25f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.375f, 0.375f)
                horizontalLineToRelative(30.65f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.375f, -0.375f)
                lineTo(35.314f, 27.72f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.375f, -0.374f)
                close()
                moveTo(4.664f, 28.096f)
                horizontalLineToRelative(29.9f)
                verticalLineToRelative(10.779f)
                horizontalLineToRelative(-29.9f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.66f, 38.9f)
                horizontalLineToRelative(31.92f)
                arcToRelative(1.474f, 1.474f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 2.95f)
                horizontalLineTo(3.66f)
                arcToRelative(1.474f, 1.474f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -2.95f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.66f, 38.65f)
                arcToRelative(1.73f, 1.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.73f, 1.725f)
                arcTo(1.73f, 1.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.66f, 42.1f)
                horizontalLineToRelative(31.92f)
                arcToRelative(1.73f, 1.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.73f, -1.725f)
                arcToRelative(1.73f, 1.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.73f, -1.725f)
                close()
                moveTo(3.66f, 39.15f)
                horizontalLineToRelative(31.92f)
                curveToRelative(0.686f, 0f, 1.23f, 0.542f, 1.23f, 1.225f)
                arcToRelative(1.22f, 1.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.23f, 1.225f)
                lineTo(3.66f, 41.6f)
                arcToRelative(1.22f, 1.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.23f, -1.225f)
                arcToRelative(1.22f, 1.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.23f, -1.225f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(39.57f, 30.01f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, 0.15f)
                verticalLineToRelative(2.41f)
                horizontalLineToRelative(-4.66f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, 0.15f)
                verticalLineToRelative(1.42f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.15f, 0.15f)
                horizontalLineToRelative(4.66f)
                verticalLineToRelative(2.41f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.15f, 0.15f)
                horizontalLineToRelative(3.31f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.15f, -0.15f)
                verticalLineToRelative(-6.54f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, -0.15f)
                close()
                moveTo(39.72f, 30.31f)
                horizontalLineToRelative(3.01f)
                verticalLineToRelative(6.24f)
                horizontalLineToRelative(-3.01f)
                verticalLineToRelative(-2.41f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.15f, -0.15f)
                horizontalLineToRelative(-4.66f)
                verticalLineToRelative(-1.12f)
                horizontalLineToRelative(4.66f)
                arcToRelative(0.15f, 0.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.15f, -0.15f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(40.58f, 30.79f)
                arcToRelative(0.1f, 0.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.1f, 0.1f)
                verticalLineToRelative(1.85f)
                arcToRelative(0.1f, 0.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.1f, 0.1f)
                horizontalLineToRelative(1.3f)
                arcToRelative(0.1f, 0.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.1f, -0.1f)
                verticalLineToRelative(-1.85f)
                arcToRelative(0.1f, 0.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.1f, -0.1f)
                close()
                moveTo(40.68f, 30.99f)
                horizontalLineToRelative(1.1f)
                verticalLineToRelative(1.65f)
                horizontalLineToRelative(-1.1f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(40.58f, 34.09f)
                arcToRelative(0.1f, 0.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.1f, 0.1f)
                verticalLineToRelative(1.85f)
                arcToRelative(0.1f, 0.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.1f, 0.1f)
                horizontalLineToRelative(1.3f)
                arcToRelative(0.1f, 0.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.1f, -0.1f)
                verticalLineToRelative(-1.85f)
                arcToRelative(0.1f, 0.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.1f, -0.1f)
                close()
                moveTo(40.68f, 34.29f)
                horizontalLineToRelative(1.1f)
                verticalLineToRelative(1.65f)
                horizontalLineToRelative(-1.1f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7.214f, 13.668f)
                arcToRelative(0.375f, 0.375f, 128.41f, isMoreThanHalf = false, isPositiveArc = false, -0.329f, 0.416f)
                lineToRelative(0.851f, 7.28f)
                arcToRelative(0.375f, 0.375f, 128.857f, isMoreThanHalf = false, isPositiveArc = false, 0.416f, 0.329f)
                lineToRelative(8.621f, -1.008f)
                arcToRelative(0.375f, 0.375f, 128.654f, isMoreThanHalf = false, isPositiveArc = false, 0.329f, -0.416f)
                lineToRelative(-0.851f, -7.28f)
                arcToRelative(0.375f, 0.375f, 55.86f, isMoreThanHalf = false, isPositiveArc = false, -0.416f, -0.329f)
                close()
                moveTo(7.673f, 14.369f)
                lineToRelative(7.876f, -0.921f)
                lineToRelative(0.764f, 6.535f)
                lineToRelative(-7.876f, 0.921f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7.307f, 14.408f)
                curveToRelative(-1.425f, 0.17f, -2.548f, 1.275f, -2.399f, 2.545f)
                lineToRelative(0.24f, 2.07f)
                curveToRelative(0.15f, 1.269f, 1.49f, 2.088f, 2.916f, 1.918f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.221f, -0.105f)
                lineToRelative(0.01f, -0.012f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.107f, -0.308f)
                lineToRelative(-0.68f, -5.78f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.415f, -0.328f)
                moveToRelative(-0.27f, 0.912f)
                lineToRelative(0.565f, 4.8f)
                curveToRelative(-0.895f, -0.051f, -1.63f, -0.514f, -1.71f, -1.183f)
                verticalLineToRelative(-0.002f)
                lineToRelative(-0.24f, -2.068f)
                verticalLineToRelative(-0.002f)
                curveToRelative(-0.078f, -0.665f, 0.53f, -1.283f, 1.385f, -1.545f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(22.72f, 5.479f)
                lineToRelative(-7.109f, 7.289f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.103f, 0.306f)
                lineToRelative(0.85f, 7.28f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.185f, 0.28f)
                lineToRelative(8.37f, 4.84f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.56f, -0.365f)
                lineTo(23.363f, 5.7f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.642f, -0.22f)
                moveToRelative(-0.017f, 1.091f)
                lineToRelative(1.943f, 17.885f)
                lineToRelative(-7.566f, -4.375f)
                lineToRelative(-0.807f, -6.916f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(23.715f, 12.176f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.338f, 0.412f)
                lineToRelative(0.598f, 5.762f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.414f, 0.373f)
                curveToRelative(1.424f, -0.147f, 2.559f, -1.233f, 2.424f, -2.51f)
                verticalLineToRelative(-0.002f)
                lineToRelative(-0.21f, -2.068f)
                verticalLineToRelative(-0.002f)
                curveToRelative(-0.136f, -1.269f, -1.465f, -2.1f, -2.888f, -1.965f)
                moveToRelative(0.46f, 0.836f)
                curveToRelative(0.887f, 0.075f, 1.61f, 0.544f, 1.682f, 1.209f)
                lineToRelative(0.21f, 2.066f)
                verticalLineToRelative(0.002f)
                curveToRelative(0.071f, 0.675f, -0.54f, 1.279f, -1.395f, 1.525f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(11.03f, 20.723f)
                lineToRelative(-2f, 0.238f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.208f, 0.326f)
                lineToRelative(2.12f, 6.51f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.238f, 0.174f)
                horizontalLineToRelative(3.17f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.224f, -0.36f)
                lineToRelative(-3.289f, -6.75f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.256f, -0.138f)
                moveToRelative(-0.116f, 0.517f)
                lineToRelative(3.035f, 6.23f)
                horizontalLineToRelative(-2.588f)
                lineToRelative(-1.97f, -6.048f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(30.59f, 19.45f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.01f, 1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.01f, -1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.01f, -1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.01f, 1.01f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(29.58f, 18.19f)
                curveToRelative(-0.693f, 0f, -1.26f, 0.566f, -1.26f, 1.26f)
                curveToRelative(0f, 0.692f, 0.567f, 1.26f, 1.26f, 1.26f)
                reflectiveCurveToRelative(1.26f, -0.568f, 1.26f, -1.26f)
                curveToRelative(0f, -0.694f, -0.567f, -1.26f, -1.26f, -1.26f)
                moveToRelative(0f, 0.5f)
                curveToRelative(0.423f, 0f, 0.76f, 0.337f, 0.76f, 0.76f)
                arcToRelative(0.76f, 0.76f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.76f, 0.76f)
                arcToRelative(0.76f, 0.76f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.76f, -0.76f)
                curveToRelative(0f, -0.423f, 0.337f, -0.76f, 0.76f, -0.76f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(30.854f, 13.916f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.407f, 0.342f)
                lineToRelative(-0.441f, 5.049f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.342f, 0.406f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.406f, -0.34f)
                lineToRelative(0.44f, -5.05f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.34f, -0.407f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(30.68f, 15.79f)
                lineToRelative(2.6f, 0.63f)
                lineToRelative(-2.35f, -2.3f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(31.105f, 13.941f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.421f, 0.141f)
                lineToRelative(-0.25f, 1.672f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.187f, 0.28f)
                lineToRelative(2.6f, 0.628f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.234f, -0.42f)
                close()
                moveTo(31.105f, 14.641f)
                lineTo(32.455f, 15.963f)
                lineTo(30.961f, 15.602f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(33.6f, 10.77f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.01f, 1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.01f, -1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.01f, -1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.01f, 1.01f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(32.59f, 9.51f)
                curveToRelative(-0.693f, 0f, -1.26f, 0.567f, -1.26f, 1.26f)
                curveToRelative(0f, 0.692f, 0.567f, 1.26f, 1.26f, 1.26f)
                reflectiveCurveToRelative(1.26f, -0.568f, 1.26f, -1.26f)
                reflectiveCurveToRelative(-0.567f, -1.26f, -1.26f, -1.26f)
                moveToRelative(0f, 0.5f)
                curveToRelative(0.423f, 0f, 0.76f, 0.337f, 0.76f, 0.76f)
                curveToRelative(0f, 0.422f, -0.337f, 0.76f, -0.76f, 0.76f)
                arcToRelative(0.756f, 0.756f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.76f, -0.76f)
                curveToRelative(0f, -0.423f, 0.337f, -0.76f, 0.76f, -0.76f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(33.34f, 5.176f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.365f, 0.385f)
                lineToRelative(0.14f, 5.218f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.385f, 0.366f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.365f, -0.385f)
                lineToRelative(-0.14f, -5.22f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.385f, -0.364f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(37.93f, 9.26f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.01f, 1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.01f, -1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.01f, -1.01f)
                arcToRelative(1.01f, 1.01f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.01f, 1.01f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(36.92f, 8f)
                curveToRelative(-0.693f, 0f, -1.26f, 0.567f, -1.26f, 1.26f)
                reflectiveCurveToRelative(0.567f, 1.26f, 1.26f, 1.26f)
                reflectiveCurveToRelative(1.26f, -0.567f, 1.26f, -1.26f)
                reflectiveCurveTo(37.613f, 8f, 36.92f, 8f)
                moveToRelative(0f, 0.5f)
                curveToRelative(0.423f, 0f, 0.76f, 0.337f, 0.76f, 0.76f)
                curveToRelative(0f, 0.422f, -0.337f, 0.76f, -0.76f, 0.76f)
                arcToRelative(0.756f, 0.756f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.76f, -0.76f)
                curveToRelative(0f, -0.423f, 0.337f, -0.76f, 0.76f, -0.76f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(37.67f, 3.654f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.365f, 0.385f)
                lineToRelative(0.14f, 5.23f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.385f, 0.366f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.365f, -0.385f)
                lineToRelative(-0.14f, -5.23f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.385f, -0.366f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(37.557f, 3.676f)
                lineToRelative(-4.33f, 1.52f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.23f, 0.478f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.478f, 0.23f)
                lineToRelative(4.33f, -1.52f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.228f, -0.478f)
                arcToRelative(0.375f, 0.375f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.476f, -0.23f)
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(9.62f, 11.951f)
                lineToRelative(-1.52f, 0.192f)
                curveToRelative(-0.35f, 0.042f, -0.617f, 0.298f, -0.772f, 0.619f)
                curveToRelative(-0.155f, 0.32f, -0.216f, 0.72f, -0.166f, 1.138f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.28f, 0.217f)
                lineToRelative(3.189f, -0.404f)
                lineToRelative(-0.078f, -0.076f)
                arcToRelative(0.25f, 0.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.426f, -0.21f)
                arcToRelative(2.06f, 2.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.454f, -1.064f)
                curveToRelative(-0.233f, -0.272f, -0.556f, -0.455f, -0.906f, -0.412f)
                moveToRelative(0.06f, 0.496f)
                curveToRelative(0.14f, -0.017f, 0.307f, 0.057f, 0.466f, 0.242f)
                curveToRelative(0.11f, 0.128f, 0.167f, 0.347f, 0.231f, 0.551f)
                lineToRelative(-2.684f, 0.342f)
                curveToRelative(0.01f, -0.219f, 0.01f, -0.45f, 0.084f, -0.602f)
                curveToRelative(0.105f, -0.217f, 0.243f, -0.324f, 0.383f, -0.341f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(21.7f, 14.3f)
                curveToRelative(-0.41f, -0.17f, -1f, 0.3f, -1.38f, 1.07f)
                curveToRelative(-0.62f, -0.59f, -1.33f, -0.84f, -1.66f, -0.54f)
                curveToRelative(-0.36f, 0.33f, -0.12f, 1.18f, 0.53f, 1.9f)
                curveToRelative(0.34f, 0.37f, 0.71f, 0.63f, 1.05f, 0.76f)
                curveToRelative(0.03f, 0.03f, 0.07f, 0.06f, 0.11f, 0.07f)
                curveToRelative(0.1f, 0.04f, 0.21f, 0.04f, 0.33f, 0.01f)
                horizontalLineToRelative(0.04f)
                quadToRelative(0.18f, -0.015f, 0.3f, -0.12f)
                curveToRelative(0.03f, -0.03f, 0.06f, -0.07f, 0.08f, -0.11f)
                curveToRelative(0.28f, -0.23f, 0.55f, -0.61f, 0.74f, -1.07f)
                curveToRelative(0.37f, -0.9f, 0.31f, -1.78f, -0.14f, -1.97f)
            }
        }.build()

        return _MusicBoxes!!
    }

@Suppress("ObjectPropertyName")
private var _MusicBoxes: ImageVector? = null
