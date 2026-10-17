package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

data class StampRallySearchQuery(
    val query: String,
    val filterParams: StampRallyFilterParams,
    val randomSeed: Int,
)
