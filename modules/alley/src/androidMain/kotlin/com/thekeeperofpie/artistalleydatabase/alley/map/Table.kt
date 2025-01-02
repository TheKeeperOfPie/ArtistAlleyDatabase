package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.compose.ui.graphics.Color
import com.thekeeperofpie.artistalleydatabase.alley.CatalogImage

data class Table(
    val booth: String,
    val section: Section,
    val image: CatalogImage?,
    val imageIndex: Int?,
    val favorite: Boolean,
    val gridX: Int,
    val gridY: Int,
) {
    enum class Section(val range: IntRange, val color: Color, val textColor: Color) {
        MAX(0..22, Color(0xFFFDD6D9), Color.Black),
        KISEGI(20..40, Color(0xFFC7DBE6), Color.Black),
        MAHOKO(41..Int.MAX_VALUE, Color(0xFFD5C1DD), Color.Black),
        ;
        companion object {
            fun fromTableNumber(number: Int) = Section.entries.first { number in it.range }
        }
    }
}
