package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

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

class ArtistRepository(
    application: ScopedApplication,
    private val artistEntryDao: ArtistEntryDao,
    private val vgmdbApi: VgmdbApi,
) : ApiRepository<ArtistEntry>(application) {

    override suspend fun fetch(id: String) = flow { vgmdbApi.getArtist(id)?.let { emit(it) } }

    override suspend fun getLocal(id: String) = artistEntryDao.getEntry(id)

    override suspend fun insertCachedEntry(value: ArtistEntry) = artistEntryDao.insertEntries(value)

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
        R.string.vgmdb_error_fetching_artist to e
    }
}