package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.utils_network.WebScraper
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@SingleIn(AppScope::class)
@Inject
class VgmdbApi(
    private val albumEntryDao: AlbumEntryDao,
    private val vgmdbArtistDao: VgmdbArtistDao,
    vgmdbJson: VgmdbJson,
    httpClient: HttpClient,
    webScraper: WebScraper,
) {
    private val parser = VgmdbParser(vgmdbJson.json, httpClient, webScraper)

    suspend fun searchAlbums(query: String) =
        parser.search(query)?.albums?.take(5) ?: emptyList()

    suspend fun searchArtists(query: String) =
        parser.search(query)?.artists?.take(5) ?: emptyList()

    suspend fun getAlbum(id: String) = parser.parseAlbum(id)?.also { albumEntryDao.updateEntry(it) }

    suspend fun getArtist(id: String) =
        parser.parseArtist(id)?.also { vgmdbArtistDao.updateEntry(it) }
}
