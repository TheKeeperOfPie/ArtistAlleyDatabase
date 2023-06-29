package com.thekeeperofpie.artistalleydatabase.alley.search

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class ArtistAlleySearchSortOption(@StringRes override val textRes: Int) : SortOption {

    BOOTH(R.string.alley_sort_booth),
    TABLE(R.string.alley_sort_table),
    ARTIST(R.string.alley_sort_artist),
}
