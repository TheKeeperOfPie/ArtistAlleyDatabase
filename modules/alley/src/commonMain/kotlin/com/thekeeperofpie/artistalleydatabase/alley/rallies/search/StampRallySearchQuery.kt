package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

data class StampRallySearchQuery(
    val series: String?,
    val filterParams: StampRallySortFilterViewModel.FilterParams,
    val randomSeed: Int,
)
