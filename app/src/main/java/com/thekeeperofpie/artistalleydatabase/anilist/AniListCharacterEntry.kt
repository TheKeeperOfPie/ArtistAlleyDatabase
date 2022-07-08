package com.thekeeperofpie.artistalleydatabase.anilist

import com.anilist.CharactersSearchQuery
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Field names are obfuscated so that they are less likely to show up in SQLite search
@JsonClass(generateAdapter = true)
data class AniListCharacterEntry(
    @Json(name = "abcdef0") val id: Int,
    @Json(name = "abcdef1") val name: CharactersSearchQuery.Name,
)