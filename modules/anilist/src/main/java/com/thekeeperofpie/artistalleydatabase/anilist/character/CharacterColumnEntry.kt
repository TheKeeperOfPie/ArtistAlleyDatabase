package com.thekeeperofpie.artistalleydatabase.anilist.character

import kotlinx.serialization.Serializable

/**
 * For storing a JSON encoded reference to an [CharacterEntry] inside a SQLite column.
 */
@Serializable
data class CharacterColumnEntry(
    val id: String,
    val name: Name? = null,
) {
    @Serializable
    data class Name(
        val first: String? = null,
        val middle: String? = null,
        val last: String? = null,
        val full: String? = null,
        val native: String? = null,
    )
}