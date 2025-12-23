package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface BackendFormRequest {

    val artistId: Uuid

    // Split out because KotlinX Serialization doesn't support <*> star type parameters
    interface WithResponse<Response>

    @Serializable
    data class Nonce(override val artistId: Uuid, val timestamp: Instant) : BackendFormRequest,
        WithResponse<Uuid>

    @Serializable
    data class Artist(val dataYear: DataYear, override val artistId: Uuid) : BackendFormRequest,
        WithResponse<ArtistDatabaseEntry.Impl>

    @Serializable
    data class ArtistSave(
        override val artistId: Uuid,
        val nonce: Uuid,
        val dataYear: DataYear,
        val initial: ArtistDatabaseEntry.Impl?,
        val updated: ArtistDatabaseEntry.Impl,
    ) : BackendFormRequest, WithResponse<ArtistSave.Response> {

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
}
