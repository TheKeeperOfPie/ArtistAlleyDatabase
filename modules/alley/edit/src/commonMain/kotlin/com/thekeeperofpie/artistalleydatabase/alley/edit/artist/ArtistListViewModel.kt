package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@AssistedInject
class ArtistListViewModel(
    database: AlleyEditDatabase,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val dataYear = savedStateHandle.getMutableStateFlow("dataYear", DataYear.LATEST)
    val query by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }
    private val refreshFlow = RefreshFlow()
    private val artistEntries = combine(dataYear, refreshFlow.updates, ::Pair)
        .mapLatest { (dataYear) -> database.loadArtists(dataYear) }

    private val debouncedQuery = snapshotFlow { query.text.toString() }
        .debounce(500.milliseconds)
    val entries = combine(debouncedQuery, artistEntries, ::Pair)
        .mapLatest { (query, entries) ->
            if (query.isBlank()) {
                return@mapLatest entries
            }

            entries.filter {
                it.id.toString().contains(query) ||
                        it.name?.contains(query) == true ||
                        it.booth?.contains(query) == true
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refresh() = refreshFlow.refresh()

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): ArtistListViewModel
    }
}
