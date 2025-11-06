package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditInfo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
class AlleyEditDatabase(
    private val artistEntryDao: ArtistEntryDao,
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
}
