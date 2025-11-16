package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anilist.data.type.MediaType

@Entity("anime_media_history")
data class AnimeMediaHistoryEntry(
    @PrimaryKey
    val id: String,
    val type: MediaType?,
    val isAdult: Boolean? = null,
    val bannerImage: String? = null,
    val coverImage: String? = null,
    @Embedded(prefix = "title_")
    val title: AnimeMediaHistoryEntryTitle,
    val viewedAt: Long = -1L,
)

data class AnimeMediaHistoryEntryTitle(
    val romaji: String? = null,
    val english: String? = null,
    val native: String? = null,
)
