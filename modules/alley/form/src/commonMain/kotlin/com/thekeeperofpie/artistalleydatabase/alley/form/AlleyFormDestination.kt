package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.navigation3.runtime.NavKey
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRequestKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AlleyFormDestination : NavKey {

    @Serializable
    data object Home : AlleyFormDestination

    // Artist is decided via the in-memory access key
    @Serializable
    data class ArtistForm(val dataYear: DataYear) : AlleyFormDestination

    @Serializable
    data class ImagesEdit(
        val requestKey: NavigationRequestKey<List<EditImage>>,
        val images: List<EditImage>,
        val displayName: String? = null,
    ) : AlleyFormDestination

    companion object {
        fun parseRoute(route: String): AlleyFormDestination? = try {
            when {
                route.isEmpty() || route.startsWith("home") -> Home
                route.startsWith("artist") -> {
                    val (year) = route.removePrefix("artist/").split("/")
                    val dataYear = DataYear.deserialize(year) ?: return null
                    ArtistForm(dataYear)
                }
                route.startsWith("images") -> {
                    val (requestKey) = route.removePrefix("images/").split("/")
                    // TODO: Actually restore images? It's possible since each image can be
                    //  represented by a unique String ID, but not sure it matters.
                    ImagesEdit(requestKey = NavigationRequestKey(requestKey), images = emptyList())
                }
                else -> {
                    ConsoleLogger.log("Failed to find route for $route")
                    null
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }

        fun toEncodedRoute(destination: AlleyFormDestination) = when (destination) {
            is ArtistForm -> "artist/${Uri.encode(destination.dataYear.serializedName)}"
            is ImagesEdit -> "images/${Uri.encode(destination.requestKey.key)}"
            Home -> ""
        }
    }
}
