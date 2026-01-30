package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class StampRallyAddViewModel(
    @Assisted private val dataYear: DataYear,
    @Assisted stampRallyId: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            stampRallyId: String,
            savedStateHandle: SavedStateHandle,
        ): StampRallyAddViewModel
    }
}
