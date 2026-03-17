package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.ImageFileData
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyEntryDiff
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
        val deletedRallyIds: List<String>,
        val formNotes: String,
    ) : BackendFormRequest, WithResponse<ArtistSave.Response> {

        @Serializable
        sealed interface Response {
            @Serializable
            data class Success(val stampRallies: List<StampRallyDatabaseEntry>) : Response

            @Serializable
            data class Failed(val errorMessage: String) : Response
        }
    }

    @Serializable
    data class UploadImageUrls(
        val dataYear: DataYear,
        val artistId: Uuid,
        val artistImageData: List<ImageFileData>,
        val stampRallyIdsToImageData: Map<String, List<ImageFileData>>,
    ) : BackendFormRequest, WithResponse<UploadImageUrls.Response> {
        @Serializable
        sealed interface Response {
            @Serializable
            data class Success(
                val artistUrls: Map<Uuid, String>,
                val stampRallyUrls: Map<String, Map<Uuid, String>>,
            ) : Response

            @Serializable
            data class Failed(val errorMessage: String) : Response
        }
    }
}
