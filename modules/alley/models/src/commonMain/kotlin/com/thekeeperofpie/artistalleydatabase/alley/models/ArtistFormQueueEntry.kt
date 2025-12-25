package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ArtistFormQueueEntry(
    val artistId: Uuid,
    val beforeBooth: String?,
    val beforeName: String?,
    val afterBooth: String?,
    val afterName: String?,
)
