package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class AlleyFormDatabase(
    private val remoteDatabase: AlleyFormRemoteDatabase,
) {
    suspend fun loadArtist(dataYear: DataYear) = remoteDatabase.loadArtist(dataYear)

    suspend fun saveArtist(
        dataYear: DataYear,
        before: ArtistDatabaseEntry.Impl,
        after: ArtistDatabaseEntry.Impl,
    ) = remoteDatabase.saveArtist(
        dataYear = dataYear,
        before = before,
        after = after,
    )
}
