package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntryDao

class VgmdbApi(
    private val albumEntryDao: AlbumEntryDao,
    private val artistEntryDao: ArtistEntryDao,
    vgmdbJson: VgmdbJson,
) {

    private val parser = VgmdbParser(vgmdbJson.json)

    suspend fun searchAlbums(query: String) =
        parser.search(query)?.albums?.take(5) ?: emptyList()

    suspend fun searchArtists(query: String) =
        parser.search(query)?.artists?.take(5) ?: emptyList()

    suspend fun getAlbum(id: String) = parser.parseAlbum(id)?.also { albumEntryDao.updateEntry(it) }

    suspend fun getArtist(id: String) =
        parser.parseArtist(id)?.also { artistEntryDao.updateEntry(it) }
}