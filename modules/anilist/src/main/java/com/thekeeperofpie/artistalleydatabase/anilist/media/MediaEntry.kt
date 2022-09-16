package com.thekeeperofpie.artistalleydatabase.anilist.media

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity("media_entries")
data class MediaEntry(
    @PrimaryKey
    val id: String,
    @Embedded(prefix = "title_")
    val title: Title? = null,
    val type: Type? = null,
    @Embedded(prefix = "image_")
    val image: CoverImage? = null,
    val synonyms: List<String>? = null
) {

    data class Title(
        val romaji: String? = null,
        val english: String? = null,
        val native: String? = null,
    )

    enum class Type {
        ANIME, MANGA
    }

    data class CoverImage(
        val extraLarge: String? = null,
        val large: String? = null,
        val medium: String? = null,
        val color: String? = null,
    )
}