package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@Inject
class RemoteArtistDataQueueViewModel(database: AlleyEditDatabase) : ViewModel() {
    private val refreshFlow = RefreshFlow()
    val data = refreshFlow.updates
        .mapLatest { database.loadRemoteArtistData(DataYear.ANIME_EXPO_2026) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refresh() = refreshFlow.refresh()
}
