package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
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
        beforeArtist: ArtistDatabaseEntry.Impl,
        afterArtist: ArtistDatabaseEntry.Impl,
        beforeStampRallies: List<StampRallyDatabaseEntry>,
        afterStampRallies: List<StampRallyDatabaseEntry>,
        deletedRallyIds: List<String>,
        formNotes: String,
    ) = remoteDatabase.saveArtist(
        dataYear = dataYear,
        beforeArtist = beforeArtist,
        afterArtist = afterArtist,
        beforeStampRallies = beforeStampRallies,
        afterStampRallies = afterStampRallies,
        deletedRallyIds = deletedRallyIds,
        formNotes = formNotes,
    )
}
