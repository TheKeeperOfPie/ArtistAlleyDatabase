package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import artistalleydatabase.modules.vgmdb.generated.resources.Res
import artistalleydatabase.modules.vgmdb.generated.resources.vgmdb_error_fetching_album
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.ApiRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class AlbumRepository(
    scope: ApplicationScope,
    private val albumEntryDao: AlbumEntryDao,
    private val vgmdbApi: VgmdbApi,
) : ApiRepository<AlbumEntry>(scope) {

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
        Res.string.vgmdb_error_fetching_album to e
    }
}
