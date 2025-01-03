package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

data class StampRallySearchQuery(
    val filterParams: StampRallySortFilterViewModel.FilterParams,
    val randomSeed: Int,
)
