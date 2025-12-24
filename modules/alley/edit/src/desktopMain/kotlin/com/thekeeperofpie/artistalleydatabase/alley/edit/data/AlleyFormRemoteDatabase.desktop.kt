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
        val request = BackendFormRequest.Artist(dataYear, artistId)
        val valid = assertSignature(artistId, privateKey, request)
        if (!valid) return null
        return editDatabase.loadArtist(dataYear, artistId)
            ?.copy(editorNotes = null, lastEditor = null, lastEditTime = null)
    }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        artistId: Uuid,
        privateKey: String,
        before: ArtistDatabaseEntry.Impl,
        after: ArtistDatabaseEntry.Impl,
    ): BackendFormRequest.ArtistSave.Response {
        val fakeNonce = Uuid.random()
        val request = BackendFormRequest.ArtistSave(
            artistId = artistId,
            nonce = fakeNonce,
            dataYear = dataYear,
            before = before,
            after = after,
        )
        val valid = assertSignature(artistId, privateKey, request)
        if (!valid) {
            return BackendFormRequest.ArtistSave.Response.Failed(
                IllegalStateException("Invalid access key")
            )
        }

        editDatabase.artistFormQueue[artistId] = before to after
        return BackendFormRequest.ArtistSave.Response.Success
    }

    private suspend fun assertSignature(
        artistId: Uuid,
        privateKey: String,
        request: BackendFormRequest,
    ): Boolean {
        val publicKey = editDatabase.artistPublicKeys[artistId] ?: return false

        val signature = AlleyCryptography.signRequest<BackendFormRequest>(
            privateKey = privateKey,
            payload = request,
        )
        return AlleyCryptography.verifySignature<BackendFormRequest>(
            publicKey = publicKey,
            signature = signature,
            payload = request,
        )
    }
}
