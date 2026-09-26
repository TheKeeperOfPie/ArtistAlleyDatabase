package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class RemoteArtistDataQueueViewModel(
    database: AlleyEditDatabase,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val refreshFlow = RefreshFlow()
    val queue = refreshFlow.updates
        .mapLatest { database.loadRemoteArtistData(DataYear.LATEST) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val history = refreshFlow.updates
        .mapLatest { database.loadRemoteArtistDataHistory(DataYear.LATEST) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refresh() = refreshFlow.refresh()

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(savedStateHandle: SavedStateHandle): RemoteArtistDataQueueViewModel
    }
}
