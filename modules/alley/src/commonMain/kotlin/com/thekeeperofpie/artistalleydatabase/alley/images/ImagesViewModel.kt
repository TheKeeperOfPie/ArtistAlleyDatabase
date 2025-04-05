package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ImagesViewModel(
    dispatchers: CustomDispatchers,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val route = savedStateHandle.toDestination<Destinations.Images>(navigationTypeMap)

    var images by mutableStateOf(emptyList<CatalogImage>())
        private set

    init {
        viewModelScope.launch(dispatchers.io) {
            images = AlleyDataUtils.getImages(
                year = route.year,
                folder = route.folder,
                file = route.file,
            )
        }
    }
}
