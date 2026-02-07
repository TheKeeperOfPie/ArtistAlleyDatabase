package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class StampRallyFormHistoryEntry(
    val stampRallyId: String,
    val hostTable: String?,
    val fandom: String?,
    val timestamp: Instant,
)
