package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
class AlleyFormDatabase(
    private val remoteDatabase: AlleyFormRemoteDatabase,
) {
    suspend fun loadArtist(dataYear: DataYear, artistId: Uuid, privateKey: String) =
        remoteDatabase.loadArtist(dataYear, artistId, privateKey)
}
