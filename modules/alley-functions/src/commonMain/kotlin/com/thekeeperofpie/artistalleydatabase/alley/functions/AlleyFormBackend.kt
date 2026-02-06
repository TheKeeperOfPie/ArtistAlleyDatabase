package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormEntry
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormNonce
import com.thekeeperofpie.artistalleydatabase.alley.form.StampRallyFormEntry
import com.thekeeperofpie.artistalleydatabase.alley.functions.form.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import kotlinx.coroutines.await
import kotlinx.io.bytestring.hexToByteString
import kotlinx.serialization.json.Json
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.fetch.Response
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
internal object AlleyFormBackend {

    suspend fun handleRequest(context: EventContext): Response =
        handleFormRequest(
            context = context,
            request = Json.decodeFromString<BackendFormRequest>(
                context.request.text().await(),
            ),
        )

    private suspend fun handleFormRequest(
        context: EventContext,
        request: BackendFormRequest,
    ): Response {
        val signature = context.request.headers.get(AlleyCryptography.SIGNATURE_HEADER_KEY)
            ?: return Utils.unauthorizedResponse

        val messageBytes = Uint8Array(
            Json.encodeToString<BackendFormRequest>(request)
                .encodeToByteArray()
                .toTypedArray()
        )
        val signatureBytes = signature.hexToByteString().toByteArray()

        val formDatabase = Databases.formDatabase(context)
        val publicKeyQueries = formDatabase.alleyFormPublicKeyQueries

        suspend fun tryRecoverPublicKey(recoveryByte: Byte): Pair<Uuid, String>? {
            val bytes = NobleCurves.P384.recoverPublicKey(
                signature = Uint8Array((byteArrayOf(recoveryByte) + signatureBytes).toTypedArray()),
                message = messageBytes,
            ).let { bytes -> ByteArray(bytes.length) { bytes[it] } }
            val candidatePublicKey = AlleyCryptography.convertRawPublicKey(bytes)
            return publicKeyQueries
                .getArtistId(candidatePublicKey)
                .awaitAsOneOrNull()
                ?.let { it to candidatePublicKey }
        }

        val (expectedArtistId, expectedPublicKey) = listOf<Byte>(0, 1, 2, 3)
            .firstNotNullOfOrNull { tryRecoverPublicKey(it) }
            ?: return Utils.unauthorizedResponse

        val valid = AlleyCryptography.verifySignature<BackendFormRequest>(
            publicKey = expectedPublicKey,
            signature = signature,
            payload = request,
        )
        if (!valid) return Utils.unauthorizedResponse

        return with(request) {
            when (this) {
                is BackendFormRequest.Nonce ->
                    generateNonce(
                        artistId = expectedArtistId,
                        database = formDatabase,
                        requestTimestamp = timestamp,
                    )
                        ?.let { makeResponse(it) }
                        ?: Utils.unauthorizedResponse
                is BackendFormRequest.Artist -> makeResponse(
                    loadArtist(
                        context,
                        this,
                        expectedArtistId
                    )
                )
                is BackendFormRequest.ArtistSave -> {
                    if (Uuid.parse(this.afterArtist.id) != expectedArtistId) {
                        Utils.unauthorizedResponse
                    } else {
                        makeResponse(saveArtist(context, this, expectedArtistId))
                    }
                }
            }
        }
    }

    private suspend fun generateNonce(
        artistId: Uuid,
        database: AlleyFormDatabase,
        requestTimestamp: Instant,
    ): Uuid? {
        val timestamp = Clock.System.now()
        if (timestamp > requestTimestamp + 30.minutes) return null
        val nonce = Uuid.generateV7NonMonotonicAt(timestamp)
        database.alleyFormNonceQueries.insertNonce(
            ArtistFormNonce(artistId = artistId, nonce = nonce, timestamp = timestamp)
        )
        return nonce
    }

    private suspend fun loadArtist(
        context: EventContext,
        request: BackendFormRequest.Artist,
        artistId: Uuid,
    ): BackendFormRequest.Artist.Response? {
        val artist = BackendUtils.loadArtist(context, request.dataYear, artistId)
            ?.copy(
                editorNotes = null,
                lastEditor = null,
                lastEditTime = null
            ) // Strip identifying info
            ?: return null

        val booth = artist.booth

        val cacher = KeyValueCacher(context)
        val cachedStampRalliesJson = cacher.getStampRalliesJson()
        val stampRallySummaries = try {
            cachedStampRalliesJson?.let { Json.decodeFromString<List<StampRallySummary>>(it) }
        } catch (_: Exception) {
            null
        } ?: run {
            Databases.editDatabase(context).stampRallyEntryAnimeExpo2026Queries
                .getStampRallies()
                .awaitAsList()
                .map {
                    StampRallySummary(
                        id = it.id,
                        fandom = it.fandom,
                        hostTable = it.hostTable,
                        tables = it.tables,
                        series = it.series,
                    )
                }
                .also { cacher.putStampRallies(it) }
        }
        val stampRallies =
            stampRallySummaries.filter { it.hostTable == booth || it.tables.contains(booth) }
                .mapNotNull {
                    val request = BackendRequest.StampRally(artist.year, it.id)
                    BackendUtils.loadStampRally(context, request)
                }

        val artistFormDiff = BackendUtils.loadArtistFormDiff(context, request.dataYear, artistId)
        val stampRallyFormDiffs = BackendUtils.loadStampRallyFormDiffs(context, request.dataYear, artistId)
        return BackendFormRequest.Artist.Response(
            artist = artist,
            stampRallies = stampRallies,
            artistFormDiff = artistFormDiff,
            stampRallyFormDiffs = stampRallyFormDiffs,
            allStampRallySummaries = stampRallySummaries,
        )
    }

    private suspend fun saveArtist(
        context: EventContext,
        request: BackendFormRequest.ArtistSave,
        artistId: Uuid,
    ): BackendFormRequest.ArtistSave.Response {
        val database = Databases.formDatabase(context)
        try {
            val expectedNonce =
                database.alleyFormNonceQueries.getNonce(artistId).awaitAsOneOrNull()
            if (expectedNonce == null || request.nonce != expectedNonce.nonce) {
                return BackendFormRequest.ArtistSave.Response.Failed("Invalid nonce")
            }
        } finally {
            database.alleyFormNonceQueries.clearNonce(artistId)
        }

        val beforeArtist = request.beforeArtist
        val afterArtist = request.afterArtist
        val timestamp = Clock.System.now()
        val artistFormEntry =
            ArtistFormEntry(
                artistId = artistId,
                dataYear = request.dataYear,
                beforeBooth = beforeArtist.booth,
                beforeName = beforeArtist.name,
                beforeSummary = beforeArtist.summary,
                beforeSocialLinks = beforeArtist.socialLinks,
                beforeStoreLinks = beforeArtist.storeLinks,
                beforePortfolioLinks = beforeArtist.portfolioLinks,
                beforeCatalogLinks = beforeArtist.catalogLinks,
                beforeNotes = beforeArtist.notes,
                beforeCommissions = beforeArtist.commissions,
                beforeSeriesInferred = beforeArtist.seriesInferred,
                beforeSeriesConfirmed = beforeArtist.seriesConfirmed,
                beforeMerchInferred = beforeArtist.merchInferred,
                beforeMerchConfirmed = beforeArtist.merchConfirmed,
                beforeImages = beforeArtist.images,
                afterBooth = afterArtist.booth,
                afterName = afterArtist.name,
                afterSummary = afterArtist.summary,
                afterSocialLinks = afterArtist.socialLinks,
                afterStoreLinks = afterArtist.storeLinks,
                afterPortfolioLinks = afterArtist.portfolioLinks,
                afterCatalogLinks = afterArtist.catalogLinks,
                afterNotes = afterArtist.notes,
                afterCommissions = afterArtist.commissions,
                afterSeriesInferred = afterArtist.seriesInferred,
                afterSeriesConfirmed = afterArtist.seriesConfirmed,
                afterMerchInferred = afterArtist.merchInferred,
                afterMerchConfirmed = afterArtist.merchConfirmed,
                afterImages = afterArtist.images,
                formNotes = request.formNotes,
                timestamp = timestamp,
            )
        val stampRallyFormEntries = request.afterStampRallies.map { after ->
            val before = request.beforeStampRallies.find { it.id == after.id }
            StampRallyFormEntry(
                dataYear = request.dataYear,
                artistId = artistId,
                stampRallyId = after.id,
                beforeFandom = before?.fandom,
                beforeTables = before?.tables,
                beforeLinks = before?.links,
                beforeTableMin = before?.tableMin,
                beforePrize = before?.prize,
                beforePrizeLimit = before?.prizeLimit,
                beforeSeries = before?.series,
                beforeMerch = before?.merch,
                beforeNotes = before?.notes,
                beforeImages = before?.images,
                afterFandom = after.fandom,
                afterTables = after.tables,
                afterLinks = after.links,
                afterTableMin = after.tableMin,
                afterPrize = after.prize,
                afterPrizeLimit = after.prizeLimit,
                afterSeries = after.series,
                afterMerch = after.merch,
                afterNotes = after.notes,
                afterImages = after.images,
                timestamp = timestamp,
            )
        }

        database.artistFormEntryQueries.insertFormEntry(artistFormEntry)
        stampRallyFormEntries.forEach {
            database.stampRallyFormEntryQueries.insertFormEntry(it)
        }
        Databases.editDatabase(context)
            .artistEntryAnimeExpo2026Queries
            .markArtistHasSubmittedForm(artistId.toString())
        return BackendFormRequest.ArtistSave.Response.Success
    }

    private inline fun <reified Request : BackendFormRequest.WithResponse<Response>, reified Response> Request.makeResponse(
        response: Response?,
    ): org.w3c.fetch.Response = if (response == null) {
        Utils.jsonResponse(null)
    } else {
        Utils.jsonResponse<Response>(response)
    }
}
