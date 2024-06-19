package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class StampRallySearchSortOption(@StringRes override val textRes: Int) : SortOption {

    MAIN_TABLE(R.string.alley_sort_booth),
    FANDOM(R.string.alley_sort_fandom),
    RANDOM(R.string.alley_sort_random),
}
