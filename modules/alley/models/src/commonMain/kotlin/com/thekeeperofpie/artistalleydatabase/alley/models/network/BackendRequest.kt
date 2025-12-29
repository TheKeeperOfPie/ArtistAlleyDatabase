package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface BackendRequest {

    // Split out because KotlinX Serialization doesn't support <*> star type parameters
    interface WithResponse<Response>

    @Serializable
    data class Artist(val dataYear: DataYear, val artistId: Uuid) : BackendRequest,
        WithResponse<ArtistDatabaseEntry.Impl>

    @Serializable
    data object ArtistFormHistory : BackendRequest, WithResponse<List<ArtistFormHistoryEntry>>

    @Serializable
    data object ArtistFormQueue : BackendRequest, WithResponse<List<ArtistFormQueueEntry>>

    @Serializable
    data class ArtistWithFormEntry(val dataYear: DataYear, val artistId: Uuid) : BackendRequest,
        WithResponse<ArtistWithFormEntry.Response> {
        @Serializable
        data class Response(
            val artist: ArtistDatabaseEntry.Impl,
            val formDiff: ArtistEntryDiff,
        )
    }

    @Serializable
    data class ArtistHistory(val dataYear: DataYear, val artistId: Uuid) : BackendRequest,
        WithResponse<List<ArtistHistoryEntry>>

    @Serializable
    data class ArtistCommitForm(
        val dataYear: DataYear,
        val initial: ArtistDatabaseEntry.Impl,
        val updated: ArtistDatabaseEntry.Impl,
        val formEntryTimestamp: Instant,
    ) : BackendRequest, WithResponse<ArtistCommitForm.Response> {
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

    @Serializable
    data object Artists : BackendRequest, WithResponse<List<ArtistSummary>>

    // TODO: Allow querying presence?
    @Serializable
    data class GenerateFormKey(val artistId: Uuid, val publicKeyForResponse: String) :
        BackendRequest, WithResponse<String>

    @Serializable
    data object Series : BackendRequest, WithResponse<List<SeriesInfo>>

    @Serializable
    data object Merch : BackendRequest, WithResponse<List<MerchInfo>>
}
