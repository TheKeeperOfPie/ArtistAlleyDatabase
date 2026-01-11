package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class ImagesViewModel(
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val route = savedStateHandle.toDestination<Destinations.Images>(navigationTypeMap)

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): ImagesViewModel
    }
}
