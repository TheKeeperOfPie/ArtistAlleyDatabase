package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.util.toImageBitmap
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
    suspend fun loadArtists(dataYear: DataYear) = when (dataYear) {
        DataYear.ANIME_EXPO_2026 -> remoteDatabase.loadArtists(dataYear)
        DataYear.ANIME_EXPO_2023,
        DataYear.ANIME_EXPO_2024,
        DataYear.ANIME_EXPO_2025,
        DataYear.ANIME_NYC_2024,
        DataYear.ANIME_NYC_2025,
            -> artistEntryDao.getAllEntries(dataYear)
    }

    suspend fun loadArtist(dataYear: DataYear, artistId: Uuid) = when (dataYear) {
        DataYear.ANIME_EXPO_2026 -> remoteDatabase.loadArtist(dataYear, artistId)
        DataYear.ANIME_EXPO_2023,
        DataYear.ANIME_EXPO_2024,
        DataYear.ANIME_EXPO_2025,
        DataYear.ANIME_NYC_2024,
        DataYear.ANIME_NYC_2025,
            -> artistEntryDao.getEntry(dataYear, artistId.toString())
            ?.artist
            ?.databaseEntry
    }

    suspend fun loadSeries() = seriesEntryDao.getSeries()
        .associate {
            it.id to SeriesInfo(
                id = it.id,
                uuid = Uuid.parse(it.uuid),
                notes = it.notes,
                aniListId = it.aniListId,
                aniListType = it.aniListType,
                wikipediaId = it.wikipediaId,
                source = it.source,
                titlePreferred = it.titlePreferred,
                titleEnglish = it.titleEnglish,
                titleRomaji = it.titleRomaji,
                titleNative = it.titleNative,
                link = it.link,
            )
        }

    suspend fun loadMerch() = merchEntryDao.getMerch()
        .associate {
            it.name to MerchInfo(
                name = it.name,
                uuid = it.uuid,
                notes = it.notes,
            )
        }

    fun getArtistEditImages(
        year: DataYear,
        images: List<CatalogImage>
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
            databaseImages[it.name] ?: networkImages[it.name]?.fillSize(it.width, it.height)
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
    ): EditImage? {
        val (width, height) = try {
            platformFile.toImageBitmap().run { width to height }
        } catch (_: Throwable) {
            // TODO: Error handling
            return null
        }
        return remoteDatabase.uploadImage(dataYear, artistId, platformFile)
            .fillSize(width, height)
    }
}
