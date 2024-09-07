package com.thekeeperofpie.artistalleydatabase.animethemes.models

import kotlinx.serialization.Serializable

@Serializable
data class ArtistResponse(
    val artist: ArtistWithAniList
)
