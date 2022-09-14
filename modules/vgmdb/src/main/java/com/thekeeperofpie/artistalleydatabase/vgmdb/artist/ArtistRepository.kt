package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.distinctWithBuffer
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ArtistRepository(
    private val application: ScopedApplication,
    private val artistEntryDao: ArtistEntryDao,
    private val vgmdbApi: VgmdbApi,
) {

    private val fetchAlbumFlow = MutableSharedFlow<String>()

    init {
        application.scope.launch(Dispatchers.IO) {
            @Suppress("OPT_IN_USAGE")
            fetchAlbumFlow
                .distinctWithBuffer(10)
                .mapLatest { vgmdbApi.getArtist(it) }
                .catch {}
                .mapNotNull { it }
                .collect(artistEntryDao::insertEntries)
        }
    }

    suspend fun getEntry(id: String) = artistEntryDao.getEntry(id)
        .onEach { if (it == null) fetchAlbumFlow.emit(id) }

    fun ensureSaved(id: String) {
        application.scope.launch(Dispatchers.IO) {
            val entry = artistEntryDao.getEntry(id).first()
            if (entry == null) {
                fetchAlbumFlow.emit(id)
            }
        }
    }
}