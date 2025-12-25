package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import kotlinx.serialization.Serializable

object SeriesSave {
    @Serializable
    data class Request(
        val initial: SeriesInfo?,
        val updated: SeriesInfo,
    ): BackendRequest, BackendRequest.WithResponse<Response>

    @Serializable
    data class Response(val result: Result) {
        @Serializable
        sealed interface Result {
            @Serializable
            data object Success : Result
            @Serializable
            data class Outdated(val current: SeriesInfo) : Result
            @Serializable
            data class Failed(val errorMessage: String) : Result
        }
    }
}
