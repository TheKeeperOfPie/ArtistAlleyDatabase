package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import java.time.LocalDate

data class MediaEditData(
    val id: String?,
    val mediaId: String,
    val title: String,
    val image: String?,
    val type: MediaType?,
    val status: MediaListStatus?,
    val scoreRaw: Int?,
    val progress: Int?,
    val progressMax: Int,
    val repeat: Int?,
    val priority: Int?,
    val private: Boolean?,
    val startedAt: LocalDate?,
    val completedAt: LocalDate?,
    val updatedAt: Long?,
    val createdAt: Long?,

    // TODO: Notes, too much data to pass through navigation params, need to fetch async
    val notes: String? = null,
)
