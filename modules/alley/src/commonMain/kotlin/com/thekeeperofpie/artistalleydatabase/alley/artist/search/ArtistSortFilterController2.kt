package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import dev.zacsweers.metro.Inject

@Inject
internal class ArtistSortFilterController2(
    settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
) {
    val state by savedStateHandle.saved { ArtistSortFilterState() }

    val persistentState = ArtistSortFilterPersistentState(
        showGridByDefault = settings.showGridByDefault,
        showRandomCatalogImage = settings.showRandomCatalogImage,
        showOutdatedCatalogs = settings.showOutdatedCatalogs,
        forceOneDisplayColumn = settings.forceOneDisplayColumn,
    )

    fun clear() {
        state.clear()
        persistentState.clear()
    }
}
