package com.thekeeperofpie.artistalleydatabase.anilist.character

import com.anilist.fragment.AniListCharacter
import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.R
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.AppJson

class CharacterRepository(
    application: ScopedApplication,
    private val appJson: AppJson,
    private val characterEntryDao: CharacterEntryDao,
    private val aniListApi: AniListApi,
) : ApiRepository<CharacterEntry>(application) {

    override suspend fun fetch(id: String) = aniListApi.getCharacter(id)?.let(::makeEntry)

    override suspend fun getLocal(id: String) = characterEntryDao.getEntry(id.toInt())

    override suspend fun insertCachedEntry(value: CharacterEntry) =
        characterEntryDao.insertEntries(value)

    override suspend fun ensureSaved(ids: List<String>) = try {
        (ids - characterEntryDao.getEntriesById(ids).toSet())
            .map(String::toInt)
            .let { aniListApi.getCharacters(it) }
            .map(::makeEntry)
            .forEach { insertCachedEntry(it) }
        null
    } catch (e: Exception) {
        R.string.aniList_error_fetching_character to e
    }

    private fun makeEntry(character: AniListCharacter) = CharacterEntry(
        character = character,
        appJson = appJson,
    )

    fun search(query: String) = characterEntryDao.getEntries(query)
}
