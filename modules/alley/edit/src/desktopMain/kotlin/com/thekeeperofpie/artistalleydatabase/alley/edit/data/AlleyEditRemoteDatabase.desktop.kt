package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptographyKeys
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.toArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.toStampRallySummary
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase(
    private val artistEntryDao: ArtistEntryDao,
    private val stampRallyEntryDao: StampRallyEntryDao,
) {
    private val artistsByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, ArtistDatabaseEntry.Impl>>()
    private val artistHistoryByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, MutableList<ArtistHistoryEntry>>>()
    private val images = mutableMapOf<String, EditImage>()

    private val series = mutableMapOf<Uuid, SeriesInfo>()
    private val merch = mutableMapOf<Uuid, MerchInfo>()

    private val stampRalliesByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, StampRallyDatabaseEntry>>()
    private val stampRallyHistoryByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, MutableList<StampRallyHistoryEntry>>>()

    internal val artistKeys = mutableMapOf<Uuid, AlleyCryptographyKeys>()
    internal val artistFormQueue = mutableMapOf<Uuid, ArtistFormSubmission>()
    internal val artistFormHistory = mutableListOf<ArtistFormSubmission>()

    internal val stampRallyFormQueue = mutableMapOf<Pair<Uuid, String>, StampRallyFormSubmission>()
    internal val stampRallyFormHistory = mutableListOf<StampRallyFormSubmission>()

    private var fakeArtistPrivateKey: String? = null

    private val simulatedLatency = 1.seconds

    actual suspend fun databaseCreate(): Unit = Unit

    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl? {
        simulateLatency()
        return artistsByDataYearAndId[dataYear]?.get(artistId.toString())
            ?: artistEntryDao.getEntry(dataYear, artistId.toString())
                ?.artist
                ?.databaseEntry
    }

    actual suspend fun loadArtistWithFormMetadata(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormMetadata.Response? {
        simulateLatency()
        return artistsByDataYearAndId[dataYear]?.get(artistId.toString())?.let {
            BackendRequest.ArtistWithFormMetadata.Response(
                artist = it,
                hasPendingFormSubmission = artistFormQueue[artistId] != null,
                hasFormLink = artistKeys[artistId] != null,
            )
        }
    }

    actual suspend fun loadArtistHistory(
        dataYear: DataYear,
        artistId: Uuid,
    ): List<ArtistHistoryEntry> =
        artistHistoryByDataYearAndId[dataYear]?.get(artistId.toString()).orEmpty()
            .sortedByDescending { it.timestamp }

    actual suspend fun loadArtists(dataYear: DataYear) =
        artistsByDataYearAndId[dataYear]?.values.orEmpty().toList().map { it.toArtistSummary() }

    actual suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ): BackendRequest.ArtistSave.Response = saveArtist(dataYear, initial, updated, null)

    private suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
        formTimestamp: Instant?,
    ): BackendRequest.ArtistSave.Response {
        simulateLatency()
        val oldArtist = loadArtist(dataYear, Uuid.parse(updated.id))
        if (oldArtist != null && oldArtist != initial) {
            return BackendRequest.ArtistSave.Response.Outdated(oldArtist)
        }
        val historyEntry = ArtistHistoryEntry.create(oldArtist, updated, formTimestamp)
            .copy(lastEditor = "local")
        artistHistoryByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }
            .getOrPut(updated.id) { mutableListOf() }
            .add(historyEntry)
        artistsByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }[updated.id] = updated
        return BackendRequest.ArtistSave.Response.Success
    }

    actual suspend fun deleteArtist(
        dataYear: DataYear,
        expected: ArtistDatabaseEntry.Impl,
    ): BackendRequest.ArtistDelete.Response {
        val artistId = Uuid.parse(expected.id)
        val currentArtist = loadArtist(dataYear, artistId)
        if (expected != currentArtist) {
            return BackendRequest.ArtistDelete.Response.Outdated(currentArtist)
        }

        artistsByDataYearAndId[dataYear]?.remove(expected.id)
        return BackendRequest.ArtistDelete.Response.Success
    }

    actual suspend fun listImages(
        dataYear: DataYear,
        artistId: Uuid,
    ): List<EditImage> {
        val prefix = EditImage.NetworkImage.makePrefix(dataYear, artistId.toString())
        return images.entries.filter { it.key.startsWith(prefix) }.map { it.value }
    }

    actual suspend fun listImages(
        dataYear: DataYear,
        stampRallyId: String,
    ): List<EditImage> {
        val prefix = EditImage.NetworkImage.makePrefix(dataYear, stampRallyId)
        return images.entries.filter { it.key.startsWith(prefix) }.map { it.value }
    }

    actual suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage {
        simulateLatency()
        val prefix = EditImage.NetworkImage.makePrefix(dataYear, artistId.toString())
        val key = "$prefix/$id.${platformFile.extension}"
        val imageKey = PlatformImageKey(id)
            .takeIf { PlatformImageCache[it] != null }
            ?: PlatformImageCache.add(platformFile)
        val image = EditImage.LocalImage(imageKey, name = key)
        images[key] = image
        return image
    }

    actual suspend fun uploadImage(
        dataYear: DataYear,
        stampRallyId: String,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage {
        simulateLatency()
        val prefix = EditImage.NetworkImage.makePrefix(dataYear, stampRallyId)
        val key = "$prefix/$id.${platformFile.extension}"
        val imageKey = PlatformImageKey(id)
            .takeIf { PlatformImageCache[it] != null }
            ?: PlatformImageCache.add(platformFile)
        val image = EditImage.LocalImage(imageKey, name = key)
        images[key] = image
        return image
    }

    actual suspend fun loadSeries(): List<SeriesInfo> = series.values.toList()

    actual suspend fun saveSeries(
        initial: SeriesInfo?,
        updated: SeriesInfo,
    ): BackendRequest.SeriesSave.Response {
        simulateLatency()
        val existing = initial?.uuid?.let { series[it] }
        if (existing != null && existing != initial) {
            return BackendRequest.SeriesSave.Response.Outdated(existing)
        }
        series[updated.uuid] = updated
        return BackendRequest.SeriesSave.Response.Success
    }

    actual suspend fun deleteSeries(expected: SeriesInfo): BackendRequest.SeriesDelete.Response {
        val currentSeries = series[expected.uuid]
        if (expected != currentSeries) {
            return BackendRequest.SeriesDelete.Response.Outdated(currentSeries)
        }

        series.remove(expected.uuid)
        return BackendRequest.SeriesDelete.Response.Success
    }

    actual suspend fun loadMerch(): List<MerchInfo> = merch.values.toList()

    actual suspend fun saveMerch(
        initial: MerchInfo?,
        updated: MerchInfo,
    ): BackendRequest.MerchSave.Response {
        simulateLatency()
        val existing = initial?.uuid?.let { merch[it] }
        if (existing != null && existing != initial) {
            return BackendRequest.MerchSave.Response.Outdated(existing)
        }
        merch[updated.uuid] = updated
        return BackendRequest.MerchSave.Response.Success
    }

    actual suspend fun deleteMerch(expected: MerchInfo): BackendRequest.MerchDelete.Response {
        val currentMerch = merch[expected.uuid]
        if (expected != currentMerch) {
            return BackendRequest.MerchDelete.Response.Outdated(currentMerch)
        }

        merch.remove(expected.uuid)
        return BackendRequest.MerchDelete.Response.Success
    }

    actual suspend fun generateFormLink(
        dataYear: DataYear,
        artistId: Uuid,
        forceRegenerate: Boolean,
    ): String? {
        simulateLatency()
        if (artistKeys[artistId] != null && !forceRegenerate) return null
        val keys = AlleyCryptography.generate()
        artistKeys[artistId] = keys

        if (artistId == AlleyCryptography.FAKE_ARTIST_ID) {
            fakeArtistPrivateKey = keys.privateKey
        }

        return "localhost://form?${AlleyCryptography.ACCESS_KEY_PARAM}=${keys.privateKey}"
    }

    actual suspend fun loadArtistFormQueue(): List<ArtistFormQueueEntry> =
        artistFormQueue.values.map {
            ArtistFormQueueEntry(
                artistId = Uuid.parse(it.after.id),
                booth = it.after.booth?.ifBlank { null } ?: it.before.booth,
                name = it.after.name.ifBlank { null } ?: it.before.name,
            )
        }

    actual suspend fun loadArtistFormHistory(): List<ArtistFormHistoryEntry> =
        artistFormHistory.map {
            ArtistFormHistoryEntry(
                artistId = Uuid.parse(it.after.id),
                booth = it.after.booth?.ifBlank { null } ?: it.before.booth,
                name = it.after.name.ifBlank { null } ?: it.before.name,
                timestamp = it.timestamp,
            )
        }

    actual suspend fun loadArtistWithFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormEntry.Response? {
        val formSubmission = artistFormQueue[artistId] ?: return null
        return BackendRequest.ArtistWithFormEntry.Response(
            artist = loadArtist(dataYear, artistId) ?: return null,
            formDiff = formSubmission.toArtistEntryDiff(),
        )
    }

    actual suspend fun loadArtistWithHistoricalFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
        formTimestamp: Instant,
    ): BackendRequest.ArtistWithHistoricalFormEntry.Response? {
        val formSubmission = findFormHistoryEntry(dataYear, artistId, formTimestamp) ?: return null
        return BackendRequest.ArtistWithHistoricalFormEntry.Response(
            artist = loadArtist(dataYear, artistId) ?: return null,
            formDiff = formSubmission.toArtistEntryDiff(),
        )
    }

    actual suspend fun saveArtistAndClearFormEntry(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl,
        updated: ArtistDatabaseEntry.Impl,
        formEntryTimestamp: Instant,
    ): BackendRequest.ArtistCommitForm.Response {
        val artistId = Uuid.parse(updated.id)
        if (artistFormQueue[artistId]?.timestamp == formEntryTimestamp ||
            findFormHistoryEntry(dataYear, artistId, formEntryTimestamp) != null
        ) {
            saveArtist(
                dataYear = dataYear,
                initial = initial,
                updated = updated.copy(verifiedArtist = true),
                formTimestamp = formEntryTimestamp,
            )
            artistFormQueue.remove(artistId)?.let {
                artistFormHistory += it
            }
        }
        return BackendRequest.ArtistCommitForm.Response.Success
    }

    actual suspend fun fakeArtistFormLink() =
        fakeArtistPrivateKey?.let { "localhost://form/artist?${AlleyCryptography.ACCESS_KEY_PARAM}=$it" }

    actual suspend fun deleteFakeArtistData() {
        fakeArtistPrivateKey = null
        artistKeys.remove(AlleyCryptography.FAKE_ARTIST_ID)
        artistFormHistory.removeIf { it.before.id == AlleyCryptography.FAKE_ARTIST_ID.toString() }
        artistFormQueue.remove(AlleyCryptography.FAKE_ARTIST_ID)
    }

    actual suspend fun loadStampRallies(dataYear: DataYear) =
        stampRalliesByDataYearAndId[dataYear]?.values.orEmpty().toList()
            .map { it.toStampRallySummary() }

    actual suspend fun loadStampRally(
        dataYear: DataYear,
        stampRallyId: String,
    ): StampRallyDatabaseEntry? {
        simulateLatency()
        return stampRalliesByDataYearAndId[dataYear]?.get(stampRallyId)
            ?: stampRallyEntryDao.getEntry(dataYear, stampRallyId)
                ?.stampRally
    }

    actual suspend fun loadStampRallyHistory(
        dataYear: DataYear,
        stampRallyId: String,
    ): List<StampRallyHistoryEntry> =
        stampRallyHistoryByDataYearAndId[dataYear]?.get(stampRallyId).orEmpty()
            .sortedByDescending { it.timestamp }

    actual suspend fun saveStampRally(
        dataYear: DataYear,
        initial: StampRallyDatabaseEntry?,
        updated: StampRallyDatabaseEntry,
    ): BackendRequest.StampRallySave.Response = saveStampRally(dataYear, initial, updated, null)

    actual suspend fun deleteStampRally(
        dataYear: DataYear,
        expected: StampRallyDatabaseEntry
    ): BackendRequest.StampRallyDelete.Response {
        val stampRallyId = expected.id
        val currentStampRally = loadStampRally(dataYear, stampRallyId)
        if (expected != currentStampRally) {
            return BackendRequest.StampRallyDelete.Response.Outdated(currentStampRally)
        }

        stampRalliesByDataYearAndId[dataYear]?.remove(expected.id)
        return BackendRequest.StampRallyDelete.Response.Success
    }

    actual suspend fun loadStampRallyFormQueue(): List<StampRallyFormQueueEntry> =
        stampRallyFormQueue.values.map {
            StampRallyFormQueueEntry(
                stampRallyId = it.after.id,
                hostTable = it.after.hostTable.ifBlank { null } ?: it.before?.hostTable,
                fandom = it.after.fandom.ifBlank { null } ?: it.before?.fandom,
            )
        }

    actual suspend fun loadStampRallyFormHistory(): List<StampRallyFormHistoryEntry> =
        stampRallyFormHistory.map {
            StampRallyFormHistoryEntry(
                stampRallyId = it.after.id,
                hostTable = it.after.hostTable.ifBlank { null } ?: it.before?.hostTable,
                fandom = it.after.fandom.ifBlank { null } ?: it.before?.fandom,
                timestamp = it.timestamp,
            )
        }

    private suspend fun saveStampRally(
        dataYear: DataYear,
        initial: StampRallyDatabaseEntry?,
        updated: StampRallyDatabaseEntry,
        formTimestamp: Instant?,
    ): BackendRequest.StampRallySave.Response {
        simulateLatency()
        val oldStampRally = loadStampRally(dataYear, updated.id)
        if (oldStampRally != null && oldStampRally != initial) {
            return BackendRequest.StampRallySave.Response.Outdated(oldStampRally)
        }
        val historyEntry = StampRallyHistoryEntry.create(oldStampRally, updated, formTimestamp)
            .copy(lastEditor = "local")
        stampRallyHistoryByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }
            .getOrPut(updated.id) { mutableListOf() }
            .add(historyEntry)
        stampRalliesByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }[updated.id] = updated
        return BackendRequest.StampRallySave.Response.Success
    }

    private suspend fun simulateLatency() = delay(simulatedLatency)

    private fun findFormHistoryEntry(dataYear: DataYear, artistId: Uuid, formTimestamp: Instant) =
        artistFormHistory.find {
            it.after.year == dataYear &&
                    it.after.id == artistId.toString() &&
                    it.timestamp == formTimestamp
        }

    private fun ArtistFormSubmission.toArtistEntryDiff(): ArtistEntryDiff {
        return ArtistEntryDiff(
            booth = after.booth.orEmpty().takeIf { it != before.booth.orEmpty() },
            name = after.name.takeIf { it != before.name },
            summary = after.summary.orEmpty().takeIf { it != before.summary.orEmpty() },
            notes = after.notes.orEmpty().takeIf { it != before.notes.orEmpty() },
            socialLinks = HistoryListDiff.diffList(before.socialLinks, after.socialLinks),
            storeLinks = HistoryListDiff.diffList(before.storeLinks, after.storeLinks),
            portfolioLinks = HistoryListDiff.diffList(
                before.portfolioLinks,
                after.portfolioLinks
            ),
            catalogLinks = HistoryListDiff.diffList(before.catalogLinks, after.catalogLinks),
            commissions = HistoryListDiff.diffList(before.commissions, after.commissions),
            seriesInferred = HistoryListDiff.diffList(
                before.seriesInferred,
                after.seriesInferred
            ),
            seriesConfirmed =
                HistoryListDiff.diffList(before.seriesConfirmed, after.seriesConfirmed),
            merchInferred = HistoryListDiff.diffList(before.merchInferred, after.merchInferred),
            merchConfirmed = HistoryListDiff.diffList(
                before.merchConfirmed,
                after.merchConfirmed
            ),
            formNotes = formNotes,
            timestamp = timestamp,
        )
    }

    internal data class ArtistFormSubmission(
        val before: ArtistDatabaseEntry.Impl,
        val after: ArtistDatabaseEntry.Impl,
        val formNotes: String,
        val timestamp: Instant = Clock.System.now(),
    )

    internal data class StampRallyFormSubmission(
        val before: StampRallyDatabaseEntry?,
        val after: StampRallyDatabaseEntry,
        val timestamp: Instant = Clock.System.now(),
    )
}
