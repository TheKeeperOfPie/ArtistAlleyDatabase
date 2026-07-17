package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.github.ajalt.colormath.extensions.android.composecolor.toColormathColor
import com.github.ajalt.colormath.extensions.android.composecolor.toComposeColor
import com.github.ajalt.colormath.model.HSL
import com.github.ajalt.colormath.parse
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object ComposeColorUtils {

    fun calculateContrast(color1: Color, color2: Color): Double {
        val l1 = color1.luminance()
        val l2 = color2.luminance()
        return (max(l1, l2) + 0.05) / (min(l1, l2) + 0.05)
    }

    fun bestTextColor(backgroundColor: Color): Color {
        val whiteContrast = calculateContrast(Color.White, backgroundColor)
        val blackContrast = calculateContrast(Color.Black, backgroundColor)
        return if (whiteContrast > blackContrast) Color.White else Color.Black
    }

    fun hexToColor(value: String) = try {
        com.github.ajalt.colormath.Color.parse(value).toComposeColor()
    } catch (_: Exception) {
        null
    }

    fun derivedColor(input: Any) =
        HSL(Random(input.hashCode()).nextDouble(360.0), 0.7f, 0.5f)
            .toComposeColor()
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
