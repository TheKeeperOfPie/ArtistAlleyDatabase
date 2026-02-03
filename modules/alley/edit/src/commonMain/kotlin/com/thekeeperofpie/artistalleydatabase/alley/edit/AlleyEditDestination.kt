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
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface AlleyEditDestination : NavKey {

    @Serializable
    data object Home : AlleyEditDestination

    @Serializable
    data object Admin : AlleyEditDestination

    @Serializable
    data class ArtistAdd(val dataYear: DataYear, val artistId: Uuid = Uuid.random()) :
        AlleyEditDestination

    @Serializable
    data class ArtistEdit(val dataYear: DataYear, val artistId: Uuid) : AlleyEditDestination

    @Serializable
    data class ArtistFormHistory(
        val dataYear: DataYear,
        val artistId: Uuid,
        val formTimestamp: Instant,
    ) : AlleyEditDestination

    @Serializable
    data class ArtistFormMerge(val dataYear: DataYear, val artistId: Uuid) : AlleyEditDestination

    @Serializable
    data object ArtistFormQueue : AlleyEditDestination

    @Serializable
    data class ArtistHistory(val dataYear: DataYear, val artistId: Uuid) : AlleyEditDestination

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
    data class SeriesEdit(val series: SeriesInfo, val seriesColumn: SeriesColumn?) :
        AlleyEditDestination

    @Serializable
    data object Merch : AlleyEditDestination

    @Serializable
    data class MerchAdd(val merchId: Uuid = Uuid.random()) : AlleyEditDestination

    @Serializable
    data class MerchEdit(val merch: MerchInfo) : AlleyEditDestination

    @Serializable
    data object TagResolution : AlleyEditDestination

    @Serializable
    data class SeriesResolution(val seriesId: String) : AlleyEditDestination

    @Serializable
    data class MerchResolution(val merchId: String) : AlleyEditDestination

    @Serializable
    data object StampRallies : AlleyEditDestination

    @Serializable
    data class StampRallyAdd(
        val dataYear: DataYear,
        val stampRallyId: String = Uuid.random().toString(),
    ) :
        AlleyEditDestination

    @Serializable
    data class StampRallyEdit(val dataYear: DataYear, val stampRallyId: String) :
        AlleyEditDestination

    @Serializable
    data class StampRallyHistory(
        val dataYear: DataYear,
        val stampRallyId: String,
    ) : AlleyEditDestination

    companion object {
        fun parseRoute(route: String): AlleyEditDestination? = try {
            when {
                route.isEmpty() || route.startsWith("home") -> Home
                route == "admin" -> Admin
                route == "series" -> Series
                route == "merch" -> Merch
                route == "queue" -> ArtistFormQueue
                route == "rallies" -> StampRallies
                route.startsWith("resolution") -> {
                    val segments = route.split("/").filter { it.isNotEmpty() }
                    when {
                        segments.getOrNull(0) == "series" ->
                            segments.getOrNull(1)?.let(::SeriesResolution)
                        segments.getOrNull(0) == "merch" ->
                            segments.getOrNull(1)?.let(::MerchResolution)
                        else -> null
                    } ?: TagResolution
                }
                route.startsWith("artist/history") -> {
                    val (dataYear, artistId) = parseDataYearThenArtistId(
                        route.removePrefix("artist/history/")
                    ) ?: return null
                    ArtistHistory(dataYear, artistId)
                }
                route.startsWith("artist/merge") -> {
                    val (dataYear, artistId) = parseDataYearThenArtistId(
                        route.removePrefix("artist/merge/")
                    ) ?: return null
                    ArtistFormMerge(dataYear, artistId)
                }
                route.startsWith("artist/add") -> {
                    val (dataYear, artistId) = parseDataYearThenArtistId(
                        route.removePrefix("artist/add/")
                    ) ?: return null
                    ArtistAdd(dataYear, artistId)
                }
                route.startsWith("artist") -> {
                    val (dataYear, artistId) = parseDataYearThenArtistId(
                        route.removePrefix("artist/")
                    ) ?: return null
                    ArtistEdit(dataYear, artistId)
                }
                route.startsWith("rally/add") -> {
                    val (dataYear, stampRallyId) = parseDataYearThenStampRallyId(
                        route.removePrefix("rally/add/")
                    ) ?: return null
                    StampRallyAdd(dataYear, stampRallyId)
                }
                route.startsWith("rally") -> {
                    val (dataYear, stampRallyId) = parseDataYearThenStampRallyId(
                        route.removePrefix("artist/")
                    ) ?: return null
                    StampRallyEdit(dataYear, stampRallyId)
                }
                route.startsWith("rally/history") -> {
                    val (dataYear, stampRallyId) = parseDataYearThenStampRallyId(
                        route.removePrefix("rally/history/")
                    ) ?: return null
                    StampRallyHistory(dataYear, stampRallyId)
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

        fun toEncodedRoute(destination: AlleyEditDestination) = when (destination) {
            is Admin -> "admin"
            is ArtistAdd -> "artist/add/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
            is ArtistEdit -> "artist/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
            is ArtistFormMerge -> "artist/merge/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
            is ArtistFormHistory -> "artist/form/history${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
            is ArtistFormQueue -> "queue"
            is ArtistHistory -> "artist/history/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.artistId.toString())
            Series -> "series"
            is SeriesAdd -> "series/add/${Uri.encode(destination.seriesId.toString())}"
            Merch -> "merch"
            is MerchAdd -> "merch/add/${Uri.encode(destination.merchId.toString())}"
            TagResolution -> "resolution"
            is SeriesResolution -> "resolution/series/${destination.seriesId}"
            is MerchResolution -> "resolution/merch/${destination.merchId}"
            StampRallies -> "rallies"
            is StampRallyAdd -> "rally/add/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.stampRallyId)
            is StampRallyEdit -> "rally/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.stampRallyId)
            is StampRallyHistory -> "rally/history/${Uri.encode(destination.dataYear.serializedName)}/" +
                    Uri.encode(destination.stampRallyId.toString())
            Home -> ""
            is ImagesEdit,
            is MerchEdit,
            is SeriesEdit,
                -> null
        }

        private fun parseDataYearThenArtistId(trailingPathSegments: String): Pair<DataYear, Uuid>? {
            val (year, artist) = trailingPathSegments.split("/")
            val dataYear = DataYear.deserialize(year) ?: return null
            val artistId = Uuid.parse(artist)
            return dataYear to artistId
        }

        private fun parseDataYearThenStampRallyId(trailingPathSegments: String): Pair<DataYear, String>? {
            val (year, stampRallyId) = trailingPathSegments.split("/")
            val dataYear = DataYear.deserialize(year) ?: return null
            return dataYear to stampRallyId
        }
    }
}
