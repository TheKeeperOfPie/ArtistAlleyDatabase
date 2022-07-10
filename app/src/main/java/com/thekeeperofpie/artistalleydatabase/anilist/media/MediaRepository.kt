package com.thekeeperofpie.artistalleydatabase.anilist.media

import com.thekeeperofpie.artistalleydatabase.CustomApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.LinkedList

@OptIn(ExperimentalCoroutinesApi::class)
class MediaRepository(
    application: CustomApplication,
    private val mediaEntryDao: MediaEntryDao,
    private val aniListApi: AniListApi,
) {

    private val fetchMediaFlow = MutableStateFlow(-1)

    init {
        application.scope.launch(Dispatchers.IO) {
            fetchMediaFlow
                .drop(1) // Ignore initial value
                .distinctWithBuffer(10)
                .flatMapLatest { aniListApi.getMedia(it) }
                .mapNotNull { it?.aniListMedia }
                .map(MediaEntry::from)
                .collect(mediaEntryDao::insertEntries)
        }
    }

    private fun <T> Flow<T>.distinctWithBuffer(bufferSize: Int): Flow<T> = flow {
        val past = LinkedList<T>()
        collect {
            val contains = past.contains(it)
            if (!contains) {
                while (past.size > bufferSize) {
                    past.removeFirst()
                }
                past.addLast(it)
                emit(it)
            }
        }
    }

    suspend fun getEntry(id: Int) = mediaEntryDao.getEntry(id)
        .onEach { if (it == null) fetchMediaFlow.emit(id) }
}