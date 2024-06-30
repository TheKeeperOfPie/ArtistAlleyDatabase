package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

data class StampRallySearchQuery(
    val filterParams: StampRallySortFilterController.FilterParams,
    val randomSeed: Int,
)
