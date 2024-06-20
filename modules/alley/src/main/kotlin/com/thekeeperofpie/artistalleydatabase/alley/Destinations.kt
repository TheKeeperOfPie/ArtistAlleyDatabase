package com.thekeeperofpie.artistalleydatabase.alley

import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object Home : Destinations

    @Serializable
    data class ArtistDetails(val id: String, val imageIndex: String? = null) : Destinations

    @Serializable
    data class Series(val series: String) : Destinations

    @Serializable
    data class Merch(val merch: String) : Destinations

    @Serializable
    data class StampRallyDetails(val id: String, val imageIndex: String? = null) : Destinations
}
