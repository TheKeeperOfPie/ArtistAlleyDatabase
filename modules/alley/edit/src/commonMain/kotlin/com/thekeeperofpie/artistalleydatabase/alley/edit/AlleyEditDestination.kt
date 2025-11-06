package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.navigation3.runtime.NavKey
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface AlleyEditDestination : NavKey {

    @Serializable
    data object Home : AlleyEditDestination

    @Serializable
    data class ArtistEdit(val dataYear: DataYear, val artistId: Uuid) : AlleyEditDestination

    companion object {
        fun parseRoute(route: String) = if (route.isEmpty() || route.startsWith("home")) {
            Home
        } else if (route.startsWith("artist")) {
            try {
                val (year, artist) = route.removePrefix("artist/").split("/")
                val artistId = Uuid.parse(artist)
                val dataYear = DataYear.deserialize(year) ?: return null
                ArtistEdit(dataYear, artistId)
            } catch (_: IllegalArgumentException) {
                null
            }
        } else {
            null
        }

        fun toEncodedRoute(destination: NavKey) = if (destination !is AlleyEditDestination) {
            null
        } else when (destination) {
            is ArtistEdit -> "artist/${Uri.encode(destination.dataYear.serializedName)}/${Uri.encode(destination.artistId.toString())}"
            Home -> ""
        }
    }
}
