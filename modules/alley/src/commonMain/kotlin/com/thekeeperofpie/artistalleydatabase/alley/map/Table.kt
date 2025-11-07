package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.compose.ui.graphics.Color
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

sealed interface Table {
    val year: DataYear
    val booth: String
    val favorite: Boolean
    val gridX: Int
    val gridY: Int
    val section: AnimeExpoSection?
    val image: CatalogImage?

    data class Single(
        override val year: DataYear,
        val artistId: String,
        override val booth: String,
        override val section: AnimeExpoSection?,
        override val image: CatalogImage?,
        val imageIndex: Int?,
        override val favorite: Boolean,
        override val gridX: Int,
        override val gridY: Int,
    ) : Table

    data class Shared(
        override val year: DataYear,
        val artistIds: List<String>,
        override val booth: String,
        override val section: AnimeExpoSection?,
        override val image: CatalogImage?,
        val imageIndex: Int?,
        override val favorite: Boolean,
        override val gridX: Int,
        override val gridY: Int,
    ) : Table

    enum class AnimeExpoSection(val range: IntRange, val color: Color, val textColor: Color) {
        MAX(0..22, Color(0xFFFDD6D9), Color.Black),
        KISEGI(23..41, Color(0xFFC7DBE6), Color.Black),
        MAHOKO(42..Int.MAX_VALUE, Color(0xFFD5C1DD), Color.Black),
        ;
        companion object {
            fun fromTableNumber(number: Int) = AnimeExpoSection.entries.first { number in it.range }
        }
    }
}
