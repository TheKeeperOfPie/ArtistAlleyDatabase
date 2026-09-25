package com.thekeeperofpie.artistalleydatabase.alley.artist.search

data class ArtistSearchQuery(
    val filterParams: ArtistSortFilterParams,
    val randomSeed: Int,
)
