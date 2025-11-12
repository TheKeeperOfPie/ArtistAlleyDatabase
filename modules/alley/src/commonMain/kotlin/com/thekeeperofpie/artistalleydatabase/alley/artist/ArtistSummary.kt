package com.thekeeperofpie.artistalleydatabase.alley.artist

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ArtistSummary(
    val id: Uuid,
    val booth: String?,
    val name: String?,
)
