package com.thekeeperofpie.artistalleydatabase.anilist.character

import com.anilist.fragment.AniListCharacter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.R
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import com.thekeeperofpie.artistalleydatabase.utils_compose.ApiRepository
import com.thekeeperofpie.artistalleydatabase.utils_compose.StringResourceId

class CharacterRepository(
    scope: ApplicationScope,
    private val appJson: AppJson,
    private val characterEntryDao: CharacterEntryDao,
    private val aniListApi: AniListApi,
) : ApiRepository<CharacterEntry>(scope) {

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
        StringResourceId(R.string.aniList_error_fetching_character) to e
    }

    private fun makeEntry(character: AniListCharacter) = CharacterEntry(
        character = character,
        appJson = appJson,
    )

    fun search(query: String) = characterEntryDao.getEntries(query)
}
