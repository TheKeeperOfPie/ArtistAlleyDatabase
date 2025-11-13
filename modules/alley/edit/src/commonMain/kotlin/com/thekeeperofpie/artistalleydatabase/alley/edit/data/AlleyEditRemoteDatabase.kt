package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditInfo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlin.uuid.Uuid

expect class AlleyEditRemoteDatabase {
    suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistEditInfo?
    suspend fun loadArtists(dataYear: DataYear): List<ArtistSummary>
    suspend fun saveArtist(dataYear: DataYear, artist: ArtistDatabaseEntry.Impl)
}
