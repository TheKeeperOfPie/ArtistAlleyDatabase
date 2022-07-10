package com.thekeeperofpie.artistalleydatabase.anilist

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatabaseCharacterEntry(
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