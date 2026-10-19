package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.serialization.saved
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
    val showOnlyConfirmedTags = savedStateHandle.getMutableStateFlow(
        key = "showOnlyConfirmedTags",
        initialValue = settings.showOnlyConfirmedTags.value,
    )

    private val persistentState = SeriesSortFilterPersistentState(
        sortOption = settings.seriesSortOption,
        sortAscending = settings.seriesSortAscending,
        showOnlyConfirmedTags = showOnlyConfirmedTags,
    )

    private val saveableState by savedStateHandle.saved { SeriesSortFilterSaveableState() }

    val state = SeriesSortFilterState(
        persistentState = persistentState,
        saveableState = saveableState,
    )

    internal val filterParams by transform(scope) {
        val showOnlyConfirmedTags by persistentState.showOnlyConfirmedTags.collectAsStateWithLifecycle()
        FilterParams(
            sortOption = persistentState.sortOption.collectAsStateWithLifecycle().value,
            sortAscending = persistentState.sortAscending.collectAsStateWithLifecycle().value,
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
