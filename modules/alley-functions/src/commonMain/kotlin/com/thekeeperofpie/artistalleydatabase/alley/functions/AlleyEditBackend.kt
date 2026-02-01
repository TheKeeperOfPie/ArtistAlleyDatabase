@file:OptIn(ExperimentalUuidApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.toMerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.data.toStampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toStampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormPublicKey
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.R2ListOptions
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.ResponseWithBody
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ListImages
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object AlleyEditBackend {

    suspend fun handleRequest(context: EventContext, path: String): Response {
        val pathSegments = path.removePrefix("/").split("/")
        val firstSegment = pathSegments.getOrNull(0)

        // Assumes that middleware has authorized non-form requests already
        return when (firstSegment) {
            "image" -> loadImage(context, pathSegments.drop(1).joinToString(separator = "/"))
            "uploadImage" ->
                uploadImage(context, pathSegments.drop(1).joinToString(separator = "/"))
            else -> Json.decodeFromString<BackendRequest>(context.request.text().await()).run {
                when (this) {
                    is BackendRequest.Artist -> makeResponse(loadArtist(context, this))
                    is BackendRequest.ArtistWithFormMetadata ->
                        makeResponse(loadArtistWithFormMetadata(context, this))
                    is BackendRequest.ArtistCommitForm ->
                        makeResponse(commitArtistForm(context, this))
                    is BackendRequest.ArtistDelete -> makeResponse(deleteArtist(context, this))
                    is BackendRequest.ArtistFormHistory ->
                        makeResponse(loadArtistFormHistory(context))
                    is BackendRequest.ArtistFormQueue -> makeResponse(loadArtistFormQueue(context))
                    is BackendRequest.ArtistWithFormEntry ->
                        makeResponse(loadArtistWithFormEntry(context, this))
                    is BackendRequest.ArtistWithHistoricalFormEntry ->
                        makeResponse(loadArtistWithHistoricalFormEntry(context, this))
                    is BackendRequest.ArtistHistory ->
                        makeResponse(loadArtistHistory(context, this))
                    is BackendRequest.Artists -> makeResponse(loadArtists(context))
                    is BackendRequest.ArtistSave -> makeResponse(saveArtist(context, this, null))
                    is BackendRequest.DatabaseCreate -> makeResponse(databaseCreate(context))
                    is BackendRequest.GenerateFormKey ->
                        makeResponse(generateFormKey(context, this))
                    is BackendRequest.FakeArtistData -> makeResponse(fakeArtistData(context, this))
                    is BackendRequest.DeleteFakeArtistData ->
                        makeResponse(deleteFakeArtistData(context))
                    is BackendRequest.Merch -> makeResponse(loadMerch(context))
                    is BackendRequest.MerchDelete -> makeResponse(deleteMerch(context, this))
                    is BackendRequest.MerchSave -> makeResponse(saveMerch(context, this))
                    is BackendRequest.Series -> loadSeries(context)
                    is BackendRequest.SeriesDelete -> makeResponse(deleteSeries(context, this))
                    is BackendRequest.SeriesSave -> makeResponse(saveSeries(context, this))
                    is BackendRequest.StampRallies -> makeResponse(loadStampRallies(context))
                    is BackendRequest.StampRally -> makeResponse(loadStampRally(context, this))
                    is BackendRequest.StampRallySave -> makeResponse(
                        saveStampRally(
                            context,
                            this,
                            null
                        )
                    )
                    is ListImages.Request -> makeResponse(listImages(context, this))
                }
            }
        }
    }

    private inline fun <reified Request : BackendRequest.WithResponse<Response>, reified Response> Request.makeResponse(
        response: Response?,
    ): org.w3c.fetch.Response = if (response == null) {
        Utils.jsonResponse(null)
    } else {
        Utils.jsonResponse<Response>(response)
    }

    private suspend fun databaseCreate(context: EventContext) = Databases.create(context)

    private suspend fun loadArtists(context: EventContext): List<ArtistSummary> =
        Databases.editDatabase(context).artistEntryAnimeExpo2026Queries
            .getArtists()
            .awaitAsList()
            .map {
                ArtistSummary(
                    id = Uuid.parse(it.id),
                    booth = it.booth,
                    name = it.name,
                    socialLinks = it.socialLinks,
                    storeLinks = it.storeLinks,
                    portfolioLinks = it.portfolioLinks,
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
        when (request.dataYear) {
            DataYear.ANIME_EXPO_2026 -> Databases.editDatabase(context)
                .artistEntryAnimeExpo2026Queries
                .getArtist(request.artistId.toString())
                .awaitAsOneOrNull()
                ?.toArtistDatabaseEntry()
                ?.fixForJs()
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> null // TODO: Return legacy years?
        }

    private suspend fun loadArtistWithFormMetadata(
        context: EventContext,
        request: BackendRequest.ArtistWithFormMetadata,
    ): BackendRequest.ArtistWithFormMetadata.Response? =
        when (request.dataYear) {
            DataYear.ANIME_EXPO_2026 -> Databases.editDatabase(context)
                .artistEntryAnimeExpo2026Queries
                .getArtist(request.artistId.toString())
                .awaitAsOneOrNull()
                ?.toArtistDatabaseEntry()
                ?.fixForJs()
                ?.let {
                    val formDatabase = Databases.formDatabase(context)
                    val formEntry = formDatabase.artistFormEntryQueries
                        .getFormEntry(request.dataYear, request.artistId)
                        .awaitAsOneOrNull()
                    BackendRequest.ArtistWithFormMetadata.Response(
                        artist = it,
                        hasPendingFormSubmission = formEntry != null,
                        hasFormLink = coerceBooleanForJs(
                            formDatabase.alleyFormPublicKeyQueries
                                .hasPublicKey(request.artistId)
                                .awaitAsOne()
                        ),
                    )
                }
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> null // TODO: Return legacy years?
        }

    private suspend fun loadArtistFormQueue(context: EventContext): List<ArtistFormQueueEntry> =
        Databases.formDatabase(context)
            .artistFormEntryQueries
            .getFormQueue()
            .awaitAsList()
            .map {
                ArtistFormQueueEntry(
                    artistId = it.artistId,
                    beforeBooth = it.beforeBooth,
                    beforeName = it.beforeName,
                    afterBooth = it.afterBooth,
                    afterName = it.afterName,
                )
            }

    private suspend fun loadArtistFormHistory(context: EventContext): List<ArtistFormHistoryEntry> =
        Databases.formDatabase(context)
            .artistFormEntryQueries
            .getFormHistory()
            .awaitAsList()
            .map {
                ArtistFormHistoryEntry(
                    artistId = it.artistId,
                    booth = it.afterBooth ?: it.beforeBooth,
                    name = it.afterName ?: it.beforeName,
                    timestamp = it.timestamp,
                )
            }

    private suspend fun loadArtistWithFormEntry(
        context: EventContext,
        request: BackendRequest.ArtistWithFormEntry,
    ): BackendRequest.ArtistWithFormEntry.Response? {
        val artist = BackendUtils.loadArtist(context, request.dataYear, request.artistId)
            ?: return null

        val formDiff = BackendUtils.loadFormDiff(context, request.dataYear, request.artistId)
            ?: return null

        return BackendRequest.ArtistWithFormEntry.Response(artist = artist, formDiff = formDiff)
    }

    private suspend fun loadArtistWithHistoricalFormEntry(
        context: EventContext,
        request: BackendRequest.ArtistWithHistoricalFormEntry,
    ): BackendRequest.ArtistWithHistoricalFormEntry.Response? {
        val artist = BackendUtils.loadArtist(context, request.dataYear, request.artistId)
            ?: return null

        val formDiff = BackendUtils.loadFormHistoryDiff(
            context,
            request.dataYear,
            request.artistId,
            request.formTimestamp
        )
            ?: return null

        return BackendRequest.ArtistWithHistoricalFormEntry.Response(
            artist = artist,
            formDiff = formDiff
        )
    }

    private suspend fun commitArtistForm(
        context: EventContext,
        request: BackendRequest.ArtistCommitForm,
    ): BackendRequest.ArtistCommitForm.Response {
        val saveRequest = BackendRequest.ArtistSave(
            dataYear = request.dataYear,
            initial = request.initial,
            updated = request.updated.copy(verifiedArtist = true),
        )
        when (val saveResponse = saveArtist(context, saveRequest, request.formEntryTimestamp)) {
            is BackendRequest.ArtistSave.Response.Failed ->
                return BackendRequest.ArtistCommitForm.Response.Failed(saveResponse.errorMessage)
            is BackendRequest.ArtistSave.Response.Outdated ->
                return BackendRequest.ArtistCommitForm.Response.Outdated(saveResponse.current)
            BackendRequest.ArtistSave.Response.Success -> Unit
        }

        Databases.formDatabase(context).artistFormEntryQueries.run {
            // D1 only supports transactions in a batched statement, which is hard to set up
            // A single write and delete should be consistent enough that it shouldn't matter
            val artistId = Uuid.parse(request.updated.id)
            moveFormEntryToHistory(
                dataYear = request.dataYear,
                artistId = artistId,
                timestamp = request.formEntryTimestamp,
            )
            consumeFormEntry(
                dataYear = request.dataYear,
                artistId = artistId,
                timestamp = request.formEntryTimestamp,
            )
        }
        return BackendRequest.ArtistCommitForm.Response.Success
    }

    private suspend fun deleteArtist(
        context: EventContext,
        request: BackendRequest.ArtistDelete,
    ): BackendRequest.ArtistDelete.Response =
        when (request.dataYear) {
            DataYear.ANIME_EXPO_2026 -> {
                val database = Databases.editDatabase(context)
                val currentArtist =
                    database.artistEntryAnimeExpo2026Queries
                        .getArtist(request.expected.id)
                        .awaitAsOneOrNull()
                        ?.toArtistDatabaseEntry()
                        ?.fixForJs()
                if (currentArtist == null || currentArtist != request.expected) {
                    BackendRequest.ArtistDelete.Response.Outdated(currentArtist)
                } else {
                    database.artistEntryAnimeExpo2026Queries
                        .deleteArtist(request.expected.id)
                    BackendRequest.ArtistDelete.Response.Success
                }
            }
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> BackendRequest.ArtistDelete.Response.Failed("Cannot delete legacy years")
        }

    private suspend fun loadArtistHistory(
        context: EventContext,
        request: BackendRequest.ArtistHistory,
    ): List<ArtistHistoryEntry> =
        when (request.dataYear) {
            DataYear.ANIME_EXPO_2026 -> Databases.editDatabase(context).artistEntryAnimeExpo2026Queries
                .getHistory(request.artistId.toString())
                .awaitAsList()
                .map { it.toHistoryEntry() }
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> emptyList() // TODO: Return legacy years?
        }

    private suspend fun saveArtist(
        context: EventContext,
        request: BackendRequest.ArtistSave,
        formTimestamp: Instant?,
    ) = when (request.dataYear) {
        DataYear.ANIME_EXPO_2026 -> {
            val database = Databases.editDatabase(context)
            val currentArtist =
                database.artistEntryAnimeExpo2026Queries
                    .getArtist(request.updated.id)
                    .awaitAsOneOrNull()
                    ?.toArtistDatabaseEntry()
                    ?.fixForJs()
            if (currentArtist != null && currentArtist != request.initial) {
                BackendRequest.ArtistSave.Response.Outdated(currentArtist)
            } else {
                val updatedArtist = request.updated.copy(
                    lastEditor = context.data?.cloudflareAccess?.JWT?.payload?.email,
                    lastEditTime = Clock.System.now(),
                )
                val historyEntry = ArtistHistoryEntry.create(
                    before = currentArtist,
                    after = updatedArtist,
                    formTimestamp = formTimestamp
                ).toDatabaseEntry(Uuid.parse(updatedArtist.id))
                database.artistEntryAnimeExpo2026Queries.insertHistory(historyEntry)
                database.artistEntryAnimeExpo2026Queries.insertArtist(updatedArtist.toArtistEntryAnimeExpo2026())
                BackendRequest.ArtistSave.Response.Success
            }
        }
        DataYear.ANIME_EXPO_2023,
        DataYear.ANIME_EXPO_2024,
        DataYear.ANIME_EXPO_2025,
        DataYear.ANIME_NYC_2024,
        DataYear.ANIME_NYC_2025,
            -> BackendRequest.ArtistSave.Response.Failed("Editing legacy years not supported")
    }

    private suspend fun generateFormKey(
        context: EventContext,
        request: BackendRequest.GenerateFormKey,
    ): String? {
        val database = Databases.formDatabase(context)
        val keys = AlleyCryptography.generate()
        if (!request.forceRegenerate && database.alleyFormPublicKeyQueries.hasPublicKey(request.artistId)
                .awaitAsOne()
        ) {
            return null
        }
        database.alleyFormPublicKeyQueries.insertPublicKey(
            ArtistFormPublicKey(artistId = request.artistId, publicKey = keys.publicKey)
        )
        if (request.artistId == AlleyCryptography.FAKE_ARTIST_ID) {
            KeyValueCacher(context).putFakeArtistData(keys.privateKey)
        }

        return AlleyCryptography.oneTimeEncrypt(
            publicKey = request.publicKeyForResponse,
            payload = keys.privateKey,
        )
    }

    private suspend fun fakeArtistData(
        context: EventContext,
        request: BackendRequest.FakeArtistData,
    ): String? {
        val fakeArtistData = KeyValueCacher(context).getFakeArtistData() ?: return null
        return AlleyCryptography.oneTimeEncrypt(
            publicKey = request.publicKeyForResponse,
            payload = fakeArtistData.privateKey,
        )
    }

    private suspend fun deleteFakeArtistData(context: EventContext) {
        Databases.formDatabase(context).fakeArtistDataQueries.run {
            deleteFakeArtistFormEntry()
            deleteFakeArtistFormEntryHistory()
            deleteFakeArtistPublicKey()
        }
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
        val cachedSeriesJson = KeyValueCacher(context).getSeriesJson()
        if (cachedSeriesJson != null) {
            return literalJsonResponse(cachedSeriesJson)
        }

        val seriesJson = loadSeriesIntoCache(context)
        return literalJsonResponse(seriesJson)
    }

    private suspend fun loadSeriesIntoCache(context: EventContext) =
        Databases.editDatabase(context)
            .seriesEntryQueries
            .getSeries()
            .awaitAsList()
            .map { it.toSeriesInfo() }
            .let { KeyValueCacher(context).putSeries(it) }

    private suspend fun saveSeries(
        context: EventContext,
        request: BackendRequest.SeriesSave,
    ): BackendRequest.SeriesSave.Response {
        val database = Databases.editDatabase(context)
        val currentSeries =
            database.seriesEntryQueries.getSeriesById(request.updated.id)
                .awaitAsOneOrNull()?.toSeriesInfo()
        if (currentSeries != null && currentSeries != request.initial) {
            return BackendRequest.SeriesSave.Response.Outdated(currentSeries)
        }
        database.seriesEntryQueries.insertSeries(request.updated.toSeriesEntry())
        loadSeriesIntoCache(context)
        return BackendRequest.SeriesSave.Response.Success
    }

    private suspend fun deleteSeries(
        context: EventContext,
        request: BackendRequest.SeriesDelete,
    ): BackendRequest.SeriesDelete.Response {
        val database = Databases.editDatabase(context)
        val currentSeries =
            database.seriesEntryQueries.getSeriesById(request.expected.id)
                .awaitAsOneOrNull()?.toSeriesInfo()
        if (currentSeries != null && currentSeries != request.expected) {
            return BackendRequest.SeriesDelete.Response.Outdated(currentSeries)
        }
        database.seriesEntryQueries.deleteSeries(request.expected.id)
        loadSeriesIntoCache(context)
        return BackendRequest.SeriesDelete.Response.Success
    }

    private suspend fun loadMerch(context: EventContext): List<MerchInfo> =
        Databases.editDatabase(context).merchEntryQueries
            .getMerch()
            .awaitAsList()
            .map { it.toMerchInfo() }

    private suspend fun saveMerch(
        context: EventContext,
        request: BackendRequest.MerchSave,
    ): BackendRequest.MerchSave.Response {
        val database = Databases.editDatabase(context)
        val currentMerch =
            database.merchEntryQueries.getMerchById(request.updated.name)
                .awaitAsOneOrNull()?.toMerchInfo()
        if (currentMerch != null && currentMerch != request.initial) {
            return BackendRequest.MerchSave.Response.Outdated(currentMerch)
        }
        database.merchEntryQueries.insertMerch(request.updated.toMerchEntry())
        return BackendRequest.MerchSave.Response.Success
    }

    private suspend fun deleteMerch(
        context: EventContext,
        request: BackendRequest.MerchDelete,
    ): BackendRequest.MerchDelete.Response {
        val database = Databases.editDatabase(context)
        val currentMerch =
            database.merchEntryQueries.getMerchById(request.expected.name)
                .awaitAsOneOrNull()?.toMerchInfo()
        if (currentMerch != null && currentMerch != request.expected) {
            return BackendRequest.MerchDelete.Response.Outdated(currentMerch)
        }
        database.merchEntryQueries.deleteMerch(request.expected.name)
        return BackendRequest.MerchDelete.Response.Success
    }

    private suspend fun loadStampRallies(context: EventContext): List<StampRallySummary> =
        Databases.editDatabase(context).stampRallyEntryAnimeExpo2026Queries
            .getStampRallies()
            .awaitAsList()
            .map {
                StampRallySummary(
                    id = it.id,
                    fandom = it.fandom,
                    hostTable = it.hostTable,
                )
            }

    private suspend fun loadStampRally(
        context: EventContext,
        request: BackendRequest.StampRally,
    ): StampRallyDatabaseEntry? =
        when (request.dataYear) {
            DataYear.ANIME_EXPO_2026 -> Databases.editDatabase(context)
                .stampRallyEntryAnimeExpo2026Queries
                .getStampRally(request.stampRallyId)
                .awaitAsOneOrNull()
                ?.toStampRallyDatabaseEntry()
                ?.fixForJs()
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025,
                -> null // TODO: Return legacy years?
        }

    private suspend fun saveStampRally(
        context: EventContext,
        request: BackendRequest.StampRallySave,
        formTimestamp: Instant?,
    ) = when (request.dataYear) {
        DataYear.ANIME_EXPO_2026 -> {
            val database = Databases.editDatabase(context)
            val currentStampRally =
                database.stampRallyEntryAnimeExpo2026Queries
                    .getStampRally(request.updated.id)
                    .awaitAsOneOrNull()
                    ?.toStampRallyDatabaseEntry()
                    ?.fixForJs()
            if (currentStampRally != null && currentStampRally != request.initial) {
                BackendRequest.StampRallySave.Response.Outdated(currentStampRally)
            } else {
                val updatedStampRally = request.updated.copy(
                    lastEditor = context.data?.cloudflareAccess?.JWT?.payload?.email,
                    lastEditTime = Clock.System.now(),
                )
                val historyEntry = StampRallyHistoryEntry.create(
                    before = currentStampRally,
                    after = updatedStampRally,
                    formTimestamp = formTimestamp
                ).toDatabaseEntry(updatedStampRally.id)
                database.stampRallyEntryAnimeExpo2026Queries.insertHistory(historyEntry)
                database.stampRallyEntryAnimeExpo2026Queries.insertStampRally(updatedStampRally.toStampRallyEntryAnimeExpo2026())
                BackendRequest.StampRallySave.Response.Success
            }
        }
        DataYear.ANIME_EXPO_2023,
        DataYear.ANIME_EXPO_2024,
        DataYear.ANIME_EXPO_2025,
        DataYear.ANIME_NYC_2024,
        DataYear.ANIME_NYC_2025,
            -> BackendRequest.StampRallySave.Response.Failed("Editing legacy years not supported")
    }

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

    private fun ArtistHistoryEntry.toDatabaseEntry(id: Uuid) =
        ArtistEntryAnimeExpo2026History(
            id = id.toString(),
            status = status,
            booth = booth,
            name = name,
            summary = summary,
            socialLinks = socialLinks,
            storeLinks = storeLinks,
            portfolioLinks = portfolioLinks,
            catalogLinks = catalogLinks,
            notes = notes,
            commissions = commissions,
            seriesInferred = seriesInferred,
            seriesConfirmed = seriesConfirmed,
            merchInferred = merchInferred,
            merchConfirmed = merchConfirmed,
            images = images,
            editorNotes = editorNotes,
            lastEditor = lastEditor,
            lastEditTime = timestamp,
            formTimestamp = formTimestamp,
        )

    private fun ArtistEntryAnimeExpo2026History.toHistoryEntry() =
        ArtistHistoryEntry(
            status = status,
            booth = booth,
            name = name,
            summary = summary,
            socialLinks = socialLinks,
            storeLinks = storeLinks,
            portfolioLinks = portfolioLinks,
            catalogLinks = catalogLinks,
            notes = notes,
            commissions = commissions,
            seriesInferred = seriesInferred,
            seriesConfirmed = seriesConfirmed,
            merchInferred = merchInferred,
            merchConfirmed = merchConfirmed,
            images = images,
            editorNotes = editorNotes,
            lastEditor = lastEditor,
            timestamp = lastEditTime,
            formTimestamp = formTimestamp,
        )

    private fun StampRallyHistoryEntry.toDatabaseEntry(id: String) =
        StampRallyEntryAnimeExpo2026History(
            id = id,
            fandom = fandom,
            hostTable = hostTable,
            tables = tables,
            links = links,
            tableMin = tableMin,
            totalCost = totalCost,
            prize = prize,
            prizeLimit = prizeLimit,
            series = series,
            merch = merch,
            notes = notes,
            images = images,
            confirmed = confirmed,
            editorNotes = editorNotes,
            lastEditor = lastEditor,
            lastEditTime = timestamp,
            formTimestamp = formTimestamp,
        )

    private fun StampRallyEntryAnimeExpo2026History.toHistoryEntry() =
        StampRallyHistoryEntry(
            fandom = fandom,
            hostTable = hostTable,
            tables = tables,
            links = links,
            tableMin = tableMin,
            totalCost = totalCost,
            prize = prize,
            prizeLimit = prizeLimit,
            series = series,
            merch = merch,
            notes = notes,
            images = images,
            confirmed = confirmed,
            editorNotes = editorNotes,
            lastEditor = lastEditor,
            timestamp = lastEditTime,
            formTimestamp = formTimestamp,
        )
}
