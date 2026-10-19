package com.thekeeperofpie.artistalleydatabase.alley.series.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption

interface SeriesSortFilterSettings {
    var seriesSortOption: SeriesSearchSortOption
    var seriesSortAscending: Boolean

    companion object {
        @Composable
        fun rememberForPreview() = object : SeriesSortFilterSettings {
            override var seriesSortOption by mutableStateOf(SeriesSearchSortOption.RANDOM)
            override var seriesSortAscending by mutableStateOf(true)
        }
    }
}
