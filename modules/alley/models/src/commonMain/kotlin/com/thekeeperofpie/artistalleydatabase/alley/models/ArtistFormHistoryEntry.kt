package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class ArtistFormHistoryEntry(
    val artistId: Uuid,
    val booth: String?,
    val name: String?,
    val timestamp: Instant,
)
