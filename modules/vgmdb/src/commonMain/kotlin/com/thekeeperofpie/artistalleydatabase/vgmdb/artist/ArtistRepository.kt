package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import artistalleydatabase.modules.vgmdb.generated.resources.Res
import artistalleydatabase.modules.vgmdb.generated.resources.vgmdb_error_fetching_artist
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.ApiRepository
import com.thekeeperofpie.artistalleydatabase.utils_compose.StringResourceCompose
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class ArtistRepository(
    scope: ApplicationScope,
    private val vgmdbArtistDao: VgmdbArtistDao,
    private val vgmdbApi: VgmdbApi,
) : ApiRepository<VgmdbArtist>(scope) {

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
        StringResourceCompose(Res.string.vgmdb_error_fetching_artist) to e
    }
}
