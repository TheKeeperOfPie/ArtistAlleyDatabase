package com.thekeeperofpie.artistalleydatabase.anilist.media

import kotlinx.serialization.Serializable

/**
 * For storing a JSON encoded reference to an [MediaEntry] inside a SQLite column.
 */
@Serializable
data class MediaColumnEntry(
    val id: String,
    val title: String = "",
)