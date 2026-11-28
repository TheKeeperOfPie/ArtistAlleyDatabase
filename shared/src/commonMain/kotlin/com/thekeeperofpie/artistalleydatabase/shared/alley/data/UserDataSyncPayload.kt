package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant

// TODO: js target boolean equality broken
// JS interop infers this as 0, whereas KotlinX Serialization requires true
@Suppress("EQUALITY_NOT_APPLICABLE_WARNING")
private fun coerceBooleanForJs(value: Boolean?) = value == true || value.toString() == "1"

@Serializable
data class UserDataSyncPayload(
    val generation: Long? = null,
    val artists: List<Artist>? = null,
    val artistNotes: List<ArtistNotes>? = null,
    val rallies: List<StampRally>? = null,
    val rallyNotes: List<StampRallyNotes>? = null,
    val series: List<Series>? = null,
    val merch: List<Merch>? = null,
) {

    val size
        get() = (artists?.size ?: 0) +
                (artistNotes?.size ?: 0) +
                (rallies?.size ?: 0) +
                (rallyNotes?.size ?: 0) +
                (series?.size ?: 0) +
                (merch?.size ?: 0)

    fun isEmpty() = size == 0

    // TODO: Use value class for all IDs
    @Serializable
    data class Artist(
        val generation: Long,
        val dataYear: DataYear,
        val id: String,
        private val _favorite: Int? = null,
        private val _ignored: Int? = null,
        val updatedAt: Instant? = null,
    ) {
        val favorite get() = _favorite?.let { it == 1 }
        val ignored get() = _ignored?.let { it == 1 }

        constructor(
            generation: Long,
            dataYear: DataYear,
            id: String,
            favorite: Boolean? = null,
            ignored: Boolean? = null,
            updatedAt: Instant? = null,
        ) : this(
            generation = generation,
            dataYear = dataYear,
            id = id,
            _favorite = when (favorite?.let(::coerceBooleanForJs)) {
                null -> null
                false -> 0
                true -> 1
            },
            _ignored = when (ignored?.let(::coerceBooleanForJs)) {
                null -> null
                false -> 0
                true -> 1
            },
            updatedAt = updatedAt,
        )

    }

    @Serializable
    data class ArtistNotes(
        val generation: Long,
        val dataYear: DataYear,
        val artistId: EncryptedArtistId,
        val notes: String? = null,
        val updatedAt: Instant? = null,
    )

    @Serializable
    data class StampRally(
        val generation: Long,
        val id: String,
        private val _favorite: Int? = null,
        private val _ignored: Int? = null,
        val updatedAt: Instant? = null,
    ) {
        val favorite get() = _favorite?.let { it == 1 }
        val ignored get() = _ignored?.let { it == 1 }

        constructor(
            generation: Long,
            id: String,
            favorite: Boolean? = null,
            ignored: Boolean? = null,
            updatedAt: Instant? = null,
        ) : this(
            generation = generation,
            id = id,
            _favorite = when (favorite?.let(::coerceBooleanForJs)) {
                null -> null
                false -> 0
                true -> 1
            },
            _ignored = when (ignored?.let(::coerceBooleanForJs)) {
                null -> null
                false -> 0
                true -> 1
            },
            updatedAt = updatedAt,
        )
    }

    @Serializable
    data class StampRallyNotes(
        val generation: Long,
        val stampRallyId: EncryptedStampRallyId,
        val notes: String? = null,
        val updatedAt: Instant? = null,
    )

    @Serializable
    data class Series(
        val generation: Long,
        val id: String,
        private val _favorite: Int? = null,
        val updatedAt: Instant? = null,
    ) {
        val favorite get() = _favorite?.let { it == 1 }

        constructor(
            generation: Long,
            id: String,
            favorite: Boolean? = null,
            updatedAt: Instant? = null,
        ) : this(
            generation = generation,
            id = id,
            _favorite = when (favorite?.let(::coerceBooleanForJs)) {
                null -> null
                false -> 0
                true -> 1
            },
            updatedAt = updatedAt,
        )
    }

    @Serializable
    data class Merch(
        val generation: Long,
        val id: String,
        private val _favorite: Int? = null,
        val updatedAt: Instant? = null,
    ) {
        val favorite get() = _favorite?.let { it == 1 }

        constructor(
            generation: Long,
            id: String,
            favorite: Boolean? = null,
            updatedAt: Instant? = null,
        ) : this(
            generation = generation,
            id = id,
            _favorite = when (favorite?.let(::coerceBooleanForJs)) {
                null -> null
                false -> 0
                true -> 1
            },
            updatedAt = updatedAt,
        )
    }
}
