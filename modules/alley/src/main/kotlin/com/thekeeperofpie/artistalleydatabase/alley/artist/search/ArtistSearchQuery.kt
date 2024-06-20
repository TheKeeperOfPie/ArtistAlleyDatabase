package com.thekeeperofpie.artistalleydatabase.alley.artist.search

data class ArtistSearchQuery(
    val booth: String?,
    val artist: String?,
    val summary: String?,
    val series: List<String>,
    val seriesById: List<String>,
    val merch: List<String>,
    val sortOption: ArtistSearchSortOption,
    val sortAscending: Boolean,
    val showOnlyFavorites: Boolean,
    val showOnlyWithCatalog: Boolean,
    val showIgnored: Boolean,
    val showOnlyIgnored: Boolean,
    val randomSeed: Int,
    val lockedSeries: String?,
    val lockedMerch: String?,
)
