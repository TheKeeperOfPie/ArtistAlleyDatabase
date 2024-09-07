package com.thekeeperofpie.artistalleydatabase.animethemes.models

import kotlinx.serialization.Serializable

@Serializable
data class Anime(
    val id: String,
    val slug: String,
    val animethemes: List<AnimeTheme>,
)
