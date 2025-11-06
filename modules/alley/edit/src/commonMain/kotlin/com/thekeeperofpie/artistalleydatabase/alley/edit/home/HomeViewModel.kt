package com.thekeeperofpie.artistalleydatabase.alley.edit.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest

@AssistedInject
class HomeViewModel(
    database: AlleyEditDatabase,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val dataYear = savedStateHandle.getMutableStateFlow("dataYear", DataYear.LATEST)
    val query = savedStateHandle.getMutableStateFlow("query", "")
    private val artistEntries = dataYear.mapLatest(database::loadArtists)
    val entries = combine(query, artistEntries, ::Pair)
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

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): HomeViewModel
    }
}
