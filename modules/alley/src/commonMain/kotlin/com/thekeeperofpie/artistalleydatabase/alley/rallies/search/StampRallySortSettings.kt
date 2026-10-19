package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface StampRallySortSettings {
    var stampRalliesSortOption: StampRallySearchSortOption
    var stampRalliesSortAscending: Boolean

    companion object {
        @Composable
        fun rememberForPreview() = object : StampRallySortSettings {
            override var stampRalliesSortOption by mutableStateOf(StampRallySearchSortOption.RANDOM)
            override var stampRalliesSortAscending by mutableStateOf(true)
        }
    }
}
