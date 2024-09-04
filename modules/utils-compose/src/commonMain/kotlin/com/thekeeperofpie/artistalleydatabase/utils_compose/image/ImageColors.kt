package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import coil3.Image
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.set

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
    fun calculatePalette(
        id: String,
        image: Image?,
        heightStartThreshold: Float = 0f,
        widthEndThreshold: Float = 1f,
        selectMaxPopulation: Boolean = false,
    ) {
        if (shouldCalculate(id)) {
            if (image == null || !PlatformPalette.canProcess(image)) return
            scope.launch(CustomDispatchers.IO) {
                try {
                    val palette = PlatformPalette.fromCollResult(
                        image = image,
                        heightStartThreshold = heightStartThreshold,
                        widthEndThreshold = widthEndThreshold,
                        selectMaxPopulation = selectMaxPopulation,
                    )
                    val swatch = palette?.select(
                        selectMaxPopulation = selectMaxPopulation,
                        isDarkMode = isDarkMode,
                    )
                    if (swatch != null) {
                        setColor(
                            id = id,
                            containerColor = swatch.color,
                            textColor = swatch.bodyTextColor.copy(alpha = 1f)
                        )
                    }
                } catch (ignored: Exception) {
                }
            }
        }
    }
}
