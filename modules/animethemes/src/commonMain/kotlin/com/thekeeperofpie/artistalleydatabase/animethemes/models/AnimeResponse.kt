package com.thekeeperofpie.artistalleydatabase.animethemes.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeResponse(
    val anime: List<Anime>,
    val meta: Meta,
) {
    @Serializable
    data class Meta(
        @SerialName("current_page")
        val currentPage: Int,
        @SerialName("per_page")
        val perPage: Int,
    )
}
