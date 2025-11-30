package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import kotlinx.serialization.Serializable

object ArtistSave {
    @Serializable
    data class Request(
        val initial: ArtistDatabaseEntry.Impl?,
        val updated: ArtistDatabaseEntry.Impl,
    )

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
