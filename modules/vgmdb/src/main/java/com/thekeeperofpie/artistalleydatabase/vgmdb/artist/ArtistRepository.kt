package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.R
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class ArtistRepository(
    application: ScopedApplication,
    private val vgmdbArtistDao: VgmdbArtistDao,
    private val vgmdbApi: VgmdbApi,
) : ApiRepository<VgmdbArtist>(application) {

    override suspend fun fetch(id: String) = vgmdbApi.getArtist(id)

    override suspend fun getLocal(id: String) = vgmdbArtistDao.getEntryFlow(id)

    override suspend fun insertCachedEntry(value: VgmdbArtist) = vgmdbArtistDao.insertEntries(value)

    override suspend fun ensureSaved(ids: List<String>) = try {
        withContext(Dispatchers.IO) {
            (ids - vgmdbArtistDao.getEntriesById(ids).toSet())
                .map { async { fetch(it)?.let { insertCachedEntry(it) } } }
                .awaitAll()
        }
        null
    } catch (e: Exception) {
        R.string.vgmdb_error_fetching_artist to e
    }
}