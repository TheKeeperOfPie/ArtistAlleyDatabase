package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class StampRallyFormQueueEntry(
    val artistId: Uuid,
    val stampRallyId: String,
    val hostTable: String?,
    val fandom: String?,
)
