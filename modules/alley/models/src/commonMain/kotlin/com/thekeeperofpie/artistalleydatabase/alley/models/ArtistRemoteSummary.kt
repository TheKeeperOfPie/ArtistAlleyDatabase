package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class ArtistRemoteSummary(
    val confirmedId: Uuid?,
    val booth: String,
    val name: String,
    val timestamp: Instant,
) {
    val id = ArtistRemoteEntry.Id(booth, name)
}
