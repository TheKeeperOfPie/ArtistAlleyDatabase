package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlin.uuid.Uuid

data class ArtistFormQueueEntry(
    val artistId: Uuid,
    val beforeBooth: String?,
    val beforeName: String?,
    val afterBooth: String?,
    val afterName: String?,
)
