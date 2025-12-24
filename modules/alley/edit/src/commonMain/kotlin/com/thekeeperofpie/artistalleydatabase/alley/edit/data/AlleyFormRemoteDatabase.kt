package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlin.uuid.Uuid

expect class AlleyFormRemoteDatabase {

    suspend fun loadArtist(
        dataYear: DataYear,
        artistId: Uuid,
        privateKey: String,
    ): ArtistDatabaseEntry.Impl?

    suspend fun saveArtist(
        dataYear: DataYear,
        artistId: Uuid,
        privateKey: String,
        before: ArtistDatabaseEntry.Impl,
        after: ArtistDatabaseEntry.Impl,
    ): BackendFormRequest.ArtistSave.Response
}
