@file:OptIn(ExperimentalUuidApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.async.coroutines.awaitCreate
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object Database {
    private val artistEntryAnimeExpo2026Adapter = ArtistEntryAnimeExpo2026.Adapter(
        linksAdapter = ColumnAdapters.listStringAdapter,
        storeLinksAdapter = ColumnAdapters.listStringAdapter,
        catalogLinksAdapter = ColumnAdapters.listStringAdapter,
        commissionsAdapter = ColumnAdapters.listStringAdapter,
        seriesInferredAdapter = ColumnAdapters.listStringAdapter,
        seriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
        merchInferredAdapter = ColumnAdapters.listStringAdapter,
        merchConfirmedAdapter = ColumnAdapters.listStringAdapter,
        imagesAdapter = ColumnAdapters.listCatalogImageAdapter,
    )

    suspend fun handleRequest(context: EventContext, path: String): Response {
        val segments = path.removePrefix("/").split("/")
        return when (segments.getOrNull(0)) {
            "artists" -> loadArtists(context)
            "artist" -> try {
                loadArtist(context, Uuid.parse(segments[1]))
            } catch (_: IllegalArgumentException) {
                null
            }
            "insertArtist" -> insertArtist(context)
            else -> null
        } ?: Response("", ResponseInit(status = 404))
    }

    suspend fun loadArtists(context: EventContext): Response =
        database(context).artistEntryAnimeExpo2026Queries
            .getArtists()
            .awaitAsList()
            .map {
                ArtistSummary(
                    id = Uuid.parse(it.id),
                    booth = it.booth,
                    name = it.name,
                )
            }
            .let(::jsonResponse)

    suspend fun loadArtist(context: EventContext, artistId: Uuid): Response =
        database(context)
            .artistEntryAnimeExpo2026Queries
            .getArtist(artistId.toString())
            .awaitAsOneOrNull()
            ?.toArtistDatabaseEntry()
            .let(::jsonResponse)

    suspend fun insertArtist(context: EventContext): Response {
        val artist = Json.decodeFromString<ArtistDatabaseEntry.Impl>(context.request.text().await())
        database(context, tryCreate = true)
            .artistEntryAnimeExpo2026Queries
            .insertArtist(artist.toArtistEntryAnimeExpo2026())
        return Response("")
    }

    private suspend fun database(
        context: EventContext,
        tryCreate: Boolean = false,
    ): AlleyFunctionsDatabase {
        val sqlDriver = WorkerSqlDriver(database = context.env.ARTIST_ALLEY_DB)
        val database = AlleyFunctionsDatabase(
            driver = sqlDriver,
            artistEntryAnimeExpo2026Adapter = artistEntryAnimeExpo2026Adapter,
        )
        if (tryCreate) {
            AlleyFunctionsDatabase.Schema.awaitCreate(sqlDriver)
        }
        return database
    }

    private inline fun <reified T> jsonResponse(value: T) = Response(
        body = Json.encodeToString(value),
        init = ResponseInit(status = 200, headers = Headers().apply {
            set("Content-Type", "application/json")
        })
    )

    private fun ArtistDatabaseEntry.Impl.toArtistEntryAnimeExpo2026() =
        ArtistEntryAnimeExpo2026(
            id = id,
            booth = booth,
            name = name,
            summary = summary,
            links = links,
            storeLinks = storeLinks,
            catalogLinks = catalogLinks,
            linkFlags = 0,
            linkFlags2 = 0,
            driveLink = driveLink,
            notes = notes,
            commissions = commissions,
            commissionFlags = 0,
            seriesInferred = seriesInferred,
            seriesConfirmed = seriesConfirmed,
            merchInferred = merchInferred,
            merchConfirmed = merchConfirmed,
            images = images,
            counter = counter,
        )

    private fun ArtistEntryAnimeExpo2026.toArtistDatabaseEntry() =
        ArtistDatabaseEntry.Impl(
            year = DataYear.ANIME_EXPO_2026,
            id = id,
            booth = booth,
            name = name,
            summary = summary,
            links = links,
            storeLinks = storeLinks,
            catalogLinks = catalogLinks,
            driveLink = driveLink,
            notes = notes,
            commissions = commissions,
            seriesInferred = seriesInferred,
            seriesConfirmed = seriesConfirmed,
            merchInferred = merchInferred,
            merchConfirmed = merchConfirmed,
            images = images,
            counter = counter,
        )
}
