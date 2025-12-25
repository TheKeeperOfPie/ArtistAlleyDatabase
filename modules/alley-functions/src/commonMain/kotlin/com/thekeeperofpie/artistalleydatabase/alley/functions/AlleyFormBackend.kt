package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormEntry
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormNonce
import com.thekeeperofpie.artistalleydatabase.alley.functions.form.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
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
        val formDatabase = Databases.formDatabase(context)
        val artistId = request.artistId
        val publicKey = formDatabase
            .alleyFormPublicKeyQueries
            .getPublicKey(artistId)
            .awaitAsOneOrNull()
            ?: return Utils.unauthorizedResponse

        val valid = AlleyCryptography.verifySignature<BackendFormRequest>(
            publicKey = publicKey.publicKey,
            signature = signature,
            payload = request,
        )
        if (!valid) return Utils.unauthorizedResponse

        return with(request) {
            when (this) {
                is BackendFormRequest.Nonce ->
                    generateNonce(
                        artistId = artistId,
                        database = formDatabase,
                        requestTimestamp = timestamp,
                    )
                        ?.let { makeResponse(it) }
                        ?: Utils.unauthorizedResponse
                is BackendFormRequest.Artist -> makeResponse(loadArtist(context, this))
                is BackendFormRequest.ArtistSave -> makeResponse(saveArtist(context, this))
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
    ): ArtistDatabaseEntry.Impl? =
        when (request.dataYear) {
            DataYear.ANIME_EXPO_2026 -> Databases.editDatabase(context)
                .artistEntryAnimeExpo2026Queries
                .getArtist(request.artistId.toString())
                .awaitAsOneOrNull()
                ?.toArtistDatabaseEntry()
                ?.copy(
                    editorNotes = null,
                    lastEditor = null,
                    lastEditTime = null
                ) // Strip identifying info
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> null // TODO: Return legacy years?
        }

    private suspend fun saveArtist(
        context: EventContext,
        request: BackendFormRequest.ArtistSave,
    ): BackendFormRequest.ArtistSave.Response {
        val database = Databases.formDatabase(context)
        try {
            val expectedNonce =
                database.alleyFormNonceQueries.getNonce(request.artistId).awaitAsOneOrNull()
            if (expectedNonce == null || request.nonce != expectedNonce.nonce) {
                return BackendFormRequest.ArtistSave.Response.Failed("Invalid nonce")
            }
        } finally {
            database.alleyFormNonceQueries.clearNonce(request.artistId)
        }

        val before = request.before
        val after = request.after
        database.artistFormEntryQueries.insertFormEntry(
            ArtistFormEntry(
                artistId = request.artistId,
                dataYear = request.dataYear,
                beforeBooth = before.booth,
                beforeName = before.name,
                beforeSummary = before.summary,
                beforeLinks = before.links,
                beforeStoreLinks = before.storeLinks,
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
                afterLinks = after.links,
                afterStoreLinks = after.storeLinks,
                afterCatalogLinks = after.catalogLinks,
                afterNotes = after.notes,
                afterCommissions = after.commissions,
                afterSeriesInferred = after.seriesInferred,
                afterSeriesConfirmed = after.seriesConfirmed,
                afterMerchInferred = after.merchInferred,
                afterMerchConfirmed = after.merchConfirmed,
                afterImages = after.images,
                timestamp = Clock.System.now(),
            )
        )
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
