package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptographyKeys
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
import com.thekeeperofpie.artistalleydatabase.alley.models.toArtistSummary
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
actual class AlleyEditRemoteDatabase {

    private val artistsByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, ArtistDatabaseEntry.Impl>>()
    private val artistHistoryByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, MutableList<ArtistHistoryEntry>>>()
    private val images = mutableMapOf<String, EditImage>()

    private val series = mutableMapOf<Uuid, SeriesInfo>()
    private val merch = mutableMapOf<Uuid, MerchInfo>()

    internal val artistKeys = mutableMapOf<Uuid, AlleyCryptographyKeys>()
    internal val artistFormQueue =
        mutableMapOf<Uuid, Triple<Instant, ArtistDatabaseEntry.Impl, ArtistDatabaseEntry.Impl>>()

    private var simulatedLatency: Duration? = null

    init {
        runBlocking {
            // Seed some initial data to make it easier to test out features locally
            val artistUpdates = listOf<(ArtistDatabaseEntry.Impl) -> ArtistDatabaseEntry.Impl>(
                { it.copy(name = "First Last", lastEditor = "firstlast@example.org") },
                { it.copy(summary = "Description", lastEditor = "fakeemail@example.com") },
                {
                    it.copy(
                        links = listOf(
                            "https://example.com/social",
                            "https://example.com/profile",
                        ),
                        notes = "Test notes",
                        editorNotes = "Added links",
                    )
                },
                {
                    it.copy(
                        storeLinks = listOf("https://example.org/store"),
                        catalogLinks = listOf("https://example.net/portfolio"),
                        notes = "",
                    )
                },
                {
                    it.copy(
                        commissions = listOf("On-site", "Online"),
                        notes = "More test notes",
                        editorNotes = "Added commissions",
                    )
                },
                {
                    it.copy(
                        seriesInferred = listOf("SeriesA", "SeriesB"),
                        merchInferred = listOf("MerchA"),
                        editorNotes = "",
                    )
                },
                {
                    it.copy(
                        seriesInferred = it.seriesInferred + listOf("SeriesC"),
                        merchInferred = it.merchInferred + listOf("MerchB"),
                        lastEditor = "firstlast@example.com",
                    )
                },
                {
                    it.copy(
                        seriesInferred = it.seriesInferred - listOf("SeriesA"),
                        merchInferred = it.merchInferred - listOf("MerchA"),
                        seriesConfirmed = listOf("SeriesA", "SeriesC"),
                        merchConfirmed = listOf("MerchA", "MerchC"),
                    )
                },
            )
            var previous =
                ArtistDatabaseEntry.Impl(
                    year = DataYear.ANIME_EXPO_2026,
                    id = Uuid.random().toString(),
                    status = ArtistStatus.UNKNOWN,
                    booth = "C38",
                    name = "",
                    summary = null,
                    links = emptyList(),
                    storeLinks = emptyList(),
                    catalogLinks = emptyList(),
                    driveLink = null,
                    notes = null,
                    commissions = emptyList(),
                    seriesInferred = emptyList(),
                    seriesConfirmed = emptyList(),
                    merchInferred = emptyList(),
                    merchConfirmed = emptyList(),
                    images = emptyList(),
                    counter = 0,
                    editorNotes = null,
                    lastEditor = "fakeemail@example.com",
                    lastEditTime = Clock.System.now() - 1.hours,
                )
            saveArtist(dataYear = DataYear.ANIME_EXPO_2026, initial = null, updated = previous)
            artistUpdates.forEach {
                val next = it(previous).copy(lastEditTime = previous.lastEditTime!! + 1.minutes)
                saveArtist(dataYear = DataYear.ANIME_EXPO_2026, initial = previous, updated = next)
                previous = next
            }

            val after = previous.copy(
                name = previous.name + " - edited",
                summary = "New description",
                seriesInferred = previous.seriesInferred.drop(1) + "SeriesD",
                merchConfirmed = previous.merchConfirmed.drop(1) + "MerchD",
            )

            artistFormQueue[Uuid.parse(previous.id)] = Triple(Clock.System.now(), previous, after)

            simulatedLatency = 5.seconds
        }
    }

    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistDatabaseEntry.Impl? =
        artistsByDataYearAndId[dataYear]?.get(artistId.toString())

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
    ): ArtistSave.Response {
        simulateLatency()
        val oldArtist = loadArtist(dataYear, Uuid.parse(updated.id))
        val historyEntry = ArtistHistoryEntry.create(oldArtist, updated)
        artistHistoryByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }
            .getOrPut(updated.id) { mutableListOf() }
            .add(historyEntry)
        artistsByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }[updated.id] = updated
        return ArtistSave.Response.Success
    }

    actual suspend fun listImages(
        dataYear: DataYear,
        artistId: Uuid,
    ): List<EditImage> {
        val prefix = EditImage.NetworkImage.makePrefix(dataYear, artistId)
        return images.entries.filter { it.key.startsWith(prefix) }.map { it.value }
    }

    actual suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage {
        simulateLatency()
        val prefix = EditImage.NetworkImage.makePrefix(dataYear, artistId)
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
    ): SeriesSave.Response.Result {
        simulateLatency()
        val existing = initial?.uuid?.let { series[it] }
        if (existing != null && existing != initial) {
            return SeriesSave.Response.Result.Outdated(existing)
        }
        series[updated.uuid] = updated
        return SeriesSave.Response.Result.Success
    }

    actual suspend fun loadMerch(): List<MerchInfo> = merch.values.toList()

    actual suspend fun saveMerch(
        initial: MerchInfo?,
        updated: MerchInfo,
    ): MerchSave.Response.Result {
        simulateLatency()
        val existing = initial?.uuid?.let { merch[it] }
        if (existing != null && existing != initial) {
            return MerchSave.Response.Result.Outdated(existing)
        }
        merch[updated.uuid] = updated
        return MerchSave.Response.Result.Success
    }

    actual suspend fun generateFormLink(dataYear: DataYear, artistId: Uuid): String? {
        val keys = AlleyCryptography.generate()
        artistKeys[artistId] = keys
        return "localhost://form/artist/${dataYear.serializedName}/$artistId" +
                "?${AlleyCryptography.ACCESS_KEY_PARAM}=${keys.privateKey}"
    }

    actual suspend fun loadArtistFormQueue(): List<ArtistFormQueueEntry> =
        artistFormQueue.values.map { (id, before, after) ->
            ArtistFormQueueEntry(
                artistId = Uuid.parse(before.id),
                beforeBooth = before.booth,
                beforeName = before.name,
                afterBooth = after.booth,
                afterName = after.name,
            )
        }

    actual suspend fun loadArtistWithFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormEntry.Response? {
        val (timestamp, before, after) = artistFormQueue[artistId] ?: return null
        return BackendRequest.ArtistWithFormEntry.Response(
            artist = loadArtist(dataYear, artistId) ?: return null,
            formDiff = ArtistEntryDiff(
                booth = after.booth.orEmpty().takeIf { it != before.booth.orEmpty() },
                name = after.name.orEmpty().takeIf { it != before.name.orEmpty() },
                summary = after.summary.orEmpty().takeIf { it != before.summary.orEmpty() },
                notes = after.notes.orEmpty().takeIf { it != before.notes.orEmpty() },
                links = ArtistEntryDiff.diffList(before.links, after.links),
                storeLinks = ArtistEntryDiff.diffList(before.storeLinks, after.storeLinks),
                catalogLinks = ArtistEntryDiff.diffList(before.catalogLinks, after.catalogLinks),
                commissions = ArtistEntryDiff.diffList(before.commissions, after.commissions),
                seriesInferred = ArtistEntryDiff.diffList(
                    before.seriesInferred,
                    after.seriesInferred
                ),
                seriesConfirmed =
                    ArtistEntryDiff.diffList(before.seriesConfirmed, after.seriesConfirmed),
                merchInferred = ArtistEntryDiff.diffList(before.merchInferred, after.merchInferred),
                merchConfirmed = ArtistEntryDiff.diffList(
                    before.merchConfirmed,
                    after.merchConfirmed
                ),
                timestamp = timestamp,
            )
        )
    }

    actual suspend fun saveArtistAndClearFormEntry(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl,
        updated: ArtistDatabaseEntry.Impl,
        formEntryTimestamp: Instant,
    ): BackendRequest.ArtistCommitForm.Response {
        saveArtist(dataYear = dataYear, initial = initial, updated = updated)
        val artistId = Uuid.parse(updated.id)
        if (artistFormQueue[artistId]?.first == formEntryTimestamp) {
            artistFormQueue.remove(artistId)
        }
        return BackendRequest.ArtistCommitForm.Response.Success
    }

    private suspend fun simulateLatency() = simulatedLatency?.let { delay(it) }
}
