package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ArtistFormQueueEntry(
    val artistId: Uuid,
    val booth: String?,
    val name: String?,
)
