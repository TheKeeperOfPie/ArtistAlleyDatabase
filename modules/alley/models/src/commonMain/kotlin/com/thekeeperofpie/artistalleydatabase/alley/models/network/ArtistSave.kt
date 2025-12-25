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
    sealed interface Response {
        @Serializable
        data object Success : Response
        @Serializable
        data class Outdated(val current: ArtistDatabaseEntry.Impl) : Response
        @Serializable
        data class Failed(val errorMessage: String) : Response
    }
}
