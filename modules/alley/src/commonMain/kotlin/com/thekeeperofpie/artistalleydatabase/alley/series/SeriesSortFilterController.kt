package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterPersistentState
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterSaveableState
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import kotlinx.coroutines.CoroutineScope

class SeriesSortFilterController(
    scope: CoroutineScope,
    settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
) {
    var showOnlyConfirmedTags by savedStateHandle.saveable {
        mutableStateOf(settings.showOnlyConfirmedTags)
    }

    private val persistentState = SeriesSortFilterPersistentState(
        sortFilterSettings = settings,
        displaySettings = settings,
    )

    private val saveableState by savedStateHandle.saved { SeriesSortFilterSaveableState() }

    val state = SeriesSortFilterState(
        persistentState = persistentState,
        saveableState = saveableState,
    )

    internal val filterParams by transform(scope) {
        FilterParams(
            sortOption = persistentState.sortFilterSettings.seriesSortOption,
            sortAscending = persistentState.sortFilterSettings.seriesSortAscending,
            sourceIn = saveableState.sourceIn.filterIn.toSet(),
            showOnlyConfirmedTags = showOnlyConfirmedTags,
        )
    }

    data class FilterParams(
        val sortOption: SeriesSearchSortOption,
        val sortAscending: Boolean,
        val sourceIn: Set<SeriesFilterOption>,
        val showOnlyConfirmedTags: Boolean,
    )
}
