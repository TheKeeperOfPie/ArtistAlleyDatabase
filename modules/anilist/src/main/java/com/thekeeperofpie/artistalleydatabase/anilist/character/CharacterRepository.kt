package com.thekeeperofpie.artistalleydatabase.anilist.character

import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class CharacterRepository(
    application: ScopedApplication,
    private val characterEntryDao: CharacterEntryDao,
    private val aniListApi: AniListApi,
) : ApiRepository<CharacterEntry>(application) {

    override suspend fun fetch(id: String) = aniListApi.getCharacter(id)
        .mapNotNull { it?.aniListCharacter }
        .map {
            CharacterEntry(
                id = it.id.toString(),
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
                mediaIds = it.media?.nodes?.mapNotNull { it?.aniListMedia?.id?.toString() }
            )
        }

    override suspend fun getLocal(id: String) = characterEntryDao.getEntry(id.toInt())

    override suspend fun insertCachedEntry(value: CharacterEntry) =
        characterEntryDao.insertEntries(value)
}