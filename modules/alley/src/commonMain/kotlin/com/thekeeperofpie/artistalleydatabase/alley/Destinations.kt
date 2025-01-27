package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable

sealed interface Destinations : NavDestination {
    @Serializable
    data object Home : Destinations

    @Serializable
    data class ArtistDetails(val id: String, val imageIndex: String? = null) : Destinations

    @Serializable
    data class ArtistMap(val id: String) : Destinations

    @Serializable
    data class Series(val series: String) : Destinations

    @Serializable
    data class SeriesMap(val series: String) : Destinations

    @Serializable
    data class Merch(val merch: String) : Destinations

    @Serializable
    data class MerchMap(val merch: String) : Destinations

    @Serializable
    data class StampRallyDetails(val id: String, val imageIndex: String? = null) : Destinations

    @Serializable
    data class StampRallyMap(val id: String) : Destinations
}
