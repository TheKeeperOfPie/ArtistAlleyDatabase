package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao

class VgmdbApi(
    private val albumEntryDao: AlbumEntryDao,
) {

    private val parser = VgmdbParser()

    suspend fun getAlbum(id: String) = parser.parseAlbum(id)?.also { albumEntryDao.updateEntry(it) }

    suspend fun search(query: String) = VgmdbParser().search(query)?.albums?.take(5)
}