package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.toMerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
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
    private val remoteDatabase: AlleyEditRemoteDatabase,
) {
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

    suspend fun saveSeries(initial: SeriesInfo?, updated: SeriesInfo): SeriesSave.Response.Result =
        remoteDatabase.saveSeries(initial, updated)

    suspend fun saveMerch(initial: MerchInfo?, updated: MerchInfo): MerchSave.Response.Result =
        remoteDatabase.saveMerch(initial, updated)

    suspend fun generateFormLink(dataYear: DataYear, artistId: Uuid): String? =
        remoteDatabase.generateFormLink(dataYear, artistId)

    suspend fun loadArtistFormQueue(): List<ArtistFormQueueEntry> =
        remoteDatabase.loadArtistFormQueue()

    suspend fun loadArtistWithFormEntry(
        dataYear: DataYear,
        artistId: Uuid,
    ): BackendRequest.ArtistWithFormEntry.Response? =
        remoteDatabase.loadArtistWithFormEntry(dataYear, artistId)

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
}
