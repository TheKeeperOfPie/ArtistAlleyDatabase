package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
class AlleyEditDatabase(
    private val artistEntryDao: ArtistEntryDao,
    private val merchEntryDao: MerchEntryDao,
    private val seriesEntryDao: SeriesEntryDao,
    private val remoteDatabase: AlleyEditRemoteDatabase,
) {
    suspend fun loadArtists(dataYear: DataYear) = artistEntryDao.getAllEntries(dataYear)

    suspend fun loadArtist(dataYear: DataYear, artistId: Uuid) =
        artistEntryDao.getEntry(dataYear, artistId.toString())
            ?.artist
            ?.let {
                ArtistEditInfo(
                    id = Uuid.parse(it.id),
                    booth = it.booth,
                    name = it.name,
                    summary = it.summary,
                    links = it.links,
                    storeLinks = it.storeLinks,
                    catalogLinks = it.catalogLinks,
                    notes = it.notes,
                    commissions = it.commissions,
                    seriesInferred = it.seriesInferred,
                    seriesConfirmed = it.seriesConfirmed,
                    merchInferred = it.merchInferred,
                    merchConfirmed = it.merchConfirmed,
                )
            }

    suspend fun loadSeries() = seriesEntryDao.getSeries()
        .associate {
            it.id to SeriesInfo(
                id = it.id,
                uuid = it.uuid,
                notes = it.notes,
                aniListId = it.aniListId,
                aniListType = it.aniListType,
                wikipediaId = it.wikipediaId,
                titlePreferred = it.titlePreferred,
                titleEnglish = it.titleEnglish,
                titleRomaji = it.titleRomaji,
                titleNative = it.titleNative,
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

    suspend fun loadArtistImages(year: DataYear, artistId: Uuid) =
        artistEntryDao.getImagesById(year, artistId.toString())
            ?.let { AlleyImageUtils.getArtistImages(year, it) }
            ?.map(EditImage::DatabaseImage)

    suspend fun saveArtist(artist: ArtistDatabaseEntry.Impl) = remoteDatabase.saveArtist(artist)
}
