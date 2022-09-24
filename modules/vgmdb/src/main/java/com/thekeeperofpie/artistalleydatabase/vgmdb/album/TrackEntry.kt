package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import kotlinx.serialization.Serializable

@Serializable
data class TrackEntry(
    val number: String,

    /** Map of language to title in that language */
    val titles: Map<String, String>,
    val duration: String,
) {
    val title
        get() = titles["en"] ?: titles["ja"] ?: titles.values.firstOrNull() ?: ""
}