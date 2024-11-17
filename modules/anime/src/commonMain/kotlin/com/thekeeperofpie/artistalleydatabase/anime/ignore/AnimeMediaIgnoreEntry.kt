package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anilist.data.type.MediaType

@Entity("anime_media_ignore")
data class AnimeMediaIgnoreEntry(
    @PrimaryKey
    val id: String,
    val type: MediaType?,
    val isAdult: Boolean? = null,
    val bannerImage: String? = null,
    val coverImage: String? = null,
    @Embedded(prefix = "title_")
    val title: Title,
    val viewedAt: Long = -1L,
) {
    data class Title(
        val romaji: String? = null,
        val english: String? = null,
        val native: String? = null,
    )
}
