package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
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
        val formDiff = editDatabase.artistFormQueue[artistId]
            ?.let {
                val before = it.before
                val after = it.after
                ArtistEntryDiff(
                    booth = after.booth.orEmpty()
                        .takeIf { it != before.booth.orEmpty() },
                    name = after.name
                        .takeIf { it != before.name },
                    summary = after.summary.orEmpty()
                        .takeIf { it != before.summary.orEmpty() },
                    notes = after.notes.orEmpty()
                        .takeIf { it != before.notes.orEmpty() },
                    socialLinks = ArtistEntryDiff.diffList(before.socialLinks, after.socialLinks),
                    storeLinks = ArtistEntryDiff.diffList(
                        before.storeLinks,
                        after.storeLinks
                    ),
                    catalogLinks = ArtistEntryDiff.diffList(
                        before.catalogLinks,
                        after.catalogLinks
                    ),
                    commissions = ArtistEntryDiff.diffList(
                        before.commissions,
                        after.commissions
                    ),
                    seriesInferred = ArtistEntryDiff.diffList(
                        before.seriesInferred,
                        after.seriesInferred
                    ),
                    seriesConfirmed = ArtistEntryDiff.diffList(
                        before.seriesConfirmed,
                        after.seriesConfirmed
                    ),
                    merchInferred = ArtistEntryDiff.diffList(
                        before.merchInferred,
                        after.merchInferred
                    ),
                    merchConfirmed = ArtistEntryDiff.diffList(
                        before.merchConfirmed,
                        after.merchConfirmed
                    ),
                    formNotes = it.formNotes,
                    timestamp = it.timestamp,
                )
            }

        return BackendFormRequest.Artist.Response(artist, formDiff)
    }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        before: ArtistDatabaseEntry.Impl,
        after: ArtistDatabaseEntry.Impl,
        formNotes: String,
    ): BackendFormRequest.ArtistSave.Response {
        val fakeNonce = Uuid.random()
        val request = BackendFormRequest.ArtistSave(
            nonce = fakeNonce,
            dataYear = dataYear,
            before = before,
            after = after,
            formNotes = formNotes,
        )
        val artistId = assertSignatureAndGetArtistId(request)
            ?: return BackendFormRequest.ArtistSave.Response.Failed("Invalid access key")

        editDatabase.artistFormQueue[artistId] =
            AlleyEditRemoteDatabase.FormSubmission(before, after, formNotes)
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
