package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistDatabaseEntry

expect class AlleyEditRemoteDatabase {
    suspend fun saveArtist(artist: ArtistDatabaseEntry.Impl)
}
