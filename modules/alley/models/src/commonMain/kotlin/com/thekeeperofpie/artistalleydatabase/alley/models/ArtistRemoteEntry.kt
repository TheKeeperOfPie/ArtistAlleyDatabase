package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class ArtistRemoteEntry(
    val confirmedId: Uuid?,
    val booth: String?,
    val name: String?,
    val summary: String?,
    val links: List<String>,
    val timestamp: Instant,
)
