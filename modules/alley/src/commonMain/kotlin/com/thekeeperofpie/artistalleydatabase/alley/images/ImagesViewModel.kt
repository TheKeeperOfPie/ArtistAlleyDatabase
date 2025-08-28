package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ImagesViewModel(
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val route = savedStateHandle.toDestination<Destinations.Images>(navigationTypeMap)

    val images = when (val type = route.type) {
        is Destinations.Images.Type.Artist -> AlleyDataUtils.getArtistImages(route.year, type.images)
        is Destinations.Images.Type.StampRally -> AlleyDataUtils.getRallyImages(
            year = route.year,
            id = type.id,
            hostTable = type.hostTable,
            fandom = type.fandom,
        )
    }
}
