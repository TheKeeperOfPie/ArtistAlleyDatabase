package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchQuery

data class ArtistSearchQuery(
    override val query: String = "",
    val includeBooth: Boolean = false,
    val includeTableName: Boolean = false,
    val includeArtistNames: Boolean = false,
    val includeRegion: Boolean = false,
    val includeDescription: Boolean = false,
) : EntrySearchQuery {
    val includeAll = !includeBooth && !includeTableName && !includeArtistNames
            && !includeRegion && !includeDescription
}
