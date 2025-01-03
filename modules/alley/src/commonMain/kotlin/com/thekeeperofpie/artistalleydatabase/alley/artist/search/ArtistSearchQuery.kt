package com.thekeeperofpie.artistalleydatabase.alley.artist.search

data class ArtistSearchQuery(
    val filterParams: ArtistSortFilterViewModel.FilterParams,
    val randomSeed: Int,
    val lockedSeries: String?,
    val lockedMerch: String?,
)
