package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class ImagesEditViewModel(
    @Assisted images: List<EditImage>,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val images by savedStateHandle.saveable(saver = StateUtils.snapshotListJsonSaver()) {
        images.toMutableStateList()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            images: List<EditImage>,
            savedStateHandle: SavedStateHandle,
        ): ImagesEditViewModel
    }
}
