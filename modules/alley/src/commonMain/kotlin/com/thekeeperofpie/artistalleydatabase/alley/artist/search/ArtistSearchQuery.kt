package com.thekeeperofpie.artistalleydatabase.alley.artist.search

data class ArtistSearchQuery(
    val query: String,
    val filterParams: ArtistSortFilterParams,
    val randomSeed: Int,
)
