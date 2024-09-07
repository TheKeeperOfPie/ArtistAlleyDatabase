package com.thekeeperofpie.artistalleydatabase.animethemes.models

import kotlinx.serialization.Serializable

@Serializable
data class AnimeThemeEntry(
    val id: String,
    val tag: String? = null,
    val version: String? = null,
    val episodes: String? = null,
    val nsfw: Boolean = false,
    val spoiler: Boolean = false,
    val notes: String? = null,
    val videos: List<Video> = emptyList(),
) {
    @Serializable
    data class Video(
        val id: String,
        val link: String? = null,
        val audio: Audio? = null,
    ) {
        @Serializable
        data class Audio(
            val id: String,
            val link: String? = null,
        )
    }
}
