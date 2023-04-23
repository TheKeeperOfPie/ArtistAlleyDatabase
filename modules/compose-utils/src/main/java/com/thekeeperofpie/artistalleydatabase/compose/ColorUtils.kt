package com.thekeeperofpie.artistalleydatabase.compose

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

object ColorUtils {

    fun bestTextColor(background: Color): Color? = try {
        val backgroundArgb = background.toArgb()
        val contrastWhite = ColorUtils.calculateContrast(Color.White.toArgb(), backgroundArgb)
        val contrastBlack = ColorUtils.calculateContrast(Color.Black.toArgb(), backgroundArgb)
        if (contrastWhite >= contrastBlack) Color.White else Color.Black
    } catch (ignored: Exception) {
        null
    }

    fun hexToColor(value: String) = try {
        Color(android.graphics.Color.parseColor(value))
    } catch (ignored: Exception) {
        null
    }
}

fun Color.multiplyCoerceSaturation(
    @FloatRange(from = 0.0, to = 1.0) multiplier: Float,
    maxSaturation: Float = 1f,
): Color {
    val array = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), array)
    return Color.hsl(array[0], (array[1] * multiplier).coerceAtMost(maxSaturation), array[2], alpha)
}
