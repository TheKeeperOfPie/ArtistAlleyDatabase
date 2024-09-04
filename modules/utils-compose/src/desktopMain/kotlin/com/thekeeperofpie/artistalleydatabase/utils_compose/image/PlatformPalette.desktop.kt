package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.BitmapImage
import coil3.Image
import com.kmpalette.bodyTextColor
import com.kmpalette.color
import com.kmpalette.get
import com.kmpalette.palette.graphics.Palette
import com.kmpalette.palette.graphics.Target

actual class PlatformPalette(private val palette: Palette) {
    actual companion object {
        actual fun canProcess(image: Image) = (image as? BitmapImage)?.bitmap != null
        actual fun fromCollResult(
            image: Image,
            heightStartThreshold: Float,
            widthEndThreshold: Float,
            selectMaxPopulation: Boolean,
        ): PlatformPalette? {
            val bitmap = (image as? BitmapImage)?.bitmap?.asComposeImageBitmap() ?: return null
            return PlatformPalette(Palette.from(bitmap)
                .setRegion(
                    0,
                    (bitmap.height * heightStartThreshold).toInt(),
                    (bitmap.width * widthEndThreshold).toInt(),
                    bitmap.height
                )
                .run { if (selectMaxPopulation) clearFilters() else this }
                .generate()
            )
        }
    }

    actual fun select(
        selectMaxPopulation: Boolean,
        isDarkMode: Boolean,
    ): PlatformPaletteSwatch? {
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
        ?: return null
        return PlatformPaletteSwatch(bodyTextColor = swatch.bodyTextColor(), color = swatch.color)
    }
}
