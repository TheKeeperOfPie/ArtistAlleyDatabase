package com.thekeeperofpie.artistalleydatabase.alley

import androidx.navigation.NavType
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
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
        )
    }

    @Serializable
    data object Home : Destinations

    @Serializable
    data class ArtistDetails(
        val year: DataYear,
        val id: String,
        val imageIndex: String? = null,
    ) : Destinations

    @Serializable
    data class ArtistMap(val id: String) : Destinations

    @Serializable
    data class Series(val year: DataYear?, val series: String) : Destinations

    @Serializable
    data class SeriesMap(val year: DataYear?, val series: String) : Destinations

    @Serializable
    data class Merch(val year: DataYear?, val merch: String) : Destinations

    @Serializable
    data class MerchMap(val year: DataYear?, val merch: String) : Destinations

    @Serializable
    data class StampRallyDetails(
        val year: DataYear,
        val id: String,
        val imageIndex: String? = null,
    ) : Destinations

    @Serializable
    data class StampRallyMap(val year: DataYear, val id: String) : Destinations
}
