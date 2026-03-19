package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun rememberTintPainter(painter: Painter, tintColor: Color = LocalContentColor.current) =
    remember(painter, tintColor) { TintPainter(painter, tintColor) }

class TintPainter(private val painter: Painter, tintColor: Color) : Painter() {
    override val intrinsicSize: Size
        get() = painter.intrinsicSize

    private val colorFilter = ColorFilter.tint(tintColor)

    override fun DrawScope.onDraw() {
        with(painter) {
            draw(size = size, colorFilter = colorFilter)
        }
    }
}
