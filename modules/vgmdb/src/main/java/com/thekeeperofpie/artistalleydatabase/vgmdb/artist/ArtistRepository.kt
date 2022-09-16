package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import kotlinx.coroutines.flow.flow

class ArtistRepository(
    application: ScopedApplication,
    private val artistEntryDao: ArtistEntryDao,
    private val vgmdbApi: VgmdbApi,
) : ApiRepository<ArtistEntry>(application) {

    override suspend fun fetch(id: String) = flow { vgmdbApi.getArtist(id)?.let { emit(it) } }

    override suspend fun getLocal(id: String) = artistEntryDao.getEntry(id)

    override suspend fun insertCachedEntry(value: ArtistEntry) = artistEntryDao.insertEntries(value)
}