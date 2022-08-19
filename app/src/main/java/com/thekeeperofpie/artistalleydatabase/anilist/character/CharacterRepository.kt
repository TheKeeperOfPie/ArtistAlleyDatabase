package com.thekeeperofpie.artistalleydatabase.anilist.character

import com.thekeeperofpie.artistalleydatabase.CustomApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.utils.distinctWithBuffer
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
class CharacterRepository(
    private val application: CustomApplication,
    private val characterEntryDao: CharacterEntryDao,
    private val aniListApi: AniListApi,
) {

    private val fetchCharacterFlow = MutableStateFlow(-1)

    init {
        application.scope.launch(Dispatchers.IO) {
            fetchCharacterFlow
                .drop(1) // Ignore initial value
                .distinctWithBuffer(10)
                .flatMapLatest { aniListApi.getCharacter(it) }
                .catch {}
                .mapNotNull { it?.aniListCharacter }
                .map {
                    CharacterEntry(
                        id = it.id,
                        name = CharacterEntry.Name(
                            first = it.name?.first?.trim(),
                            middle = it.name?.middle?.trim(),
                            last = it.name?.last?.trim(),
                            full = it.name?.full?.trim(),
                            native = it.name?.native?.trim(),
                            alternative = it.name?.alternative?.filterNotNull()
                                ?.map(String::trim),
                        ),
                        image = CharacterEntry.Image(
                            large = it.image?.large,
                            medium = it.image?.medium,
                        ),
                        mediaIds = it.media?.nodes?.mapNotNull { it?.aniListMedia?.id }
                    )
                }
                .collect(characterEntryDao::insertEntries)
        }
    }

    suspend fun getEntry(id: Int) = characterEntryDao.getEntry(id)
        .onEach { if (it == null) fetchCharacterFlow.emit(id) }

    fun ensureSaved(id: Int) {
        application.scope.launch(Dispatchers.IO) {
            val entry = characterEntryDao.getEntry(id).first()
            if (entry == null) {
                fetchCharacterFlow.emit(id)
            }
        }
    }
}