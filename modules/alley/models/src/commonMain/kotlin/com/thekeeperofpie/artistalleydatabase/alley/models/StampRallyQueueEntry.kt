package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable

@Serializable
data class StampRallyQueueEntry(
    val link: String,
    val booths: Set<String>,
)
