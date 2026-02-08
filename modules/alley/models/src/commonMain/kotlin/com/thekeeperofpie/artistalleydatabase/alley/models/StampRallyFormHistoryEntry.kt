package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class StampRallyFormHistoryEntry(
    val artistId: Uuid,
    val stampRallyId: String,
    val hostTable: String?,
    val fandom: String?,
    val timestamp: Instant,
)
