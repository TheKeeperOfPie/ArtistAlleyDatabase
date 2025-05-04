package com.thekeeperofpie.artistalleydatabase.alley

import androidx.navigation.NavType
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed interface Destinations : NavDestination {

    companion object {
        val typeMap: Map<KType, NavType<*>> = mapOf(
            typeOf<DataYear>() to CustomNavTypes.SerializableType<DataYear>(),
            typeOf<DataYear?>() to CustomNavTypes.SerializableType<DataYear>(),
            typeOf<AlleyDataUtils.Folder>() to CustomNavTypes.SerializableType<AlleyDataUtils.Folder>(),
            typeOf<Images.Title>() to CustomNavTypes.SerializableType<Images.Title>(),
        )
    }

    @Serializable
    data object Home : Destinations

    @Serializable
    data class ArtistDetails(
        val year: DataYear,
        val id: String,
        val booth: String?,
        val name: String?,
        val imageIndex: Int? = null,
    ) : Destinations {
        constructor(entry: ArtistEntry, imageIndex: Int? = null) : this(
            year = entry.year,
            id = entry.id,
            booth = entry.booth,
            name = entry.name,
            imageIndex = imageIndex,
        )
    }

    @Serializable
    data class ArtistMap(val id: String) : Destinations

    @Serializable
    data class Images(
        val year: DataYear,
        val id: String,
        val folder: AlleyDataUtils.Folder,
        val file: String,
        val title: Title,
        val initialImageIndex: Int?,
    ) : Destinations {

        @Serializable
        sealed interface Title {
            @Serializable
            data class Artist(val booth: String, val name: String?) : Title

            @Serializable
            data class StampRally(val hostTable: String?, val fandom: String?) : Title
        }
    }

    @Serializable
    data class Import(val data: String) : Destinations

    @Serializable
    data class Series(val year: DataYear? = null, val series: String) : Destinations

    @Serializable
    data class SeriesMap(val year: DataYear? = null, val series: String) : Destinations

    @Serializable
    data class Merch(val year: DataYear? = null, val merch: String) : Destinations

    @Serializable
    data class MerchMap(val year: DataYear? = null, val merch: String) : Destinations

    @Serializable
    data object Settings : Destinations

    @Serializable
    data class StampRallyDetails(
        val year: DataYear,
        val id: String,
        val hostTable: String?,
        val fandom: String?,
        val initialImageIndex: String? = null,
    ) : Destinations {
        constructor(entry: StampRallyEntry, initialImageIndex: String? = null) : this(
            year = entry.year,
            id = entry.id,
            hostTable = entry.hostTable,
            fandom = entry.fandom,
            initialImageIndex = initialImageIndex,
        )
    }

    @Serializable
    data class StampRallyMap(val year: DataYear, val id: String) : Destinations
}
