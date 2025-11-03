package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import artistalleydatabase.modules.vgmdb.generated.resources.Res
import artistalleydatabase.modules.vgmdb.generated.resources.vgmdb_error_fetching_album
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.ApiRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
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
