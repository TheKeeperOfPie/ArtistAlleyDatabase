package com.thekeeperofpie.artistalleydatabase.alley

import androidx.navigation.NavType
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed interface AlleyDestination : NavDestination {

    companion object {
        val typeMap: Map<KType, NavType<*>> = mapOf(
            typeOf<DataYear>() to CustomNavTypes.SerializableType<DataYear>(),
            typeOf<DataYear?>() to CustomNavTypes.SerializableType<DataYear>(),
            typeOf<Images.Type>() to CustomNavTypes.SerializableType<Images.Type>(),
            typeOf<Set<String>>() to CustomNavTypes.SerializableType<Set<String>>(),
            typeOf<List<CatalogImage>>() to CustomNavTypes.SerializableType<List<CatalogImage>>(
                serializer = { ListSerializer(CatalogImage.serializer()) },
            ),
            typeOf<List<CatalogImage>?>() to CustomNavTypes.SerializableType<List<CatalogImage>>(
                serializer = { ListSerializer(CatalogImage.serializer()) },
            ),
            typeOf<List<com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage>>() to CustomNavTypes.SerializableType<List<com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage>>(
                serializer = { ListSerializer(com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage.serializer()) },
            ),
            typeOf<List<com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage>?>() to CustomNavTypes.SerializableType<List<com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage>>(
                serializer = { ListSerializer(com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage.serializer()) },
            ),
        )
    }

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
        val images: List<CatalogImage>,
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
}
