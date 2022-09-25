package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import kotlinx.serialization.Serializable

@Serializable
data class ArtistColumnEntry(
    val id: String,
    val names: Map<String, String>,
    val manuallyChosen: Boolean = false,
)