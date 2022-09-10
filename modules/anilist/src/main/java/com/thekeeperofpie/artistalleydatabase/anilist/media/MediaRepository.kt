package com.thekeeperofpie.artistalleydatabase.anilist.media

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.distinctWithBuffer
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MediaRepository(
    private val application: ScopedApplication,
    private val mediaEntryDao: MediaEntryDao,
    private val aniListApi: AniListApi,
) {

    private val fetchMediaFlow = MutableStateFlow(-1)

    init {
        application.scope.launch(Dispatchers.IO) {
            fetchMediaFlow
                .drop(1) // Ignore initial value
                .distinctWithBuffer(10)
                .flatMapLatest { aniListApi.getMedia(it).catch {} }
                .catch {}
                .mapNotNull { it?.aniListMedia }
                .map {
                    MediaEntry(
                        id = it.id,
                        title = MediaEntry.Title(
                            romaji = it.title?.romaji?.trim(),
                            english = it.title?.english?.trim(),
                            native = it.title?.native?.trim(),
                        ),
                        type = it.type?.rawValue?.let(MediaEntry.Type::valueOf),
                        image = MediaEntry.CoverImage(
                            extraLarge = it.coverImage?.extraLarge,
                            large = it.coverImage?.large,
                            medium = it.coverImage?.medium,
                            color = it.coverImage?.color,
                        ),
                        synonyms = it.synonyms?.filterNotNull()?.map(String::trim),
                    )
                }
                .collect(mediaEntryDao::insertEntries)
        }
    }

    suspend fun getEntry(id: Int) = mediaEntryDao.getEntry(id)
        .onEach { if (it == null) fetchMediaFlow.emit(id) }

    fun ensureSaved(id: Int) {
        application.scope.launch(Dispatchers.IO) {
            val entry = mediaEntryDao.getEntry(id).first()
            if (entry == null) {
                fetchMediaFlow.emit(id)
            }
        }
    }
}