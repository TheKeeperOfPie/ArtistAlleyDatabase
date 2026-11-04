package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterPersistentState
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterSaveableState
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings

class SeriesSortFilterController(
    settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
) {
    private val persistentState = SeriesSortFilterPersistentState(
        sortFilterSettings = settings,
    )

    private val saveableState by savedStateHandle.saved {
        SeriesSortFilterSaveableState(
            _showOnlyConfirmedTags = mutableStateOf(settings.showOnlyConfirmedTags),
        )
    }

    val state = SeriesSortFilterState(
        persistentState = persistentState,
        saveableState = saveableState,
    )

    internal val filterParams by derivedStateOf {
        FilterParams(
            sortOption = persistentState.sortFilterSettings.seriesSortOption,
            sortAscending = persistentState.sortFilterSettings.seriesSortAscending,
            sourceIn = saveableState.sourceIn.filterIn.toSet(),
            showOnlyConfirmedTags = saveableState.showOnlyConfirmedTags,
        )
    }

    data class FilterParams(
        val sortOption: SeriesSearchSortOption,
        val sortAscending: Boolean,
        val sourceIn: Set<SeriesFilterOption>,
        val showOnlyConfirmedTags: Boolean,
    )
}
