package com.thekeeperofpie.artistalleydatabase.alley.artist.search

data class ArtistSearchQuery(
    val filterParams: ArtistSortFilterController.FilterParams,
    val randomSeed: Int,
    val seriesIn: Set<String> = emptySet(),
    val merchIn: Set<String> = emptySet(),
)
