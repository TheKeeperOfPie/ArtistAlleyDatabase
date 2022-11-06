package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.R
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext

class AlbumRepository(
    application: ScopedApplication,
    private val albumEntryDao: AlbumEntryDao,
    private val vgmdbApi: VgmdbApi,
) : ApiRepository<AlbumEntry>(application) {

    override suspend fun fetch(id: String) = flow { vgmdbApi.getAlbum(id)?.let { emit(it) } }

    override suspend fun getLocal(id: String) = albumEntryDao.getEntry(id)

    override suspend fun insertCachedEntry(value: AlbumEntry) = albumEntryDao.insertEntries(value)

    override suspend fun ensureSaved(ids: List<String>) = try {
        withContext(Dispatchers.IO) {
            ids.map {
                async {
                    fetch(it).take(1)
                        .collectLatest(::insertCachedEntry)
                }
            }.awaitAll()
        }
        null
    } catch (e: Exception) {
        R.string.vgmdb_error_fetching_album to e
    }
}