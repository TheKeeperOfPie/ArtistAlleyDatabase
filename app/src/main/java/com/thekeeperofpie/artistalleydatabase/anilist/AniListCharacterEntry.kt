package com.thekeeperofpie.artistalleydatabase.anilist

import com.anilist.CharactersSearchQuery
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AniListCharacterEntry(
    val id: Int,
    val name: CharactersSearchQuery.Name,
)