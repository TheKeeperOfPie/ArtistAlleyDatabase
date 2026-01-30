package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditViewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@AssistedInject
class StampRallyEditViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    @Assisted private val dataYear: DataYear,
    @Assisted private val stampRallyId: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private suspend fun loadStampRallyInfo() = withContext(dispatchers.io) {
        val response = database.loadStampRally(dataYear, stampRallyId)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            stampRallyId: String,
            savedStateHandle: SavedStateHandle,
        ): StampRallyEditViewModel
    }
}
