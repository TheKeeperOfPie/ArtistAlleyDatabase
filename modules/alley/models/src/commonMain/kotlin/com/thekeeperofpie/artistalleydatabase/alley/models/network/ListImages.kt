package com.thekeeperofpie.artistalleydatabase.alley.models.network

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

class ListImages {
    @Serializable
    data class Request(
        val prefix: String,
    )

    @Serializable
    data class Response(
        val idsAndKeys: List<Pair<Uuid, String>>,
    )
}
