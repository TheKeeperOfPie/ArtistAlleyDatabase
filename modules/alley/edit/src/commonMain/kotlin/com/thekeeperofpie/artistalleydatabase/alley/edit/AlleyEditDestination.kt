package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.navigation3.runtime.NavKey
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface AlleyEditDestination : NavKey {

    @Serializable
    data object Home : AlleyEditDestination

    @Serializable
    data class ArtistAdd(val dataYear: DataYear) : AlleyEditDestination

    @Serializable
    data class ArtistEdit(val dataYear: DataYear, val artistId: Uuid) : AlleyEditDestination

    @Serializable
    data class ImagesEdit(
        val dataYear: DataYear,
        val displayName: String,
        val images: List<EditImage>,
    ) : AlleyEditDestination

    companion object {
        fun parseRoute(route: String): AlleyEditDestination? = when {
            route.isEmpty() || route.startsWith("home") -> Home
            route.startsWith("artist/add") -> try {
                val (year) = route.removePrefix("artist/add").split("/")
                val dataYear = DataYear.deserialize(year) ?: return null
                ArtistAdd(dataYear)
            } catch (_: IllegalArgumentException) {
                null
            }
            route.startsWith("artist") -> try {
                val (year, artist) = route.removePrefix("artist/").split("/")
                val artistId = Uuid.parse(artist)
                val dataYear = DataYear.deserialize(year) ?: return null
                ArtistEdit(dataYear, artistId)
            } catch (_: IllegalArgumentException) {
                null
            }
            else -> null
        }

        fun toEncodedRoute(destination: AlleyEditDestination) = when (destination) {
            is ArtistAdd -> "artist/add/${Uri.encode(destination.dataYear.serializedName)}"
            is ArtistEdit -> "artist/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
            is ImagesEdit -> null
            Home -> ""
        }
    }
}
