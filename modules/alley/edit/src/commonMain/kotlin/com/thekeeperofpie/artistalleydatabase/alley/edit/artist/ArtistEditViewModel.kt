package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class ArtistEditViewModel(
    database: AlleyEditDatabase,
    @Assisted route: AlleyEditDestination.ArtistEdit,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val artist = flowFromSuspend { database.loadArtist(route.dataYear, route.artistId) }
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, null)

    val state by savedStateHandle.saveable(saver = ArtistEditScreen.State.Saver) { ArtistEditScreen.State() }
    var hasLoaded by savedStateHandle.saved { false }

    init {
        if (!hasLoaded) {
            viewModelScope.launch {
                val artist = database.loadArtist(route.dataYear, route.artistId) ?: return@launch
                Snapshot.withMutableSnapshot {
                    // TODO: Fill out other fields and store lock state in database
                    artist.booth?.ifBlank { null }?.let {
                        state.booth.value.setTextAndPlaceCursorAtEnd(it)
                        state.booth.lockState = EntryLockState.LOCKED
                    }

                    state.name.value.setTextAndPlaceCursorAtEnd(artist.name)
                    state.name.lockState = EntryLockState.LOCKED

                    artist.summary?.ifBlank { null }?.let {
                        state.summary.value.setTextAndPlaceCursorAtEnd(it)
                        state.summary.lockState = EntryLockState.LOCKED
                    }

                    artist.notes?.ifBlank { null }?.let {
                        state.notes.pendingValue.setTextAndPlaceCursorAtEnd(it)
                        state.notes.lockState = EntryLockState.LOCKED
                    }
                }
                hasLoaded = true
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            route: AlleyEditDestination.ArtistEdit,
            savedStateHandle: SavedStateHandle,
        ): ArtistEditViewModel
    }
}
