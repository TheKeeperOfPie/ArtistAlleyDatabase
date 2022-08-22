package com.thekeeperofpie.artistalleydatabase.anilist

import com.squareup.moshi.JsonClass
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry

/**
 * For storing a JSON encoded reference to an [MediaEntry] inside a SQLite column.
 */
@JsonClass(generateAdapter = true)
data class MediaColumnEntry(
    val id: Int,
    val title: String = "",
)