package com.thekeeperofpie.artistalleydatabase.search

data class SearchQueryWrapper(
    val value: String = "",
    val includeArtists: Boolean = true,
    val includeSources: Boolean = true,
    val includeSeries: Boolean = true,
    val includeCharacters: Boolean = true,
    val includeTags: Boolean = true,
    val includeNotes: Boolean = true,
    val includeOther: Boolean = true,
    val locked: Boolean = true,
    val unlocked: Boolean = true,
)