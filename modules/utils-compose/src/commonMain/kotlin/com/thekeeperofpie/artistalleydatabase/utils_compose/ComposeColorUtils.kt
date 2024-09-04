package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import com.github.ajalt.colormath.calculate.wcagContrastRatio
import com.github.ajalt.colormath.extensions.android.composecolor.toColormathColor
import com.github.ajalt.colormath.extensions.android.composecolor.toComposeColor
import com.github.ajalt.colormath.parse

object ComposeColorUtils {

    private val white = Color.White.toColormathColor()
    private val black = Color.Black.toColormathColor()

    fun bestTextColor(background: Color): Color? = try {
        val backgroundColor = background.toColormathColor()
        val contrastWhite = white.wcagContrastRatio(backgroundColor)
        val contrastBlack = black.wcagContrastRatio(backgroundColor)
        if (contrastWhite >= contrastBlack) Color.White else Color.Black
    } catch (ignored: Exception) {
        null
    }

    fun hexToColor(value: String) = try {
        com.github.ajalt.colormath.Color.parse(value).toComposeColor()
    } catch (ignored: Exception) {
        null
    }
}

fun Color.multiplyCoerceSaturation(
    @FloatRange(from = 0.0, to = 1.0) multiplier: Float,
    maxSaturation: Float = 1f,
): Color {
    val hsl = toColormathColor().toHSL()
    return hsl.copy(s = (hsl.s * multiplier).coerceAtMost(maxSaturation)).toComposeColor()
}

fun Color.withSaturation(saturation: Float = 1f): Color {
    val hsl = toColormathColor().toHSL()
    return hsl.copy(s = saturation).toComposeColor()
}
