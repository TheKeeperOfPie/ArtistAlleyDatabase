package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface BackendRequest {

    // Split out because KotlinX Serialization doesn't support <*> star type parameters
    interface WithResponse<Response>

    @Serializable
    data class Artist(val dataYear: DataYear, val artistId: Uuid) : BackendRequest,
        WithResponse<ArtistDatabaseEntry.Impl>

    @Serializable
    data class ArtistHistory(val dataYear: DataYear, val artistId: Uuid) : BackendRequest,
        WithResponse<List<ArtistHistoryEntry>>

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
