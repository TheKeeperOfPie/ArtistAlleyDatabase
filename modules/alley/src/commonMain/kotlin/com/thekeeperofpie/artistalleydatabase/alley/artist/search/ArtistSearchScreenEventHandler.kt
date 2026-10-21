package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavigator
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

internal object ArtistSearchScreenEventHandler {

    fun onEvent(
        navigator: AlleyNavigator,
        sortFilterController: ArtistSortFilterController,
        year: () -> DataYear,
        onMutationUpdate: (ArtistUserEntry) -> Unit,
        event: ArtistSearchScreen.Event,
    ) {
        when (event) {
            is ArtistSearchScreen.Event.FavoriteToggle -> onMutationUpdate(
                event.entry.userEntry.copy(favorite = event.favorite)
            )
            is ArtistSearchScreen.Event.IgnoreToggle -> onMutationUpdate(
                event.entry.userEntry.copy(ignored = event.ignored)
            )
            is ArtistSearchScreen.Event.ClearFilters -> sortFilterController.clear()
            ArtistSearchScreen.Event.Back -> navigator.goBack()
            ArtistSearchScreen.Event.OpenChangelog ->
                navigator.navigate(AlleyDestination.ArtistChangelog(year()))
            is ArtistSearchScreen.Event.OpenEntry ->
                navigator.navigate(
                    AlleyDestination.ArtistDetails(
                        entry = event.entry.artist,
                        imageIndex = event.imageIndex,
                    )
                )
            ArtistSearchScreen.Event.OpenExport ->
                navigator.navigate(AlleyDestination.Export(year()))
            is ArtistSearchScreen.Event.OpenImageFullscreen ->
                navigator.navigate(
                    AlleyDestination.Images.fromArtist(
                        artistWithUserData = event.entry.data,
                        showOutdatedCatalogs =
                            sortFilterController.state.persistentState.sortFilterSettings.showOutdatedCatalogs,
                        imageIndex = event.imageIndex,
                    )
                )
            is ArtistSearchScreen.Event.OpenMerch ->
                navigator.navigate(AlleyDestination.Merch(year(), event.merch))
            is ArtistSearchScreen.Event.OpenSeries ->
                navigator.navigate(AlleyDestination.Series(year(), event.series))
            ArtistSearchScreen.Event.OpenSettings -> navigator.navigate(AlleyDestination.Settings)
        }
    }
}
