package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.navigation3.runtime.NavKey
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface AlleyFormDestination : NavKey {

    @Serializable
    data object Home : AlleyFormDestination

    @Serializable
    data class ArtistForm(val dataYear: DataYear, val artistId: Uuid, val privateKey: String) :
        AlleyFormDestination

    companion object {
        fun parseRoute(route: String): AlleyFormDestination? = try {
            when {
                route.isEmpty() || route.startsWith("home") -> Home
                route.startsWith("form/artist") -> {
                    val (dataYear, artistId) = parseDataYearThenArtistId(
                        route.removePrefix("form/artist/")
                    ) ?: return null
                    // TODO: Actual URI parsing
                    val privateKey = route.substringAfter("?${AlleyCryptography.ACCESS_KEY_PARAM}=")
                    ArtistForm(dataYear, artistId, privateKey)
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
            is ArtistForm -> "form/artist/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
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
