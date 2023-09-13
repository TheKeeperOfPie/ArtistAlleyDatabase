package com.thekeeperofpie.artistalleydatabase.compose

import android.graphics.drawable.BitmapDrawable
import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Target
import androidx.palette.graphics.get
import coil.compose.AsyncImagePainter
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.launch

object ComposeColorUtils {

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


    /**
     * Requires non-hardware bitmaps.
     */
    fun calculatePalette(
        id: String,
        success: AsyncImagePainter.State.Success,
        colorCalculationState: ColorCalculationState,
        heightStartThreshold: Float = 0f,
        widthEndThreshold: Float = 1f,
        selectMaxPopulation: Boolean = false,
    ) {
        if (colorCalculationState.shouldCalculate(id)) {
            (success.result.drawable as? BitmapDrawable)?.bitmap?.let {
                colorCalculationState.scope.launch(CustomDispatchers.IO) {
                    try {
                        val palette = Palette.from(it)
                            .setRegion(
                                0,
                                (it.height * heightStartThreshold).toInt(),
                                (it.width * widthEndThreshold).toInt(),
                                it.height
                            )
                            .run {
                                if (selectMaxPopulation) clearFilters() else this
                            }
                            .generate()
                        val swatch = if (selectMaxPopulation) {
                            palette.swatches.maxByOrNull { it.population }
                        } else {
                            val target = if (colorCalculationState.isDarkMode) {
                                Target.DARK_VIBRANT
                            } else {
                                Target.LIGHT_VIBRANT
                            }
                            palette[target]
                        } ?: palette.swatches.firstOrNull()
                        if (swatch != null) {
                            colorCalculationState.setColor(
                                id = id,
                                containerColor = Color(swatch.rgb),
                                textColor = Color(
                                    ColorUtils.setAlphaComponent(
                                        swatch.bodyTextColor,
                                        0xFF
                                    )
                                )
                            )
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
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
