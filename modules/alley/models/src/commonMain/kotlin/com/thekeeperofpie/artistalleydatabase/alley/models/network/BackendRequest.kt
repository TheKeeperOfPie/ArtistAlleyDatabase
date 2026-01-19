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
    data class Artist(
        val dataYear: DataYear,
        val artistId: Uuid,
    ) : BackendRequest, WithResponse<ArtistDatabaseEntry.Impl?>

    @Serializable
    data class ArtistWithFormMetadata(
        val dataYear: DataYear,
        val artistId: Uuid,
    ) : BackendRequest, WithResponse<ArtistWithFormMetadata.Response?> {
        @Serializable
        data class Response(
            val artist: ArtistDatabaseEntry.Impl,
            val hasPendingFormSubmission: Boolean,
            val hasFormLink: Boolean,
        )
    }

    @Serializable
    data object ArtistFormHistory : BackendRequest, WithResponse<List<ArtistFormHistoryEntry>>

    @Serializable
    data object ArtistFormQueue : BackendRequest, WithResponse<List<ArtistFormQueueEntry>>

    @Serializable
    data class ArtistWithFormEntry(
        val dataYear: DataYear,
        val artistId: Uuid,
    ) : BackendRequest, WithResponse<ArtistWithFormEntry.Response> {
        @Serializable
        data class Response(
            val artist: ArtistDatabaseEntry.Impl,
            val formDiff: ArtistEntryDiff,
        )
    }

    @Serializable
    data class ArtistWithHistoricalFormEntry(
        val dataYear: DataYear,
        val artistId: Uuid,
        val formTimestamp: Instant,
    ) : BackendRequest, WithResponse<ArtistWithHistoricalFormEntry.Response> {

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
    data class ArtistDelete(
        val dataYear: DataYear,
        val expected: ArtistDatabaseEntry.Impl,
    ) : BackendRequest, WithResponse<ArtistDelete.Response> {
        @Serializable
        sealed interface Response {
            @Serializable
            data object Success : Response

            @Serializable
            data class Outdated(val current: ArtistDatabaseEntry.Impl?) : Response

            @Serializable
            data class Failed(val errorMessage: String) : Response
        }
    }

    @Serializable
    data object Artists : BackendRequest, WithResponse<List<ArtistSummary>>

    @Serializable
    data class ArtistSave(
        val dataYear: DataYear,
        val initial: ArtistDatabaseEntry.Impl?,
        val updated: ArtistDatabaseEntry.Impl,
    ) : BackendRequest, WithResponse<ArtistSave.Response> {
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
    data object DatabaseCreate : BackendRequest, WithResponse<Unit>

    // TODO: Allow querying presence?
    @Serializable
    data class GenerateFormKey(
        val artistId: Uuid,
        val publicKeyForResponse: String,

        /**
         * Used to ensure the editor wants to regenerate a form link, and didn't just accidentally
         * send the request twice somehow
         */
        val forceRegenerate: Boolean,
    ) : BackendRequest, WithResponse<String>

    @Serializable
    data class FakeArtistData(val publicKeyForResponse: String) : BackendRequest,
        WithResponse<String?>

    @Serializable
    data object DeleteFakeArtistData : BackendRequest, WithResponse<Unit>

    @Serializable
    data object Series : BackendRequest, WithResponse<List<SeriesInfo>>

    @Serializable
    data class SeriesDelete(val expected: SeriesInfo) : BackendRequest,
        WithResponse<SeriesDelete.Response> {
        @Serializable
        sealed interface Response {
            @Serializable
            data object Success : Response

            @Serializable
            data class Outdated(val current: SeriesInfo?) : Response

            @Serializable
            data class Failed(val errorMessage: String) : Response
        }
    }

    @Serializable
    data class SeriesSave(
        val initial: SeriesInfo?,
        val updated: SeriesInfo,
    ) : BackendRequest, WithResponse<SeriesSave.Response> {
        @Serializable
        sealed interface Response {
            @Serializable
            data object Success : Response

            @Serializable
            data class Outdated(val current: SeriesInfo) : Response

            @Serializable
            data class Failed(val errorMessage: String) : Response
        }
    }

    @Serializable
    data object Merch : BackendRequest, WithResponse<List<MerchInfo>>

    @Serializable
    data class MerchDelete(val expected: MerchInfo) : BackendRequest,
        WithResponse<MerchDelete.Response> {
        @Serializable
        sealed interface Response {
            @Serializable
            data object Success : Response

            @Serializable
            data class Outdated(val current: MerchInfo?) : Response

            @Serializable
            data class Failed(val errorMessage: String) : Response
        }
    }

    @Serializable
    data class MerchSave(
        val initial: MerchInfo?,
        val updated: MerchInfo,
    ) : BackendRequest, WithResponse<MerchSave.Response> {

        @Serializable
        sealed interface Response {
            @Serializable
            data object Success : Response

            @Serializable
            data class Outdated(val current: MerchInfo) : Response

            @Serializable
            data class Failed(val errorMessage: String) : Response
        }
    }
}
