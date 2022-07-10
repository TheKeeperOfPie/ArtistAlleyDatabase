package com.thekeeperofpie.artistalleydatabase.anilist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AniListSeriesEntry(
    val id: Int,
    val title: String,
)