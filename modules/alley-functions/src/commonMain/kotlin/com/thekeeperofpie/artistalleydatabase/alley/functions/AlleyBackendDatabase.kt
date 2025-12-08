@file:OptIn(ExperimentalUuidApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.async.coroutines.awaitCreate
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistEntryAnimeExpo2026
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
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ListImages
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object AlleyBackendDatabase {

    suspend fun handleRequest(context: EventContext, path: String): Response {
        val pathSegments = path.removePrefix("/").split("/")
        val firstSegment = pathSegments.getOrNull(0)
        return when (firstSegment) {
            "image" -> loadImage(context, pathSegments.drop(1).joinToString(separator = "/"))
            "uploadImage" ->
                uploadImage(context, pathSegments.drop(1).joinToString(separator = "/"))
            else -> Json.decodeFromString<BackendRequest>(
                context.request.text().await(),
            ).run {
                when (this) {
                    is ArtistSave.Request -> makeResponse(saveArtist(context, this))
                    is BackendRequest.Artist -> makeResponse(loadArtist(context, this))
                    is BackendRequest.Artists -> makeResponse(loadArtists(context))
                    is BackendRequest.Merch -> makeResponse(loadMerch(context))
                    is BackendRequest.Series -> loadSeries(context)
                    is ListImages.Request -> makeResponse(listImages(context, this))
                    is MerchSave.Request -> makeResponse(saveMerch(context, this))
                    is SeriesSave.Request -> makeResponse(saveSeries(context, this))
                }
            }
        }
    }

    private inline fun <reified Request : BackendRequest.WithResponse<Response>, reified Response> Request.makeResponse(
        response: Response?,
    ): org.w3c.fetch.Response = if (response == null) {
        jsonResponse(null)
    } else {
        jsonResponse<Response>(response)
    }

    private suspend fun loadArtists(context: EventContext): List<ArtistSummary> =
        database(context).artistEntryAnimeExpo2026Queries
            .getArtists()
            .awaitAsList()
            .map {
                ArtistSummary(
                    id = Uuid.parse(it.id),
                    booth = it.booth,
                    name = it.name,
                    links = it.links,
                    storeLinks = it.storeLinks,
                    catalogLinks = it.catalogLinks,
                    seriesInferred = it.seriesInferred,
                    seriesConfirmed = it.seriesConfirmed,
                    merchInferred = it.merchInferred,
                    merchConfirmed = it.merchConfirmed,
                    images = it.images,
                )
            }

    private suspend fun loadArtist(
        context: EventContext,
        request: BackendRequest.Artist,
    ): ArtistDatabaseEntry.Impl? =
        database(context)
            .artistEntryAnimeExpo2026Queries
            .getArtist(request.artistId.toString())
            .awaitAsOneOrNull()
            ?.toArtistDatabaseEntry()

    private suspend fun saveArtist(
        context: EventContext,
        request: ArtistSave.Request,
    ): ArtistSave.Response {
        val database = database(context, tryCreate = true)
        val currentArtist =
            database.artistEntryAnimeExpo2026Queries.getArtist(request.updated.id)
                .awaitAsOneOrNull()?.toArtistDatabaseEntry()
        if (currentArtist != null && currentArtist != request.initial) {
            return ArtistSave.Response(ArtistSave.Response.Result.Outdated(currentArtist))
        }

        val updatedArtist = request.updated.copy(
            lastEditor = context.data?.cloudflareAccess?.JWT?.payload?.email,
        ).toArtistEntryAnimeExpo2026()
        database.artistEntryAnimeExpo2026Queries.insertArtist(updatedArtist)
        return ArtistSave.Response(ArtistSave.Response.Result.Success)
    }

    /**
     * Exposes image for local development so that it doesn't access the remote R2 bucket via the
     * public domain.
     */
    private suspend fun loadImage(context: EventContext, key: String): Response {
        val file = context.env.ARTIST_ALLEY_IMAGES_BUCKET.get(key).await()
            ?: return Response(null, ResponseInit(status = 404))
        return Response(file.body, ResponseInit(headers = Headers().apply {
            val contentType = file.httpMetadata.contentType
            if (contentType != null) {
                set("Content-Type", contentType)
            }
        }))
    }

    private suspend fun listImages(
        context: EventContext,
        request: ListImages.Request,
    ): ListImages.Response {
        val keys = context.env.ARTIST_ALLEY_IMAGES_BUCKET
            .list(R2ListOptions(request.prefix))
            .await()
            .objects
            .map { it.key }
        return ListImages.Response(keys.map {
            Uuid.parse(it.substringAfterLast("/").substringBefore(".")) to it
        })
    }

    private suspend fun uploadImage(context: EventContext, path: String): Response {
        context.env.ARTIST_ALLEY_IMAGES_BUCKET
            .put(path, context.request.unsafeCast<ResponseWithBody>().body)
            .await()
        return Response("")
    }

    private suspend fun loadSeries(context: EventContext): Response {
        val cachedSeriesJson = context.env.ARTIST_ALLEY_CACHE_KV.get("series").await()
        if (cachedSeriesJson != null) {
            return literalJsonResponse(cachedSeriesJson)
        }

        val seriesJson = loadSeriesIntoCache(context)
        return literalJsonResponse(seriesJson)
    }

    private suspend fun loadSeriesIntoCache(context: EventContext) =
        database(context)
            .seriesEntryQueries
            .getSeries()
            .awaitAsList()
            .map { it.toSeriesInfo() }
            .let(Json::encodeToString)
            .also { context.env.ARTIST_ALLEY_CACHE_KV.put("series", it).await() }

    private suspend fun saveSeries(
        context: EventContext,
        request: SeriesSave.Request,
    ): SeriesSave.Response {
        val database = database(context, tryCreate = true)
        val currentSeries =
            database.seriesEntryQueries.getSeriesById(request.updated.id)
                .awaitAsOneOrNull()?.toSeriesInfo()
        if (currentSeries != null && currentSeries != request.initial) {
            return SeriesSave.Response(SeriesSave.Response.Result.Outdated(currentSeries))
        }
        database.seriesEntryQueries.insertSeries(request.updated.toSeriesEntry())
        loadSeriesIntoCache(context)
        return SeriesSave.Response(SeriesSave.Response.Result.Success)
    }

    private suspend fun loadMerch(context: EventContext): List<MerchInfo> =
        database(context).merchEntryQueries
            .getMerch()
            .awaitAsList()
            .map { it.toMerchInfo() }

    private suspend fun saveMerch(context: EventContext, request: MerchSave.Request): MerchSave.Response {
        val database = database(context, tryCreate = true)
        val currentMerch =
            database.merchEntryQueries.getMerchById(request.updated.name)
                .awaitAsOneOrNull()?.toMerchInfo()
        if (currentMerch != null && currentMerch != request.initial) {
            return MerchSave.Response(MerchSave.Response.Result.Outdated(currentMerch))
        }
        database.merchEntryQueries.insertMerch(request.updated.toMerchEntry())
        return MerchSave.Response(MerchSave.Response.Result.Success)
    }

    private suspend fun database(
        context: EventContext,
        tryCreate: Boolean = false,
    ): AlleySqlDatabase {
        val sqlDriver = WorkerSqlDriver(database = context.env.ARTIST_ALLEY_DB)
        val database = AlleySqlDatabase(
            driver = sqlDriver,
            artistEntryAnimeExpo2026Adapter = ColumnAdapters.artistEntryAnimeExpo2026Adapter,
            seriesEntryAdapter = ColumnAdapters.seriesEntryAdapter,
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

    private fun literalJsonResponse(value: String) = Response(
        body = value,
        init = ResponseInit(status = 200, headers = Headers().apply {
            set("Content-Type", "application/json")
        })
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
        synonyms = synonyms,
        link = link,
        inferred2024 = 0, // TODO: Recalculate counts when adding to local database
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
