package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.alley.search.ArtistAlleySearchSortOption

data class ArtistSearchQuery(
    val booth: String?,
    val artist: String?,
    val description: String?,
    val series: List<String>,
    val seriesById: List<String>,
    val sortOption: ArtistAlleySearchSortOption,
    val sortAscending: Boolean,
    val showOnlyFavorites: Boolean,
    val showOnlyWithCatalog: Boolean,
    val showIgnored: Boolean,
    val showOnlyIgnored: Boolean,
    val randomSeed: Int,
)
