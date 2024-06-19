package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

class StampRallySearchQuery(
    val fandom: String?,
    val tables: String?,
    val sortOption: StampRallySearchSortOption,
    val sortAscending: Boolean,
    val showOnlyFavorites: Boolean,
    val showIgnored: Boolean,
    val showOnlyIgnored: Boolean,
    val randomSeed: Int,
)
