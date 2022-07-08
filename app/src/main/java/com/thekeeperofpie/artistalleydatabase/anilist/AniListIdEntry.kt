package com.thekeeperofpie.artistalleydatabase.anilist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AniListIdEntry(
    val id: Int,
    val displayText: String,
)