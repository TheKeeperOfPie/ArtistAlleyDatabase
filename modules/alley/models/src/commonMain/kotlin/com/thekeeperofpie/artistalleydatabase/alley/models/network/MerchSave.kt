package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import kotlinx.serialization.Serializable

object MerchSave {
    @Serializable
    data class Request(
        val initial: MerchInfo?,
        val updated: MerchInfo,
    ): BackendRequest, BackendRequest.WithResponse<Response>

    @Serializable
    data class Response(val result: Result) {
        @Serializable
        sealed interface Result {
            @Serializable
            data object Success : Result
            @Serializable
            data class Outdated(val current: MerchInfo) : Result
            data class Failed(val throwable: Throwable) : Result
        }
    }
}
