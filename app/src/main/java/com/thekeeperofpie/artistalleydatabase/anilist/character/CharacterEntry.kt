package com.thekeeperofpie.artistalleydatabase.anilist.character

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anilist.fragment.AniListCharacter
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity("character_entries")
data class CharacterEntry(
    @PrimaryKey
    val id: Int,
    @Embedded(prefix = "name_")
    val name: Name? = null,
    @Embedded(prefix = "image_")
    val image: Image? = null,
    val mediaIds: List<Int>?,
) {
    companion object {
        fun from(character: AniListCharacter) = CharacterEntry(
            id = character.id,
            name = Name(
                first = character.name?.first,
                middle = character.name?.middle,
                last = character.name?.last,
                full = character.name?.full,
                native = character.name?.native,
                alternative = character.name?.alternative?.filterNotNull(),
            ),
            image = Image(
                large = character.image?.large,
                medium = character.image?.medium,
            ),
            mediaIds = character.media?.nodes?.mapNotNull { it?.aniListMedia?.id }
        )
    }

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