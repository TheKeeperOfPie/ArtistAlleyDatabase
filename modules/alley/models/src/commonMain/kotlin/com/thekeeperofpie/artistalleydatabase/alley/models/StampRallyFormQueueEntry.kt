package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable

@Serializable
data class StampRallyFormQueueEntry(
    val stampRallyId: String,
    val hostTable: String?,
    val fandom: String?,
)
