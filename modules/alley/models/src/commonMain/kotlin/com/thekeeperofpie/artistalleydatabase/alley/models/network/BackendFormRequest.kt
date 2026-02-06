package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface BackendFormRequest {

    // Split out because KotlinX Serialization doesn't support <*> star type parameters
    interface WithResponse<Response>

    @Serializable
    data class Nonce(val timestamp: Instant) : BackendFormRequest, WithResponse<Uuid>

    @Serializable
    data class Artist(val dataYear: DataYear) : BackendFormRequest, WithResponse<Artist.Response> {
        @Serializable
        data class Response(
            val artist: ArtistDatabaseEntry.Impl,
            val stampRallies: List<StampRallyDatabaseEntry>,
            val artistFormDiff: ArtistEntryDiff?,
            val stampRallyFormDiffs: List<StampRallyEntryDiff>,
            val allStampRallySummaries: List<StampRallySummary>,
        )
    }

    @Serializable
    data class ArtistSave(
        val nonce: Uuid,
        val dataYear: DataYear,
        val beforeArtist: ArtistDatabaseEntry.Impl,
        val afterArtist: ArtistDatabaseEntry.Impl,
        val beforeStampRallies: List<StampRallyDatabaseEntry>,
        val afterStampRallies: List<StampRallyDatabaseEntry>,
        val formNotes: String,
    ) : BackendFormRequest, WithResponse<ArtistSave.Response> {

        @Serializable
        sealed interface Response {
            @Serializable
            data object Success : Response

            @Serializable
            data class Failed(val errorMessage: String) : Response
        }
    }
}
