package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption

enum class StampRallySearchSortOption(@StringRes override val textRes: Int) : SortOption {

    MAIN_TABLE(R.string.alley_sort_booth),
    FANDOM(R.string.alley_sort_fandom),
    RANDOM(R.string.alley_sort_random),
    PRIZE_LIMIT(R.string.alley_sort_prize_limit),
    TOTAL_COST(R.string.alley_sort_total_cost),
}
