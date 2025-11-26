package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.navigation3.runtime.NavKey
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesColumn
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface AlleyEditDestination : NavKey {

    @Serializable
    data object Home : AlleyEditDestination

    @Serializable
    data class ArtistAdd(val dataYear: DataYear, val artistId: Uuid = Uuid.random()) :
        AlleyEditDestination

    @Serializable
    data class ArtistEdit(val dataYear: DataYear, val artistId: Uuid) : AlleyEditDestination

    @Serializable
    data class ImagesEdit(
        val dataYear: DataYear,
        val displayName: String,
        val images: List<EditImage>,
    ) : AlleyEditDestination

    @Serializable
    data object Series : AlleyEditDestination

    @Serializable
    data class SeriesAdd(val seriesId: Uuid = Uuid.random()) : AlleyEditDestination

    @Serializable
    data class SeriesEdit(val series: SeriesInfo, val seriesColumn: SeriesColumn?) : AlleyEditDestination

    @Serializable
    data object Merch : AlleyEditDestination

    @Serializable
    data class MerchAdd(val merchId: Uuid = Uuid.random()) : AlleyEditDestination

    @Serializable
    data class MerchEdit(val merch: MerchInfo) : AlleyEditDestination

    companion object {
        fun parseRoute(route: String): AlleyEditDestination? = when {
            route.isEmpty() || route.startsWith("home") -> Home
            route == "series" -> Series
            route == "merch" -> Merch
            route.startsWith("artist/add") -> try {
                val (year, artist) = route.removePrefix("artist/add/").split("/")
                val artistId = Uuid.parse(artist)
                val dataYear = DataYear.deserialize(year) ?: return null
                ArtistAdd(dataYear, artistId)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                null
            }
            route.startsWith("artist") -> try {
                val (year, artist) = route.removePrefix("artist/").split("/")
                val artistId = Uuid.parse(artist)
                val dataYear = DataYear.deserialize(year) ?: return null
                ArtistEdit(dataYear, artistId)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                null
            }
            else -> {
                ConsoleLogger.log("Failed to find route for $route")
                null
            }
        }

        fun toEncodedRoute(destination: AlleyEditDestination) = when (destination) {
            is ArtistAdd -> "artist/add/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
            is ArtistEdit -> "artist/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
            Series -> "series"
            is SeriesAdd -> "series/add/${Uri.encode(destination.seriesId.toString())}"
            Merch -> "merch"
            is MerchAdd -> "merch/add/${Uri.encode(destination.merchId.toString())}"
            Home -> ""
            is ImagesEdit,
            is MerchEdit,
            is SeriesEdit, -> null
        }
    }
}
