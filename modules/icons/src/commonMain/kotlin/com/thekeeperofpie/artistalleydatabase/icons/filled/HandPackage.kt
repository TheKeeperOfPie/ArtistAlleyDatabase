package com.thekeeperofpie.artistalleydatabase.icons.filled

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.materialPath

val Icons.Filled.HandPackage: ImageVector
    get() {
        if (_handPackage != null) return _handPackage!!

        _handPackage = ImageVector.Builder(
            name = "Filled.HandPackage",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            materialPath {
                moveTo(360f, 440f)
                quadToRelative(-34f, 0f, -57f, -23f)
                reflectiveQuadToRelative(-23f, -57f)
                verticalLineToRelative(-200f)
                quadToRelative(0f, -33f, 23f, -56.5f)
                reflectiveQuadToRelative(57f, -23.5f)
                horizontalLineToRelative(240f)
                verticalLineToRelative(360f)
                horizontalLineTo(360f)
                close()
                moveToRelative(320f, 0f)
                verticalLineToRelative(-360f)
                horizontalLineToRelative(120f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 160f)
                verticalLineToRelative(200f)
                quadToRelative(0f, 34f, -23.5f, 57f)
                reflectiveQuadTo(800f, 440f)
                horizontalLineTo(680f)
                close()
                moveTo(320f, 840f)
                verticalLineToRelative(-320f)
                horizontalLineToRelative(272f)
                quadToRelative(14f, 0f, 24f, 8f)
                reflectiveQuadToRelative(14f, 19f)
                quadToRelative(4f, 11f, 1.5f, 23f)
                reflectiveQuadTo(618f, 591f)
                lineToRelative(-59f, 49f)
                horizontalLineTo(400f)
                verticalLineToRelative(60f)
                horizontalLineToRelative(180f)
                lineToRelative(162f, -134f)
                quadToRelative(22f, -16f, 46.5f, -20f)
                reflectiveQuadToRelative(48.5f, 2f)
                quadToRelative(24f, 6f, 45.5f, 20.5f)
                reflectiveQuadTo(919f, 606f)
                lineTo(684f, 803f)
                quadToRelative(-22f, 18f, -48f, 27.5f)
                reflectiveQuadToRelative(-54f, 9.5f)
                horizontalLineTo(320f)
                close()
                moveTo(120f, 880f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(80f, 840f)
                verticalLineToRelative(-280f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(120f, 520f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(320f)
                quadToRelative(0f, 17f, -11.5f, 28.5f)
                reflectiveQuadTo(200f, 880f)
                horizontalLineToRelative(-80f)
                close()
            }
        }.build()

        return _handPackage!!
    }

private var _handPackage: ImageVector? = null
