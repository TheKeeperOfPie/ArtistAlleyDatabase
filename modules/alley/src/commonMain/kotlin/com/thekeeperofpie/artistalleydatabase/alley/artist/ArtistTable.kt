package com.thekeeperofpie.artistalleydatabase.alley.artist

import kotlinx.serialization.Serializable

@Serializable
data class ArtistTable(
    val booth: String,
    val name: String?,
)
