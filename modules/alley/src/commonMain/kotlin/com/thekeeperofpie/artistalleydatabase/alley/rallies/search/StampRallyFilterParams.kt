package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData

data class StampRallyFilterParams(
    val sortOption: StampRallySearchSortOption,
    val sortAscending: Boolean,
    val seriesIn: Set<String>,
    val merchIdIn: Set<String>,
    val prizeMerchIdIn: Set<String>,
    val totalCost: RangeData,
    val prizeLimit: RangeData,
    val hideFavorited: Boolean,
    val hideIgnored: Boolean,
) {
    companion object {
        fun unfiltered(lockedSeries: String? = null) = StampRallyFilterParams(
            sortOption = StampRallySearchSortOption.RANDOM,
            sortAscending = true,
            seriesIn = setOfNotNull(lockedSeries),
            merchIdIn = emptySet(),
            prizeMerchIdIn = emptySet(),
            totalCost = RangeData(100),
            prizeLimit = RangeData(50),
            hideFavorited = false,
            hideIgnored = false,
        )
    }
}
