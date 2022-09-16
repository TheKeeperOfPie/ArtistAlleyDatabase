package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import kotlinx.coroutines.flow.flow

class AlbumRepository(
    application: ScopedApplication,
    private val albumEntryDao: AlbumEntryDao,
    private val vgmdbApi: VgmdbApi,
) : ApiRepository<AlbumEntry>(application) {

    override suspend fun fetch(id: String) = flow { vgmdbApi.getAlbum(id)?.let { emit(it) } }

    override suspend fun getLocal(id: String) = albumEntryDao.getEntry(id)

    override suspend fun insertCachedEntry(value: AlbumEntry) = albumEntryDao.insertEntries(value)
}