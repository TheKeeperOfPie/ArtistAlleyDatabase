package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyFormRemoteDatabase(
    private val editDatabase: AlleyEditRemoteDatabase,
) {
    init {
        runBlocking {
            DebugTestData.initialize(editDatabase, this@AlleyFormRemoteDatabase)
            editDatabase.simulatedLatency = 3.seconds
        }
    }

    actual suspend fun loadArtist(dataYear: DataYear): BackendFormRequest.Artist.Response? {
        val request = BackendFormRequest.Artist(dataYear)
        val artistId = assertSignatureAndGetArtistId(request) ?: return null
        val artist = editDatabase.loadArtist(dataYear, artistId)
            ?.copy(editorNotes = null, lastEditor = null, lastEditTime = null)
            ?: return null
        val formSubmission = editDatabase.artistFormQueue[artistId]
        val artistFormDiff = formSubmission?.let {
            val before = it.beforeArtist
            val after = it.afterArtist
            ArtistEntryDiff(
                booth = after.booth.orEmpty()
                    .takeIf { it != before.booth.orEmpty() },
                name = after.name
                    .takeIf { it != before.name },
                summary = after.summary.orEmpty()
                    .takeIf { it != before.summary.orEmpty() },
                notes = after.notes.orEmpty()
                    .takeIf { it != before.notes.orEmpty() },
                socialLinks = HistoryListDiff.diffList(before.socialLinks, after.socialLinks),
                storeLinks = HistoryListDiff.diffList(
                    before.storeLinks,
                    after.storeLinks
                ),
                portfolioLinks = HistoryListDiff.diffList(
                    before.portfolioLinks,
                    after.portfolioLinks
                ),
                catalogLinks = HistoryListDiff.diffList(
                    before.catalogLinks,
                    after.catalogLinks
                ),
                commissions = HistoryListDiff.diffList(
                    before.commissions,
                    after.commissions
                ),
                seriesInferred = HistoryListDiff.diffList(
                    before.seriesInferred,
                    after.seriesInferred
                ),
                seriesConfirmed = HistoryListDiff.diffList(
                    before.seriesConfirmed,
                    after.seriesConfirmed
                ),
                merchInferred = HistoryListDiff.diffList(
                    before.merchInferred,
                    after.merchInferred
                ),
                merchConfirmed = HistoryListDiff.diffList(
                    before.merchConfirmed,
                    after.merchConfirmed
                ),
                formNotes = it.formNotes,
                timestamp = it.timestamp,
            )
        }

        val stampRallyFormDiffs = formSubmission?.let {
            val beforeRallies = it.beforeRallies
            it.afterRallies.map { after ->
                val before = beforeRallies.find { it.id == after.id }
                StampRallyEntryDiff(
                    id = after.id,
                    fandom = after.fandom.orEmpty()
                        .takeIf { it != before?.fandom.orEmpty() },
                    tables = HistoryListDiff.diffList(before?.tables, after.tables),
                    links = HistoryListDiff.diffList(before?.links, after.links),
                    tableMin = after.tableMin.takeIf { it != before?.tableMin },
                    prize = after.prize.takeIf { it != before?.prize },
                    prizeLimit = after.prizeLimit.takeIf { it != before?.prizeLimit },
                    series = HistoryListDiff.diffList(before?.series, after.series),
                    merch = HistoryListDiff.diffList(before?.merch, after.merch),
                    notes = after.notes.takeIf { it != before?.notes },
                )
            }
        }.orEmpty()

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
            AlleyEditRemoteDatabase.FormSubmission(
                beforeArtist = beforeArtist,
                afterArtist = afterArtist,
                beforeRallies = beforeStampRallies,
                afterRallies = afterStampRallies,
                formNotes = formNotes,
            )
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
