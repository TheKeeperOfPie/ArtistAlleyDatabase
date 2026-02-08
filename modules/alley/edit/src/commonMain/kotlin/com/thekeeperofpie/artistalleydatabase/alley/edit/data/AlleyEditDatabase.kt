package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.toMerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.util.toImageBitmap
import kotlin.time.Instant
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
class AlleyEditDatabase(
    private val artistEntryDao: ArtistEntryDao,
    private val merchEntryDao: MerchEntryDao,
    private val seriesEntryDao: SeriesEntryDao,
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val remoteDatabase: AlleyEditRemoteDatabase,
) {
    suspend fun databaseCreate() = remoteDatabase.databaseCreate()

    // TODO: Failure result
    suspend fun loadArtists(dataYear: DataYear): List<ArtistSummary> {
        val remoteArtists = remoteDatabase.loadArtists(dataYear)
        val remoteIds = remoteArtists.map { it.id }.toSet()
        val databaseArtists = artistEntryDao.getAllEntries(dataYear)
            .filter { it.id !in remoteIds }
        return databaseArtists + remoteArtists
    }

    suspend fun loadArtist(dataYear: DataYear, artistId: Uuid) =
        remoteDatabase.loadArtist(dataYear, artistId)
            ?: artistEntryDao.getEntry(dataYear, artistId.toString())
                ?.artist
                ?.databaseEntry

    suspend fun loadArtistWithFormMetadata(dataYear: DataYear, artistId: Uuid) =
        remoteDatabase.loadArtistWithFormMetadata(dataYear, artistId)
            ?: artistEntryDao.getEntry(dataYear, artistId.toString())
                ?.artist
                ?.databaseEntry
                ?.let {
                    BackendRequest.ArtistWithFormMetadata.Response(
                        artist = it,
                        hasPendingFormSubmission = false,
                        hasFormLink = false,
                    )
                }

    suspend fun loadArtistHistory(dataYear: DataYear, artistId: Uuid) =
        remoteDatabase.loadArtistHistory(dataYear, artistId)

    suspend fun loadSeries(): Map<String, SeriesInfo> =
        (seriesEntryDao.getSeries().map { it.toSeriesInfo() } + remoteDatabase.loadSeries())
            .associateBy { it.id }

    suspend fun loadMerch(): Map<String, MerchInfo> {
        return (merchEntryDao.getMerch().map { it.toMerchInfo() } + remoteDatabase.loadMerch())
            .associateBy { it.name }
    }

    fun getArtistEditImages(
        year: DataYear,
        images: List<CatalogImage>,
    ) = images.map {
        val path = "files/${year.folderName}/catalogs/${it.name}"
        EditImage.DatabaseImage(
            uri = Uri.parse(Res.getUri(path)),
            name = it.name,
            width = it.width,
            height = it.height,
        )
    }

    suspend fun loadArtistImages(year: DataYear, artist: ArtistDatabaseEntry): List<EditImage> {
        val databaseImages = artistEntryDao.getImagesById(year, artist.id)
            ?.let { getArtistEditImages(year, it) }
            ?.associateBy { it.name }
            .orEmpty()
        val networkImages = remoteDatabase.listImages(year, Uuid.parse(artist.id))
            .associateBy { it.name }
        return artist.images.mapNotNull {
            val targetName = it.name
            networkImages.entries.find { it.key.contains(targetName) }?.value
                ?.fillSize(it.width, it.height)
                ?: databaseImages.entries.find { it.key.contains(targetName) }?.value
        }
    }

    suspend fun saveArtist(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl?,
        updated: ArtistDatabaseEntry.Impl,
    ) = remoteDatabase.saveArtist(dataYear, initial, updated)

    suspend fun deleteArtist(
        dataYear: DataYear,
        expected: ArtistDatabaseEntry.Impl,
    ) = remoteDatabase.deleteArtist(dataYear, expected)

    suspend fun uploadImage(
        dataYear: DataYear,
        artistId: Uuid,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage? {
        val (width, height) = try {
            platformFile.toImageBitmap().run { width to height }
        } catch (_: Throwable) {
            // TODO: Error handling
            return null
        }
        return remoteDatabase.uploadImage(dataYear, artistId, platformFile, id)
            .fillSize(width, height)
    }

    suspend fun uploadImage(
        dataYear: DataYear,
        stampRallyId: String,
        platformFile: PlatformFile,
        id: Uuid,
    ): EditImage? {
        val (width, height) = try {
            platformFile.toImageBitmap().run { width to height }
        } catch (_: Throwable) {
            // TODO: Error handling
            return null
        }
        return remoteDatabase.uploadImage(dataYear, stampRallyId, platformFile, id)
            .fillSize(width, height)
    }

    suspend fun saveSeries(
        initial: SeriesInfo?,
        updated: SeriesInfo,
    ): BackendRequest.SeriesSave.Response =
        remoteDatabase.saveSeries(initial, updated)

    suspend fun deleteSeries(expected: SeriesInfo) = remoteDatabase.deleteSeries(expected)

    suspend fun saveMerch(
        initial: MerchInfo?,
        updated: MerchInfo,
    ): BackendRequest.MerchSave.Response =
        remoteDatabase.saveMerch(initial, updated)

    suspend fun deleteMerch(expected: MerchInfo) = remoteDatabase.deleteMerch(expected)

    suspend fun generateFormLink(
        dataYear: DataYear,
        artistId: Uuid,
        forceRegenerate: Boolean,
    ): String? = remoteDatabase.generateFormLink(dataYear, artistId, forceRegenerate)

    suspend fun loadArtistFormQueue(): List<ArtistFormQueueEntry> =
        remoteDatabase.loadArtistFormQueue()

    suspend fun loadArtistFormQueueHistory(): List<ArtistFormHistoryEntry> =
        remoteDatabase.loadArtistFormHistory()

    suspend fun loadArtistWithFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormEntry.Response? =
        remoteDatabase.loadArtistWithFormEntry(dataYear, artistId)

    suspend fun loadArtistWithHistoricalFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
        formTimestamp: Instant,
    ): BackendRequest.ArtistWithHistoricalFormEntry.Response? =
        remoteDatabase.loadArtistWithHistoricalFormEntry(dataYear, artistId, formTimestamp)

    suspend fun saveArtistAndClearFormEntry(
        dataYear: DataYear,
        initial: ArtistDatabaseEntry.Impl,
        updated: ArtistDatabaseEntry.Impl,
        formEntryTimestamp: Instant,
    ): BackendRequest.ArtistCommitForm.Response = remoteDatabase.saveArtistAndClearFormEntry(
        dataYear = dataYear,
        initial = initial,
        updated = updated,
        formEntryTimestamp = formEntryTimestamp,
    )

    suspend fun fakeArtistFormLink(): String? = remoteDatabase.fakeArtistFormLink()
    suspend fun deleteFakeArtistData() = remoteDatabase.deleteFakeArtistData()

    suspend fun loadStampRallies(dataYear: DataYear): List<StampRallySummary> {
        val remoteRallies = remoteDatabase.loadStampRallies(dataYear)
        val remoteIds = remoteRallies.map { it.id }.toSet()
        val databaseRallies = stampRallyEntryDao.getAllEntries(dataYear)
            .filter { it.id !in remoteIds }
        return databaseRallies + remoteRallies
    }

    suspend fun loadStampRally(dataYear: DataYear, stampRallyId: String) =
        remoteDatabase.loadStampRally(dataYear, stampRallyId)
            ?: stampRallyEntryDao.getEntry(dataYear, stampRallyId)
                ?.stampRally

    suspend fun saveStampRally(
        dataYear: DataYear,
        initial: StampRallyDatabaseEntry?,
        updated: StampRallyDatabaseEntry,
    ) = remoteDatabase.saveStampRally(dataYear, initial, updated)

    suspend fun loadStampRallyImages(
        year: DataYear,
        stampRally: StampRallyDatabaseEntry,
    ): List<EditImage> {
        val databaseImages = AlleyImageUtils.getRallyImages(year = year, images = stampRally.images)
            .map {
                EditImage.DatabaseImage(
                    uri = it.uri,
                    name = it.uri.toString(),
                    width = it.width,
                    height = it.height,
                )
            }
        val networkImages = remoteDatabase.listImages(year, stampRally.id)
            .associateBy { it.name }
        return stampRally.images.mapNotNull {
            val targetName = it.name
            networkImages.entries.find { it.key.contains(targetName) }?.value
                ?.fillSize(it.width, it.height)
                ?: databaseImages.find { it.name.contains(targetName) }
        }
    }

    suspend fun loadStampRallyHistory(dataYear: DataYear, stampRallyId: String) =
        remoteDatabase.loadStampRallyHistory(dataYear, stampRallyId)

    suspend fun deleteStampRally(
        dataYear: DataYear,
        expected: StampRallyDatabaseEntry,
    ) = remoteDatabase.deleteStampRally(dataYear, expected)

    suspend fun loadStampRallyFormQueue(): List<StampRallyFormQueueEntry> =
        remoteDatabase.loadStampRallyFormQueue()

    suspend fun loadStampRallyFormQueueHistory(): List<StampRallyFormHistoryEntry> =
        remoteDatabase.loadStampRallyFormHistory()

    suspend fun loadStampRallyWithFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
        stampRallyId: String,
    ): BackendRequest.StampRallyWithFormEntry.Response? =
        remoteDatabase.loadStampRallyWithFormEntry(dataYear, artistId, stampRallyId)

    suspend fun loadStampRallyWithHistoricalFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
        stampRallyId: String,
        formTimestamp: Instant,
    ): BackendRequest.StampRallyWithHistoricalFormEntry.Response? =
        remoteDatabase.loadStampRallyWithHistoricalFormEntry(dataYear, artistId, stampRallyId, formTimestamp)

    suspend fun saveStampRallyAndClearFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
        initial: StampRallyDatabaseEntry?,
        updated: StampRallyDatabaseEntry,
        formEntryTimestamp: Instant,
    ): BackendRequest.StampRallyCommitForm.Response = remoteDatabase.saveStampRallyAndClearFormEntry(
        dataYear = dataYear,
        artistId = artistId,
        initial = initial,
        updated = updated,
        formEntryTimestamp = formEntryTimestamp,
    )
}
