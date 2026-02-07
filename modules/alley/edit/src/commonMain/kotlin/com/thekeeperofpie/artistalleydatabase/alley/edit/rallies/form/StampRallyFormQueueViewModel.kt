package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class StampRallyFormQueueViewModel(
    database: AlleyEditDatabase,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val refreshFlow = RefreshFlow()
    val queue = refreshFlow.updates
        .mapLatest { database.loadStampRallyFormQueue() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val history = refreshFlow.updates
        .mapLatest { database.loadStampRallyFormQueueHistory() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun refresh() = refreshFlow.refresh()

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): StampRallyFormQueueViewModel
    }
}
