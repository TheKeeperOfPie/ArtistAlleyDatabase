package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserDataSyncPayload(
    val generation: Long? = null,
    val artists: List<Artist>? = null,
    val rallies: List<StampRally>? = null,
    val series: List<Series>? = null,
    val merch: List<Merch>? = null,
) {

    fun isEmpty() = artists.isNullOrEmpty() &&
            rallies.isNullOrEmpty() &&
            series.isNullOrEmpty() &&
            merch.isNullOrEmpty()

    // TODO: Use value class for all IDs
    @Serializable
    data class Artist(
        val generation: Long,
        val dataYear: DataYear,
        val id: String,
        val favorite: Boolean? = null,
        val ignored: Boolean? = null,
        val updatedAt: Instant? = null,
    )

    @Serializable
    data class StampRally(
        val generation: Long,
        val id: String,
        val favorite: Boolean? = null,
        val ignored: Boolean? = null,
        val updatedAt: Instant? = null,
    )

    @Serializable
    data class Series(
        val generation: Long,
        val id: String,
        val favorite: Boolean? = null,
        val updatedAt: Instant? = null,
    )

    @Serializable
    data class Merch(
        val generation: Long,
        val id: String,
        val favorite: Boolean? = null,
        val updatedAt: Instant? = null,
    )
}
