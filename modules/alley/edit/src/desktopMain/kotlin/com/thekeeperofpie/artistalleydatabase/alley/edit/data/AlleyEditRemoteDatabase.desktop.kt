package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase {

    private val artistsByDataYearAndId =
        mutableMapOf<DataYear, MutableMap<String, ArtistDatabaseEntry.Impl>>()

    actual suspend fun loadArtist(dataYear: DataYear, artistId: Uuid): ArtistEditInfo? {
        val artist = artistsByDataYearAndId[dataYear]?.get(artistId.toString()) ?: return null
        return ArtistEditInfo(
            id = Uuid.parse(artist.id),
            booth = artist.booth,
            name = artist.name,
            summary = artist.summary,
            links = artist.links,
            storeLinks = artist.storeLinks,
            catalogLinks = artist.catalogLinks,
            notes = artist.notes,
            commissions = artist.commissions,
            seriesInferred = artist.seriesInferred,
            seriesConfirmed = artist.seriesConfirmed,
            merchInferred = artist.merchInferred,
            merchConfirmed = artist.merchConfirmed,
        )
    }

    actual suspend fun loadArtists(dataYear: DataYear) =
        artistsByDataYearAndId[dataYear]?.values.orEmpty().toList()
            .map { ArtistSummary(Uuid.parse(it.id), it.booth, it.name) }

    actual suspend fun saveArtist(dataYear: DataYear, artist: ArtistDatabaseEntry.Impl) {
        artistsByDataYearAndId.getOrPut(dataYear) { mutableMapOf() }[artist.id] = artist
    }
}
