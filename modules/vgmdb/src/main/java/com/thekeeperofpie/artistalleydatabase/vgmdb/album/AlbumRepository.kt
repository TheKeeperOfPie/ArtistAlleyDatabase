package com.thekeeperofpie.artistalleydatabase.vgmdb.album

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

class AlbumRepository(
    private val application: ScopedApplication,
    private val albumEntryDao: AlbumEntryDao,
    private val vgmdbApi: VgmdbApi,
) {

    private val fetchAlbumFlow = MutableSharedFlow<String>()

    init {
        application.scope.launch(Dispatchers.IO) {
            @Suppress("OPT_IN_USAGE")
            fetchAlbumFlow
                .distinctWithBuffer(10)
                .mapLatest { vgmdbApi.getAlbum(it) }
                .catch {}
                .mapNotNull { it }
                .collect(albumEntryDao::insertEntries)
        }
    }

    suspend fun getEntry(id: String) = albumEntryDao.getEntry(id)
        .onEach { if (it == null) fetchAlbumFlow.emit(id) }

    fun ensureSaved(id: String) {
        application.scope.launch(Dispatchers.IO) {
            val entry = albumEntryDao.getEntry(id).first()
            if (entry == null) {
                fetchAlbumFlow.emit(id)
            }
        }
    }
}