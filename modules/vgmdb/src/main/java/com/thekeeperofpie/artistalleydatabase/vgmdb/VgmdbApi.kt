package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import okhttp3.OkHttpClient

class VgmdbApi(
    private val albumEntryDao: AlbumEntryDao,
    private val vgmdbArtistDao: VgmdbArtistDao,
    vgmdbJson: VgmdbJson,
    okHttpClient: OkHttpClient,
) {

    private val parser = VgmdbParser(vgmdbJson.json, okHttpClient)

    suspend fun searchAlbums(query: String) =
        parser.search(query)?.albums?.take(5) ?: emptyList()

    suspend fun searchArtists(query: String) =
        parser.search(query)?.artists?.take(5) ?: emptyList()

    suspend fun getAlbum(id: String) = parser.parseAlbum(id)?.also { albumEntryDao.updateEntry(it) }

    suspend fun getArtist(id: String) =
        parser.parseArtist(id)?.also { vgmdbArtistDao.updateEntry(it) }
}
