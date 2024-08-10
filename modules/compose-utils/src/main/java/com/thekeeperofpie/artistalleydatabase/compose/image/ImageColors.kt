package com.thekeeperofpie.artistalleydatabase.compose.image

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Target
import androidx.palette.graphics.get
import coil3.BitmapImage
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ImageColors(
    val containerColor: Color,
    val textColor: Color,
) {
    companion object {
        val DEFAULT = ImageColors(Color.Unspecified, Color.Unspecified)
    }
}

@Composable
fun rememberImageColorsState(): ImageColorsState {
    val scope = rememberCoroutineScope()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return remember(isSystemInDarkTheme) { ImageColorsState(scope, isSystemInDarkTheme) }
}

val LocalImageColorsState = staticCompositionLocalOf { ImageColorsState() }

class ImageColorsState @OptIn(DelicateCoroutinesApi::class) constructor(
    val scope: CoroutineScope = GlobalScope,
    val isDarkMode: Boolean = true,
) {
    private val colors = mutableStateMapOf<String, ImageColors>()

    @Composable
    fun getColors(id: String?) = if (id.isNullOrEmpty()) {
        ImageColors.DEFAULT
    } else {
        remember(id) {
            derivedStateOf { colors[id] ?: ImageColors.DEFAULT }
        }.value
    }.run { ImageColors(containerColor, textColor) }

    fun getColorsNonComposable(id: String?) = if (id.isNullOrEmpty()) {
        ImageColors.DEFAULT
    } else {
        colors[id] ?: ImageColors.DEFAULT
    }.run { ImageColors(containerColor, textColor) }

    @Composable
    fun allowHardware(id: String?) = if (id == null) {
        true
    } else {
        remember(id) {
            derivedStateOf { colors[id] != null }
        }.value
    }

    fun shouldCalculate(id: String?) = if (id == null) {
        false
    } else {
        !colors.containsKey(id)
    }

    suspend fun setColor(id: String, containerColor: Color, textColor: Color) {
        withContext(CustomDispatchers.Main) {
            if (!colors.containsKey(id)) {
                colors[id] = ImageColors(containerColor = containerColor, textColor = textColor)
            }
        }
    }

    /**
     * Requires non-hardware bitmaps.
     */
    @OptIn(ExperimentalCoilApi::class)
    fun calculatePalette(
        id: String,
        image: Image?,
        heightStartThreshold: Float = 0f,
        widthEndThreshold: Float = 1f,
        selectMaxPopulation: Boolean = false,
    ) {
        if (shouldCalculate(id)) {
            (image as? BitmapImage)?.bitmap?.let {
                scope.launch(CustomDispatchers.IO) {
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
                            val target = if (isDarkMode) {
                                Target.DARK_VIBRANT
                            } else {
                                Target.LIGHT_VIBRANT
                            }
                            palette[target]
                        } ?: palette.swatches.firstOrNull()
                        if (swatch != null) {
                            setColor(
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
