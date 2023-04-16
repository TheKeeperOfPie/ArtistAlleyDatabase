package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

object ColorUtils {

    fun bestTextColor(background: Color): Color {
        val backgroundArgb = background.toArgb()
        val contrastWhite = ColorUtils.calculateContrast(Color.White.toArgb(), backgroundArgb)
        val contrastBlack = ColorUtils.calculateContrast(Color.Black.toArgb(), backgroundArgb)
        return if (contrastWhite >= contrastBlack) Color.White else Color.Black
    }

    fun hexToColor(value: String) = try {
        Color(android.graphics.Color.parseColor(value))
    } catch (ignored: Exception) {
        null
    }
}