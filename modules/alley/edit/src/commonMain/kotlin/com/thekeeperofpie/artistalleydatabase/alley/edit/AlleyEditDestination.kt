package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.navigation3.runtime.NavKey
import com.eygraber.uri.Uri
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface AlleyEditDestination : NavKey {

    @Serializable
    data object Home : AlleyEditDestination

    @Serializable
    data class ArtistDetails(val artistId: Uuid) : AlleyEditDestination

    companion object {
        fun parseRoute(route: String) = if (route.isEmpty() || route.startsWith("home")) {
            Home
        } else if (route.startsWith("artist")) {
            try {
                val artistId = Uuid.parse(route.removePrefix("artist/"))
                ArtistDetails(artistId)
            } catch (_: IllegalArgumentException) {
                null
            }
        } else {
            null
        }

        fun toEncodedRoute(destination: NavKey) = if (destination !is AlleyEditDestination) {
            null
        } else when (destination) {
            is ArtistDetails -> "artist/${Uri.encode(destination.artistId.toString())}"
            Home -> ""
        }
    }
}
