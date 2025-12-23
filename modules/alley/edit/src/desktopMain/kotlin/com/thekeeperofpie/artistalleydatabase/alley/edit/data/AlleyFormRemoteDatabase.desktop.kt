package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyFormRemoteDatabase(
    private val editDatabase: AlleyEditRemoteDatabase,
) {
    actual suspend fun loadArtist(
        dataYear: DataYear,
        artistId: Uuid,
        privateKey: String,
    ): ArtistDatabaseEntry.Impl? {
        val publicKey = editDatabase.artistPublicKeys[artistId] ?: return null
        val request = BackendFormRequest.Artist(dataYear, artistId)
        val signature = AlleyCryptography.signRequest<BackendFormRequest>(
            privateKey = privateKey,
            payload = request,
        )
        val valid = AlleyCryptography.verifySignature<BackendFormRequest>(
            publicKey = publicKey,
            signature = signature,
            payload = request,
        )
        if (!valid) return null
        return editDatabase.loadArtist(dataYear, artistId)
    }
}
