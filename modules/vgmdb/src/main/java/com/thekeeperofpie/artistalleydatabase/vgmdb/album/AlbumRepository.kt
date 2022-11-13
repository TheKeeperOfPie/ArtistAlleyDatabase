package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.R
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class AlbumRepository(
    application: ScopedApplication,
    private val albumEntryDao: AlbumEntryDao,
    private val vgmdbApi: VgmdbApi,
) : ApiRepository<AlbumEntry>(application) {

    override suspend fun fetch(id: String) = vgmdbApi.getAlbum(id)

    override suspend fun getLocal(id: String) = albumEntryDao.getEntryFlow(id)

    override suspend fun insertCachedEntry(value: AlbumEntry) = albumEntryDao.insertEntries(value)

    override suspend fun ensureSaved(ids: List<String>) = try {
        withContext(Dispatchers.IO) {
            (ids - albumEntryDao.getEntriesById(ids).toSet())
                .map { async { fetch(it)?.let { insertCachedEntry(it) } } }
                .awaitAll()
        }
        null
    } catch (e: Exception) {
        R.string.vgmdb_error_fetching_album to e
    }
}