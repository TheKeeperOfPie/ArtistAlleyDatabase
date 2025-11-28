@file:OptIn(ExperimentalUuidApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.async.coroutines.awaitCreate
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toMerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.R2ListOptions
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.ResponseWithBody
import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ListImages
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object AlleyBackendDatabase {
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

    private val seriesEntryAdapter = SeriesEntry.Adapter(
        sourceAdapter = object : ColumnAdapter<SeriesSource, String> {
            override fun decode(databaseValue: String) =
                SeriesSource.entries.find { it.name == databaseValue }
                    ?: SeriesSource.NONE

            override fun encode(value: SeriesSource) = value.name
        },
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
            "image" -> loadImage(context, segments.drop(1).joinToString(separator = "/"))
            "listImages" -> listImages(context)
            "uploadImage" -> uploadImage(context, segments.drop(1).joinToString(separator = "/"))
            "series" -> loadSeries(context)
            "insertSeries" -> insertSeries(context)
            "merch" -> loadMerch(context)
            "insertMerch" -> insertMerch(context)
            else -> null
        } ?: Response(null, ResponseInit(status = 404))
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
        val request = Json.decodeFromString<ArtistSave.Request>(context.request.text().await())
        val database = database(context, tryCreate = true)
        val currentArtist =
            database.artistEntryAnimeExpo2026Queries.getArtist(request.updated.id)
                .awaitAsOneOrNull()?.toArtistDatabaseEntry()
        if (currentArtist != null && currentArtist != request.updated) {
            return jsonResponse(
                ArtistSave.Response(ArtistSave.Response.Result.Outdated(currentArtist))
            )
        }
        database.artistEntryAnimeExpo2026Queries.insertArtist(request.updated.toArtistEntryAnimeExpo2026())
        return jsonResponse(ArtistSave.Response(ArtistSave.Response.Result.Success))
    }

    /**
     * Exposes image for local development so that it doesn't access the remote R2 bucket via the
     * public domain.
     */
    suspend fun loadImage(context: EventContext, key: String): Response {
        val file = context.env.ARTIST_ALLEY_IMAGES_BUCKET.get(key).await()
            ?: return Response(null, ResponseInit(status = 404))
        return Response(file.body, ResponseInit(headers = Headers().apply {
            val contentType = file.httpMetadata.contentType
            if (contentType != null) {
                set("Content-Type", contentType)
            }
        }))
    }

    suspend fun listImages(context: EventContext): Response {
        val request = Json.decodeFromString<ListImages.Request>(context.request.text().await())
        val keys = context.env.ARTIST_ALLEY_IMAGES_BUCKET
            .list(R2ListOptions(request.prefix))
            .await()
            .objects
            .map { it.key }
        return jsonResponse(ListImages.Response(keys))
    }

    suspend fun uploadImage(context: EventContext, path: String): Response {
        context.env.ARTIST_ALLEY_IMAGES_BUCKET
            .put(path, context.request.unsafeCast<ResponseWithBody>().body)
            .await()
        return Response("")
    }

    suspend fun loadSeries(context: EventContext): Response =
        database(context).seriesEntryQueries
            .getSeries()
            .awaitAsList()
            .map { it.toSeriesInfo() }
            .let(::jsonResponse)

    suspend fun insertSeries(context: EventContext): Response {
        val request = Json.decodeFromString<SeriesSave.Request>(context.request.text().await())
        val database = database(context, tryCreate = true)
        val currentSeries =
            database.seriesEntryQueries.getSeriesById(request.updated.id)
                .awaitAsOneOrNull()?.toSeriesInfo()
        if (currentSeries != null && currentSeries != request.updated) {
            return jsonResponse(
                SeriesSave.Response(SeriesSave.Response.Result.Outdated(currentSeries))
            )
        }
        database.seriesEntryQueries.insertSeries(request.updated.toSeriesEntry())
        return jsonResponse(SeriesSave.Response(SeriesSave.Response.Result.Success))
    }

    suspend fun loadMerch(context: EventContext): Response =
        database(context).merchEntryQueries
            .getMerch()
            .awaitAsList()
            .map { it.toMerchInfo() }
            .let(::jsonResponse)

    suspend fun insertMerch(context: EventContext): Response {
        val request = Json.decodeFromString<MerchSave.Request>(context.request.text().await())
        val database = database(context, tryCreate = true)
        val currentMerch =
            database.merchEntryQueries.getMerchById(request.updated.name)
                .awaitAsOneOrNull()?.toMerchInfo()
        if (currentMerch != null && currentMerch != request.updated) {
            return jsonResponse(
                MerchSave.Response(MerchSave.Response.Result.Outdated(currentMerch))
            )
        }
        database.merchEntryQueries.insertMerch(request.updated.toMerchEntry())
        return jsonResponse(MerchSave.Response(MerchSave.Response.Result.Success))
    }

    private suspend fun database(
        context: EventContext,
        tryCreate: Boolean = false,
    ): AlleySqlDatabase {
        val sqlDriver = WorkerSqlDriver(database = context.env.ARTIST_ALLEY_DB)
        val database = AlleySqlDatabase(
            driver = sqlDriver,
            artistEntryAnimeExpo2026Adapter = artistEntryAnimeExpo2026Adapter,
            seriesEntryAdapter = seriesEntryAdapter,
        )
        if (tryCreate) {
            AlleySqlDatabase.Schema.awaitCreate(sqlDriver)
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

    private fun SeriesInfo.toSeriesEntry() = SeriesEntry(
        id = id,
        uuid = uuid.toString(),
        notes = notes,
        aniListId = aniListId,
        aniListType = aniListType.takeUnless { it == AniListType.NONE }?.name,
        wikipediaId = wikipediaId,
        source = source,
        titlePreferred = titlePreferred,
        titleEnglish = titleEnglish,
        titleRomaji = titleRomaji,
        titleNative = titleNative,
        link = link,
        inferred2024 = 0,
        inferred2025 = 0,
        inferredAnimeExpo2026 = 0,
        inferredAnimeNyc2024 = 0,
        inferredAnimeNyc2025 = 0,
        confirmed2024 = 0,
        confirmed2025 = 0,
        confirmedAnimeExpo2026 = 0,
        confirmedAnimeNyc2024 = 0,
        confirmedAnimeNyc2025 = 0,
        counter = 0,
    )

    private fun MerchInfo.toMerchEntry() = MerchEntry(
        name = name,
        uuid = uuid.toString(),
        notes = notes,
        categories = null,
        yearFlags = 0L,
    )
}
