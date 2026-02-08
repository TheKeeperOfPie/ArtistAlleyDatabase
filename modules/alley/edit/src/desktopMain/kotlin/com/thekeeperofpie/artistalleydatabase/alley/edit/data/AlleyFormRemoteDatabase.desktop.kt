package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
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
    actual suspend fun loadArtist(dataYear: DataYear): BackendFormRequest.Artist.Response? {
        val request = BackendFormRequest.Artist(dataYear)
        val artistId = assertSignatureAndGetArtistId(request) ?: return null
        val artist = editDatabase.loadArtist(dataYear, artistId)
            ?.copy(editorNotes = null, lastEditor = null, lastEditTime = null)
            ?: return null
        val formSubmission = editDatabase.artistFormQueue[artistId]
        val artistFormDiff = formSubmission?.toArtistEntryDiff()
        val stampRallyFormDiffs = editDatabase.stampRallyFormQueue
            .filter { it.key.first == artistId }
            .map { it.value.toStampRallyEntryDiff() }

        val booth = artist.booth
        val stampRallySummaries = editDatabase.loadStampRallies(dataYear)
        val stampRallies =
            stampRallySummaries.filter { it.hostTable == booth || it.tables.contains(booth) }
                .mapNotNull { editDatabase.loadStampRally(dataYear, it.id) }

        return BackendFormRequest.Artist.Response(
            artist = artist,
            stampRallies = stampRallies,
            artistFormDiff = artistFormDiff,
            stampRallyFormDiffs = stampRallyFormDiffs,
            allStampRallySummaries = stampRallySummaries
        )
    }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        beforeArtist: ArtistDatabaseEntry.Impl,
        afterArtist: ArtistDatabaseEntry.Impl,
        beforeStampRallies: List<StampRallyDatabaseEntry>,
        afterStampRallies: List<StampRallyDatabaseEntry>,
        formNotes: String,
    ): BackendFormRequest.ArtistSave.Response {
        val fakeNonce = Uuid.random()
        val request = BackendFormRequest.ArtistSave(
            nonce = fakeNonce,
            dataYear = dataYear,
            beforeArtist = beforeArtist,
            afterArtist = afterArtist,
            beforeStampRallies = beforeStampRallies,
            afterStampRallies = afterStampRallies,
            formNotes = formNotes,
        )
        val artistId = assertSignatureAndGetArtistId(request)
            ?: return BackendFormRequest.ArtistSave.Response.Failed("Invalid access key")

        editDatabase.artistFormQueue[artistId] =
            AlleyEditRemoteDatabase.ArtistFormSubmission(
                before = beforeArtist,
                after = afterArtist,
                formNotes = formNotes,
            )

        afterStampRallies.forEach { after ->
            val before = beforeStampRallies.find { it.id == after.id }
            val stampRallyId = if (before == null || Uuid.parseOrNull(after.id) == null) {
                Uuid.random().toString()
            } else {
                after.id
            }
            editDatabase.stampRallyFormQueue[artistId to stampRallyId] =
                AlleyEditRemoteDatabase.StampRallyFormSubmission(
                    artistId = artistId,
                    before = before,
                    after = after.copy(id = stampRallyId),
                )
        }

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
