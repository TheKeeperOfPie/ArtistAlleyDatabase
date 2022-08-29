package com.thekeeperofpie.artistalleydatabase.art.search

import com.thekeeperofpie.artistalleydatabase.form.search.EntrySearchQuery

data class ArtSearchQuery(
    override val query: String = "",
    val includeArtists: Boolean = false,
    val includeSources: Boolean = false,
    val includeSeries: Boolean = false,
    val includeCharacters: Boolean = false,
    val includeTags: Boolean = false,
    val includeNotes: Boolean = false,
    val includeOther: Boolean = false,
    val locked: Boolean = false,
    val unlocked: Boolean = false,
) : EntrySearchQuery {
    val includeAll = !includeArtists && !includeSources && !includeSeries && !includeCharacters
            && !includeTags && !includeNotes && !includeOther && !locked && !unlocked
}