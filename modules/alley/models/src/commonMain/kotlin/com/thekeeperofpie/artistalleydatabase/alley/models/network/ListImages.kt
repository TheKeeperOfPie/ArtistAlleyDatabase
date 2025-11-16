package com.thekeeperofpie.artistalleydatabase.alley.models.network

import kotlinx.serialization.Serializable

class ListImages {
    @Serializable
    data class Request(
        val prefix: String,
    )

    @Serializable
    data class Response(
        val keys: List<String>,
    )
}
