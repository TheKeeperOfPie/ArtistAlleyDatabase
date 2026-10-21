package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.StampRallyDetails
import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavigator
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

internal object StampRallySearchScreenEventHandler {

    fun onEvent(
        navigator: AlleyNavigator,
        sortFilterController: StampRallySortFilterController,
        year: () -> DataYear,
        onMutationUpdate: (StampRallyUserEntry) -> Unit,
        event: StampRallySearchScreen.Event,
    ) {
        when (event) {
            is StampRallySearchScreen.Event.FavoriteToggle ->
                onMutationUpdate(event.stampRally.userEntry.copy(favorite = event.favorite))
            is StampRallySearchScreen.Event.IgnoreToggle ->
                onMutationUpdate(event.stampRally.userEntry.copy(ignored = event.ignored))
            is StampRallySearchScreen.Event.OpenEntry ->
                navigator.navigate(
                    StampRallyDetails(
                        entry = event.stampRally.stampRally,
                        initialImageIndex = event.imageIndex
                    )
                )
            is StampRallySearchScreen.Event.OpenImageFullscreen ->
                navigator.navigate(
                    AlleyDestination.Images.fromStampRally(
                        stampRallyWithUserData = event.stampRally,
                        imageIndex = event.imageIndex,
                    )
                )
            is StampRallySearchScreen.Event.ClearFilters -> sortFilterController.clear()
            StampRallySearchScreen.Event.Back -> navigator.goBack()
            StampRallySearchScreen.Event.OpenChangelog -> navigator.navigate(
                AlleyDestination.StampRallyChangelog(year())
            )
            StampRallySearchScreen.Event.OpenSettings -> navigator.navigate(AlleyDestination.Settings)
        }
    }
}
