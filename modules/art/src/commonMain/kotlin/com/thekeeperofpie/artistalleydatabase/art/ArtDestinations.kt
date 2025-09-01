package com.thekeeperofpie.artistalleydatabase.art

import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable

object ArtDestinations {

    @Serializable
    data class Details(val entryIds: List<String> = emptyList()) : NavDestination
}
