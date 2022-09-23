package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import kotlinx.serialization.Serializable

@Serializable
data class DiscEntry(
    val name: String,
    val duration: String,

    /** Encoded list of [TrackEntry] objects */
    val tracks: List<String>
)