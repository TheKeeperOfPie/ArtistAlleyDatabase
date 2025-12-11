package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable

object ArtistSave {
    @Serializable
    data class Request(
        val dataYear: DataYear,
        val initial: ArtistDatabaseEntry.Impl?,
        val updated: ArtistDatabaseEntry.Impl,
    ): BackendRequest, BackendRequest.WithResponse<Response>

    @Serializable
    data class Response(val result: Result) {
        @Serializable
        sealed interface Result {
            @Serializable
            data object Success : Result
            @Serializable
            data class Outdated(val current: ArtistDatabaseEntry.Impl) : Result
            data class Failed(val throwable: Throwable) : Result
        }
    }
}
