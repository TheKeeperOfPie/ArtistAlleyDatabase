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
val MerchIcons.Bookmarks: ImageVector
    get() {
        if (_Bookmarks != null) {
            return _Bookmarks!!
        }
        _Bookmarks = ImageVector.Builder(
            name = "Bookmarks",
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
                moveTo(31.02f, 6.352f)
                arcToRelative(0.25f, 0.25f, 127.027f, isMoreThanHalf = false, isPositiveArc = false, -0.353f, 0.014f)
                lineToRelative(-24.943f, 27.002f)
                arcToRelative(0.25f, 0.25f, 75.334f, isMoreThanHalf = false, isPositiveArc = false, 0.014f, 0.353f)
                lineToRelative(9.41f, 8.692f)
                arcToRelative(0.25f, 0.25f, 123.462f, isMoreThanHalf = false, isPositiveArc = false, 0.353f, -0.014f)
                lineTo(40.444f, 15.397f)
                arcToRelative(0.25f, 0.25f, 87.628f, isMoreThanHalf = false, isPositiveArc = false, -0.014f, -0.353f)
                close()
                moveTo(30.865f, 6.889f)
                lineToRelative(9.042f, 8.353f)
                lineToRelative(-24.604f, 26.635f)
                lineTo(6.261f, 33.524f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(33.5f, 11.494f)
                arcToRelative(1.316f, 1.316f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 2.63f)
                arcToRelative(1.316f, 1.316f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -2.63f)
                moveToRelative(0f, 0.25f)
                curveToRelative(0.59f, 0f, 1.064f, 0.477f, 1.064f, 1.067f)
                reflectiveCurveToRelative(-0.474f, 1.064f, -1.064f, 1.064f)
                reflectiveCurveToRelative(-1.064f, -0.475f, -1.064f, -1.064f)
                reflectiveCurveToRelative(0.474f, -1.067f, 1.064f, -1.067f)
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(34.92f, 10.3f)
                lineToRelative(-1.3f, 1.79f)
                lineToRelative(0.62f, 0.58f)
                lineToRelative(1.69f, -1.44f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.25f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(35.004f, 10.209f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.186f, 0.018f)
                lineToRelative(-1.298f, 1.789f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.015f, 0.166f)
                lineToRelative(0.62f, 0.58f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.165f, 0.004f)
                lineToRelative(1.692f, -1.442f)
                arcToRelative(0.125f, 0.125f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.004f, -0.185f)
                close()
                moveTo(34.939f, 10.486f)
                lineTo(35.742f, 11.226f)
                lineTo(34.244f, 12.502f)
                lineTo(33.785f, 12.074f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(38.83f, 7.1f)
                curveToRelative(-0.79f, 1.47f, -3.3f, 3.56f, -3.3f, 3.56f)
                lineToRelative(-0.5f, -0.47f)
                curveToRelative(0.78f, -0.3f, 3f, -5.34f, 3f, -5.34f)
                lineToRelative(0.3f, 1.78f)
                lineToRelative(1.47f, -1.14f)
                lineToRelative(-0.96f, 1.61f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(40.23f, 9.03f)
                curveToRelative(-1.26f, 1.1f, -4.35f, 2.16f, -4.35f, 2.16f)
                lineToRelative(-0.31f, -0.62f)
                curveToRelative(0.84f, 0f, 4.7f, -3.93f, 4.7f, -3.93f)
                lineToRelative(-0.35f, 1.77f)
                lineToRelative(1.78f, -0.55f)
                close()
            }
            path(fill = SolidColor(Color(0xFF231F20))) {
                moveToRelative(12.86f, 33.17f)
                lineToRelative(0.07f, -0.08f)
                curveToRelative(0.64f, -0.72f, 1.48f, -0.96f, 2.51f, -0.71f)
                curveToRelative(0.35f, 0.1f, 0.67f, 0.26f, 0.97f, 0.49f)
                curveToRelative(0.29f, 0.22f, 0.62f, 0.59f, 1f, 1.09f)
                lineToRelative(1.68f, 2.07f)
                lineToRelative(-0.98f, 1.11f)
                curveToRelative(-1.35f, -1.66f, -2.08f, -2.56f, -2.19f, -2.68f)
                curveToRelative(-0.09f, -0.09f, -0.17f, -0.17f, -0.25f, -0.23f)
                curveToRelative(-0.34f, -0.26f, -0.73f, -0.37f, -1.15f, -0.31f)
                reflectiveCurveToRelative(2.66f, 3.27f, 2.66f, 3.27f)
                lineToRelative(0.42f, 0.52f)
                lineToRelative(-0.98f, 1.11f)
                horizontalLineToRelative(-0.01f)
                lineToRelative(-3.09f, -3.81f)
                curveToRelative(-0.05f, 0.14f, -0.04f, 0.41f, 0f, 0.8f)
                curveToRelative(0.09f, 0.41f, 0.23f, 0.73f, 0.41f, 0.95f)
                lineToRelative(2.16f, 2.65f)
                lineToRelative(-0.98f, 1.11f)
                horizontalLineToRelative(-0.02f)
                quadToRelative(-2.1f, -2.58f, -2.28f, -2.82f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.37f, -0.64f)
                quadToRelative(-0.54f, -1.305f, -0.24f, -2.64f)
                curveToRelative(0.13f, -0.5f, 0.35f, -0.93f, 0.67f, -1.28f)
                close()
            }
            path(fill = SolidColor(Color(0xFF231F20))) {
                moveTo(17.33f, 28.14f)
                horizontalLineToRelative(0.02f)
                lineToRelative(4.05f, 5.31f)
                lineToRelative(-1.13f, 1.29f)
                horizontalLineToRelative(-0.02f)
                lineToRelative(-4.05f, -5.31f)
                close()
            }
            path(fill = SolidColor(Color(0xFF231F20))) {
                moveTo(19.7f, 25.5f)
                horizontalLineToRelative(0.02f)
                lineToRelative(1.54f, 1.83f)
                lineToRelative(0.94f, -1.06f)
                curveToRelative(0.2f, -0.29f, 0.32f, -0.49f, 0.36f, -0.61f)
                curveToRelative(0.08f, -0.15f, 0.12f, -0.31f, 0.12f, -0.5f)
                quadToRelative(0.045f, -0.525f, -0.24f, -0.93f)
                lineToRelative(-0.79f, -0.94f)
                lineToRelative(0.84f, -0.95f)
                horizontalLineToRelative(0.02f)
                curveToRelative(0.52f, 0.6f, 0.82f, 0.97f, 0.91f, 1.12f)
                curveToRelative(0.11f, 0.17f, 0.21f, 0.4f, 0.29f, 0.7f)
                curveToRelative(0.08f, 0.38f, 0.11f, 0.7f, 0.07f, 0.96f)
                curveToRelative(0f, 0.18f, -0.05f, 0.41f, -0.13f, 0.69f)
                curveToRelative(-0.03f, 0.1f, -0.06f, 0.19f, -0.1f, 0.27f)
                curveToRelative(0.14f, -0.07f, 0.32f, -0.13f, 0.54f, -0.19f)
                curveToRelative(0.63f, -0.12f, 1.17f, 0f, 1.62f, 0.34f)
                lineToRelative(0.02f, 0.02f)
                curveToRelative(0.16f, 0.12f, 0.39f, 0.38f, 0.71f, 0.77f)
                curveToRelative(0.2f, 0.23f, 0.31f, 0.35f, 0.31f, 0.37f)
                lineToRelative(-0.85f, 0.95f)
                curveToRelative(-0.46f, -0.54f, -0.73f, -0.86f, -0.83f, -0.96f)
                lineToRelative(-0.07f, -0.06f)
                quadToRelative(-0.36f, -0.285f, -0.87f, -0.18f)
                curveToRelative(-0.08f, 0.01f, -0.17f, 0.06f, -0.29f, 0.14f)
                curveToRelative(-0.06f, 0.02f, -0.22f, 0.14f, -0.48f, 0.35f)
                lineToRelative(-0.96f, 1.08f)
                lineToRelative(1.55f, 1.84f)
                lineToRelative(-0.85f, 0.95f)
                horizontalLineToRelative(-0.02f)
                lineToRelative(-4.27f, -5.05f)
                lineToRelative(0.85f, -0.97f)
                close()
            }
            path(fill = SolidColor(Color(0xFF231F20))) {
                moveTo(24.63f, 19.93f)
                horizontalLineToRelative(0.02f)
                curveToRelative(1.36f, 1.67f, 2.11f, 2.59f, 2.25f, 2.75f)
                lineToRelative(0.18f, 0.15f)
                curveToRelative(0.33f, 0.25f, 0.69f, 0.36f, 1.09f, 0.33f)
                curveToRelative(0.28f, -0.04f, 0.53f, -0.18f, 0.73f, -0.41f)
                curveToRelative(0.42f, -0.47f, 0.5f, -1.09f, 0.25f, -1.88f)
                arcToRelative(2.4f, 2.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.26f, -0.51f)
                curveToRelative(-0.12f, -0.16f, -0.84f, -1.05f, -2.17f, -2.67f)
                curveToRelative(-0.03f, -0.04f, -0.04f, -0.06f, -0.04f, -0.07f)
                lineToRelative(0.98f, -1.1f)
                horizontalLineToRelative(0.02f)
                curveToRelative(1.4f, 1.71f, 2.15f, 2.64f, 2.26f, 2.79f)
                curveToRelative(0.59f, 0.85f, 0.82f, 1.81f, 0.7f, 2.91f)
                curveToRelative(-0.09f, 0.66f, -0.35f, 1.24f, -0.78f, 1.72f)
                lineToRelative(-0.02f, 0.02f)
                curveToRelative(-0.72f, 0.81f, -1.63f, 1.03f, -2.73f, 0.67f)
                curveToRelative(-0.28f, -0.11f, -0.54f, -0.25f, -0.76f, -0.42f)
                lineToRelative(-0.06f, -0.05f)
                curveToRelative(-0.24f, -0.19f, -0.55f, -0.52f, -0.91f, -1f)
                lineToRelative(-1.75f, -2.14f)
                lineToRelative(0.98f, -1.11f)
                close()
            }
        }.build()

        return _Bookmarks!!
    }

@Suppress("ObjectPropertyName")
private var _Bookmarks: ImageVector? = null
