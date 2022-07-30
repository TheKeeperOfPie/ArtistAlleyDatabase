package com.thekeeperofpie.artistalleydatabase.search

data class SearchQueryWrapper(
    val value: String = "",
    val includeArtists: Boolean = false,
    val includeSources: Boolean = false,
    val includeSeries: Boolean = false,
    val includeCharacters: Boolean = false,
    val includeTags: Boolean = false,
    val includeNotes: Boolean = false,
    val includeOther: Boolean = false,
    val locked: Boolean = false,
    val unlocked: Boolean = false,
) {
    val includeAll = !includeArtists && !includeSources && !includeSeries && !includeCharacters
            && !includeTags && !includeNotes && !includeOther && !locked && !unlocked
}