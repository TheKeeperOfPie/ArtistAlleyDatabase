package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

@AssistedInject
class ImagesEditViewModel(
    @Assisted images: List<EditImage>,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val images by savedStateHandle.saveable(saver = StateUtils.snapshotListJsonSaver()) {
        images.toMutableStateList()
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            images: List<EditImage>,
            savedStateHandle: SavedStateHandle,
        ): ImagesEditViewModel
    }
}
