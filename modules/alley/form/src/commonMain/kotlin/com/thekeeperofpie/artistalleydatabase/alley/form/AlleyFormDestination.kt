package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.navigation3.runtime.NavKey
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface AlleyFormDestination : NavKey {

    @Serializable
    data object Home : AlleyFormDestination

    // Artist is decided via the in-memory access key
    @Serializable
    data class ArtistForm(val dataYear: DataYear) : AlleyFormDestination

    companion object {
        fun parseRoute(route: String): AlleyFormDestination? = try {
            when {
                route.isEmpty() || route.startsWith("home") -> Home
                route.startsWith("artist") -> {
                    val (year) = route.removePrefix("artist/").split("/")
                    val dataYear = DataYear.deserialize(year) ?: return null
                    ArtistForm(dataYear)
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
            Home -> ""
        }

        private fun parseDataYearThenArtistId(trailingPathSegments: String): Pair<DataYear, Uuid>? {
            val (year, artist) = trailingPathSegments.split("/")
            val dataYear = DataYear.deserialize(year) ?: return null
            val artistId = Uuid.parse(artist)
            return dataYear to artistId
        }
    }
}
