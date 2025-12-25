package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyFormRemoteDatabase(
    private val editDatabase: AlleyEditRemoteDatabase,
) {
    actual suspend fun loadArtist(dataYear: DataYear): ArtistDatabaseEntry.Impl? {
        val request = BackendFormRequest.Artist(dataYear)
        val artistId = assertSignatureAndGetArtistId(request) ?: return null
        return editDatabase.loadArtist(dataYear, artistId)
            ?.copy(editorNotes = null, lastEditor = null, lastEditTime = null)
    }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        before: ArtistDatabaseEntry.Impl,
        after: ArtistDatabaseEntry.Impl,
    ): BackendFormRequest.ArtistSave.Response {
        val fakeNonce = Uuid.random()
        val request = BackendFormRequest.ArtistSave(
            nonce = fakeNonce,
            dataYear = dataYear,
            before = before,
            after = after,
        )
        val artistId = assertSignatureAndGetArtistId(request)
            ?: return BackendFormRequest.ArtistSave.Response.Failed("Invalid access key")

        editDatabase.artistFormQueue[artistId] = Triple(Clock.System.now(), before, after)
        return BackendFormRequest.ArtistSave.Response.Success
    }

    private suspend fun assertSignatureAndGetArtistId(request: BackendFormRequest): Uuid? {
        val accessKey = ArtistFormAccessKey.key ?: return null
        val artistId = editDatabase.artistKeys.entries
            .find { it.value.privateKey == accessKey }
            ?.key
            ?: return null
        val publicKey = editDatabase.artistKeys[artistId]?.publicKey ?: return null

        val signature = AlleyCryptography.signRequest<BackendFormRequest>(
            privateKey = accessKey,
            payload = request,
        )
        val valid = AlleyCryptography.verifySignature<BackendFormRequest>(
            publicKey = publicKey,
            signature = signature,
            payload = request,
        )
        return artistId.takeIf { valid }
    }
}
