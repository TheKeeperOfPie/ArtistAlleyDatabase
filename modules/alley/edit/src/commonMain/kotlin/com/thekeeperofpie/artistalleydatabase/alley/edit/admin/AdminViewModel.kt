package com.thekeeperofpie.artistalleydatabase.alley.edit.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistAddViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.launch
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class AdminViewModel(
    private val database: AlleyEditDatabase,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val createJob = ExclusiveProgressJob(viewModelScope, ::createDatabases)
    private val deleteFakeArtistDataJob = ExclusiveProgressJob(viewModelScope, ::deleteFakeArtistData)
    val fakeArtistFormLink = flowFromSuspend { database.fakeArtistFormLink() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    internal fun onClickCreate() = createJob.launch()
    internal fun onClickClearFakeArtistData() = deleteFakeArtistDataJob.launch()

    private suspend fun createDatabases() = database.databaseCreate()
    private suspend fun deleteFakeArtistData() = database.deleteFakeArtistData()

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): AdminViewModel
    }
}
