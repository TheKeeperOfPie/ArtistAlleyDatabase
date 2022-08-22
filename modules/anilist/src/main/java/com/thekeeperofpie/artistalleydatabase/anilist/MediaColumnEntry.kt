package com.thekeeperofpie.artistalleydatabase.anilist

import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import kotlinx.serialization.Serializable

/**
 * For storing a JSON encoded reference to an [MediaEntry] inside a SQLite column.
 */
@Serializable
data class MediaColumnEntry(
    val id: Int,
    val title: String = "",
)