package com.thekeeperofpie.artistalleydatabase.alley

import androidx.navigation3.runtime.NavKey
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable

sealed interface AlleyDestination : NavDestination, NavKey {

    @Serializable
    data object Home : AlleyDestination

    @Serializable
    data class ArtistDetails(
        val year: DataYear,
        val id: String,
        val booth: String?,
        val name: String?,
        val images: List<CatalogImage>? = null,
        val imageIndex: Int? = null,
    ) : AlleyDestination {
        constructor(entry: ArtistEntry, imageIndex: Int? = null) : this(
            year = entry.year,
            id = entry.id,
            booth = entry.booth,
            name = entry.name,
            imageIndex = imageIndex,
            images = entry.images,
        )
    }

    @Serializable
    data class ArtistMap(val id: String) : AlleyDestination

    @Serializable
    data class ArtistsList(val year: DataYear, val serializedBooths: String) : AlleyDestination

    @Serializable
    data object Changelog : AlleyDestination

    @Serializable
    data object Export : AlleyDestination

    @Serializable
    data class Images(
        val year: DataYear,
        val id: String,
        val type: Type,
        val images: List<com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage>,
        val initialImageIndex: Int?,
    ) : AlleyDestination {

        @Serializable
        sealed interface Type {
            @Serializable
            data class Artist(
                val id: String,
                val booth: String,
                val name: String?,
            ) : Type

            @Serializable
            data class StampRally(
                val id: String,
                val hostTable: String?,
                val fandom: String?,
            ) : Type
        }
    }

    @Serializable
    data class Import(val data: String) : AlleyDestination

    @Serializable
    data class Series(val year: DataYear? = null, val series: String) : AlleyDestination

    @Serializable
    data class SeriesMap(val year: DataYear? = null, val series: String) : AlleyDestination

    @Serializable
    data class Merch(val year: DataYear? = null, val merch: String) : AlleyDestination

    @Serializable
    data class MerchMap(val year: DataYear? = null, val merch: String) : AlleyDestination

    @Serializable
    data object Settings : AlleyDestination

    @Serializable
    data class StampRallies(val year: DataYear?, val series: String) : AlleyDestination

    @Serializable
    data class StampRallyDetails(
        val year: DataYear,
        val id: String,
        val hostTable: String?,
        val fandom: String?,
        val images: List<CatalogImage>?,
        // TODO: Why is this a string?
        val initialImageIndex: String? = null,
    ) : AlleyDestination {
        constructor(entry: StampRallyEntry, initialImageIndex: String? = null) : this(
            year = entry.year,
            id = entry.id,
            hostTable = entry.hostTable,
            fandom = entry.fandom,
            images = entry.images,
            initialImageIndex = initialImageIndex,
        )
    }

    @Serializable
    data class StampRallyMap(val year: DataYear, val id: String) : AlleyDestination

    fun toEncodedRoute() = when (this) {
        is ArtistDetails -> "artist/${year.serializedName}/$id"
        is ArtistMap -> "artist/map/$id"
        is ArtistsList -> "artists/${year.serializedName}/$serializedBooths"
        Changelog -> "changelog"
        Export -> "export"
        Home -> ""
        is Images -> {
            val typePath = when (type) {
                is Images.Type.Artist -> "artist/${type.id}"
                is Images.Type.StampRally -> "stamp_rally/${type.id}"
            }
            "images/${year.serializedName}/$typePath"
        }
        is Import -> "import/${data}"
        is Merch -> "merch/${year.serializedNameOrAll}/$merch"
        is MerchMap -> "merch/map/${year.serializedNameOrAll}/$merch"
        is Series -> "series/${year.serializedNameOrAll}/$series"
        is SeriesMap -> "series/map/${year.serializedNameOrAll}/$series"
        Settings -> "settings"
        is StampRallies -> "stamp_rallies/${year.serializedNameOrAll}/$series"
        is StampRallyDetails -> "stamp_rally/${year.serializedName}/$id"
        is StampRallyMap -> "stamp_rally/map/${year.serializedName}/$id"
    }


    companion object {

        private val DataYear?.serializedNameOrAll get() = this?.serializedName ?: "all"
        private fun String?.toDataYearOrLatest() =
            this?.let(DataYear::deserialize) ?: DataYear.LATEST

        private fun String?.toDataYearOrNull() =
            if (this == "all") null else this?.let(DataYear::deserialize) ?: DataYear.LATEST
        fun parseRoute(route: String): AlleyDestination? = try {
            val parts = route.trim('/').split('/')
            if (parts.isEmpty() || (parts.size == 1 && parts.first().isEmpty())) {
                Home
            } else {

                when (parts.first()) {
                    "artist" -> when (parts.size) {
                        3 if parts[1] == "map" -> ArtistMap(id = parts[2])
                        3 -> ArtistDetails(
                            year = parts[1].toDataYearOrLatest(),
                            id = parts[2],
                            booth = null,
                            name = null
                        )
                        else -> null
                    }
                    "artists" -> if (parts.size == 3) {
                        ArtistsList(
                            year = parts[1].toDataYearOrLatest(),
                            serializedBooths = parts[2]
                        )
                    } else null
                    "changelog" -> Changelog
                    "export" -> Export
                    "import" -> Import(parts.getOrNull(1).orEmpty())
                    "merch" -> when (parts.size) {
                        4 if parts[1] == "map" -> MerchMap(
                            year = parts[2].toDataYearOrNull(),
                            merch = parts[3]
                        )
                        3 -> Merch(year = parts[1].toDataYearOrNull(), merch = parts[2])
                        else -> null
                    }
                    "series" -> when (parts.size) {
                        4 if parts[1] == "map" ->
                            SeriesMap(year = parts[2].toDataYearOrNull(), series = parts[3])
                        3 -> Series(year = parts[1].toDataYearOrNull(), series = parts[2])
                        else -> null
                    }
                    "settings" -> Settings
                    "stamp_rallies" -> if (parts.size == 3) {
                        StampRallies(year = parts[1].toDataYearOrNull(), series = parts[2])
                    } else null
                    "stamp_rally" -> when (parts.size) {
                        4 if parts[1] == "map" ->
                            StampRallyMap(year = parts[2].toDataYearOrLatest(), id = parts[3])
                        3 -> StampRallyDetails(
                            year = parts[1].toDataYearOrLatest(),
                            id = parts[2],
                            hostTable = null,
                            fandom = null,
                            images = null,
                        )
                        else -> null
                    }
                    else -> null
                }
            }
        } catch (_: Throwable) {
            null
        }
    }
}
