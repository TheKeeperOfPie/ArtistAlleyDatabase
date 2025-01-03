package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import coil3.BitmapImage
import coil3.Image

// TODO
actual class PlatformPalette {
    actual companion object {
        actual fun canProcess(image: Image) = (image as? BitmapImage)?.bitmap != null
        actual fun fromCollResult(
            image: Image,
            heightStartThreshold: Float,
            widthEndThreshold: Float,
            selectMaxPopulation: Boolean,
        ): PlatformPalette? = null
    }

    actual fun select(
        selectMaxPopulation: Boolean,
        isDarkMode: Boolean,
    ): PlatformPaletteSwatch? = null
}
