package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class ArtistDetailsViewModel(
    @Assisted route: AlleyEditDestination.ArtistDetails,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val artistId = route.artistId

    @AssistedFactory
    interface Factory {
        fun create(
            route: AlleyEditDestination.ArtistDetails,
            savedStateHandle: SavedStateHandle,
        ): ArtistDetailsViewModel
    }
}
