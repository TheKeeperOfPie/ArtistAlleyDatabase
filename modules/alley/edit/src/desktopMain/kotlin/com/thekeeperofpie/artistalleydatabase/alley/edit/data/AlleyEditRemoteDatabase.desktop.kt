package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistDatabaseEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase {
    actual suspend fun saveArtist(artist: ArtistDatabaseEntry.Impl) {
        ConsoleLogger.log("insertArtist $artist")
    }
}
