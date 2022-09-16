package com.thekeeperofpie.artistalleydatabase.anilist.character

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity("character_entries")
data class CharacterEntry(
    @PrimaryKey
    val id: String,
    @Embedded(prefix = "name_")
    val name: Name? = null,
    @Embedded(prefix = "image_")
    val image: Image? = null,
    val mediaIds: List<String>?,
) {

    data class Name(
        val first: String? = null,
        val middle: String? = null,
        val last: String? = null,
        val full: String? = null,
        val native: String? = null,
        val alternative: List<String>? = null,
    )

    data class Image(
        val large: String? = null,
        val medium: String? = null,
    )
}