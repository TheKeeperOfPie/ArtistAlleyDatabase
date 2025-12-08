package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface BackendRequest {

    // Split out because KotlinX Serialization doesn't support <*> star type parameters
    interface WithResponse<Response>

    @Serializable
    data class Artist(val artistId: Uuid) : BackendRequest, BackendRequest.WithResponse<ArtistDatabaseEntry.Impl>

    @Serializable
    data object Artists : BackendRequest, BackendRequest.WithResponse<List<ArtistSummary>>

    @Serializable
    data object Series : BackendRequest, BackendRequest.WithResponse<List<SeriesInfo>>

    @Serializable
    data object Merch : BackendRequest, BackendRequest.WithResponse<List<MerchInfo>>
}
