package com.thekeeperofpie.artistalleydatabase.vgmdb

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao

class VgmdbApi(
    application: Application,
    private val albumEntryDao: AlbumEntryDao,
    private val vgmdbArtistDao: VgmdbArtistDao,
    vgmdbJson: VgmdbJson,
) {

    private val parser = VgmdbParser(application, vgmdbJson.json)

    suspend fun searchAlbums(query: String) =
        parser.search(query)?.albums?.take(5) ?: emptyList()

    suspend fun searchArtists(query: String) =
        parser.search(query)?.artists?.take(5) ?: emptyList()

    suspend fun getAlbum(id: String) = parser.parseAlbum(id)?.also { albumEntryDao.updateEntry(it) }

    suspend fun getArtist(id: String) =
        parser.parseArtist(id)?.also { vgmdbArtistDao.updateEntry(it) }
}