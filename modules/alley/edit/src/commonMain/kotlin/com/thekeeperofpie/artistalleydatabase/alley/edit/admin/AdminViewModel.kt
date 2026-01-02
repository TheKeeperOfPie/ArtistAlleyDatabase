package com.thekeeperofpie.artistalleydatabase.alley.edit.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistAddViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.launch
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class AdminViewModel(
    private val database: AlleyEditDatabase,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val createJob = ExclusiveProgressJob(viewModelScope, ::createDatabases)

    internal fun onClickCreate() {
        createJob.launch()
    }

    private suspend fun createDatabases() {
        database.databaseCreate()
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): AdminViewModel
    }
}
