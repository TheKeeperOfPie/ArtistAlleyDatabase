package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserDataSyncPayload(
    val artists: List<Artist>? = null,
    val rallies: List<StampRally>? = null,
    val series: List<Series>? = null,
    val merch: List<Merch>? = null,
) {
    @Serializable
    data class Artist(
        val generation: Long,
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
