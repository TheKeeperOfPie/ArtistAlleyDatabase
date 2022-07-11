package com.thekeeperofpie.artistalleydatabase.anilist.media

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anilist.fragment.AniListMedia
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity("media_entries")
data class MediaEntry(
    @PrimaryKey
    val id: Int,
    @Embedded(prefix = "title_")
    val title: Title? = null,
    val type: Type? = null,
    @Embedded(prefix = "image_")
    val image: CoverImage? = null,
    val synonyms: List<String>? = null
) {
    companion object {
        fun from(media: AniListMedia) = MediaEntry(
            id = media.id,
            title = Title(
                romaji = media.title?.romaji?.trim(),
                english = media.title?.english?.trim(),
                native = media.title?.native?.trim(),
            ),
            type = media.type?.rawValue?.let(Type::valueOf),
            image = CoverImage(
                extraLarge = media.coverImage?.extraLarge,
                large = media.coverImage?.large,
                medium = media.coverImage?.medium,
                color = media.coverImage?.color,
            ),
            synonyms = media.synonyms?.filterNotNull()?.map(String::trim),
        )
    }

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