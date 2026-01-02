package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormEntry
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormNonce
import com.thekeeperofpie.artistalleydatabase.alley.functions.form.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
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
                    if (Uuid.parse(this.after.id) != expectedArtistId) {
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

        val formDiff = BackendUtils.loadFormDiff(context, request.dataYear, artistId)
        return BackendFormRequest.Artist.Response(artist = artist, formDiff = formDiff)
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

        val before = request.before
        val after = request.after
        database.artistFormEntryQueries.insertFormEntry(
            ArtistFormEntry(
                artistId = artistId,
                dataYear = request.dataYear,
                beforeBooth = before.booth,
                beforeName = before.name,
                beforeSummary = before.summary,
                beforeSocialLinks = before.socialLinks,
                beforeStoreLinks = before.storeLinks,
                beforePortfolioLinks = before.portfolioLinks,
                beforeCatalogLinks = before.catalogLinks,
                beforeNotes = before.notes,
                beforeCommissions = before.commissions,
                beforeSeriesInferred = before.seriesInferred,
                beforeSeriesConfirmed = before.seriesConfirmed,
                beforeMerchInferred = before.merchInferred,
                beforeMerchConfirmed = before.merchConfirmed,
                beforeImages = before.images,
                afterBooth = after.booth,
                afterName = after.name,
                afterSummary = after.summary,
                afterSocialLinks = after.socialLinks,
                afterStoreLinks = after.storeLinks,
                afterPortfolioLinks = after.portfolioLinks,
                afterCatalogLinks = after.catalogLinks,
                afterNotes = after.notes,
                afterCommissions = after.commissions,
                afterSeriesInferred = after.seriesInferred,
                afterSeriesConfirmed = after.seriesConfirmed,
                afterMerchInferred = after.merchInferred,
                afterMerchConfirmed = after.merchConfirmed,
                afterImages = after.images,
                formNotes = request.formNotes,
                timestamp = Clock.System.now(),
            )
        )
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
