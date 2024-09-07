package com.thekeeperofpie.artistalleydatabase.animethemes.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistWithAniList(
    val id: String,
    val resources: List<AniListResource> = emptyList(),
) {
    @Serializable
    data class AniListResource(
        @SerialName("external_id")
        val externalId: Int,
    )
}
