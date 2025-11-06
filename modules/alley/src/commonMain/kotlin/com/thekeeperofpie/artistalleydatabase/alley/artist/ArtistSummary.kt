package com.thekeeperofpie.artistalleydatabase.alley.artist

import kotlin.uuid.Uuid

data class ArtistSummary(
    val id: Uuid,
    val booth: String?,
    val name: String?,
)
