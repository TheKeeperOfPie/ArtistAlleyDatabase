package com.thekeeperofpie.artistalleydatabase.anime.activities

import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable

object ActivityDestinations {

    @Serializable
    data class ActivityDetails(
        val activityId: String,
        val sharedTransitionScopeKey: String?,
    ) : NavDestination
}
