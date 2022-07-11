package com.thekeeperofpie.artistalleydatabase.anilist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatabaseSeriesEntry(
    val id: Int,
    val title: String = "",
)