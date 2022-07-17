package com.thekeeperofpie.artistalleydatabase.anilist

import com.squareup.moshi.JsonClass
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry

/**
 * For storing a JSON encoded reference to an [CharacterEntry] inside a SQLite column.
 */
@JsonClass(generateAdapter = true)
data class CharacterColumnEntry(
    val id: Int,
    val name: Name? = null,
) {
    @JsonClass(generateAdapter = true)
    data class Name(
        val first: String? = null,
        val middle: String? = null,
        val last: String? = null,
        val full: String? = null,
        val native: String? = null,
    )
}