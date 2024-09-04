package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import androidx.compose.ui.graphics.Color
import coil3.Image

expect class PlatformPalette {
    companion object {
        fun canProcess(image: Image): Boolean
        fun fromCollResult(
            image: Image,
            heightStartThreshold: Float = 0f,
            widthEndThreshold: Float = 1f,
            selectMaxPopulation: Boolean,
        ): PlatformPalette?
    }

    fun select(selectMaxPopulation: Boolean, isDarkMode: Boolean): PlatformPaletteSwatch?
}

data class PlatformPaletteSwatch(val bodyTextColor: Color, val color: Color)
