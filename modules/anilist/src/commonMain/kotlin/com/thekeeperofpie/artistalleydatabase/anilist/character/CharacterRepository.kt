package com.thekeeperofpie.artistalleydatabase.anilist.character

import artistalleydatabase.modules.anilist.generated.resources.Res
import artistalleydatabase.modules.anilist.generated.resources.aniList_error_fetching_character
import com.anilist.fragment.AniListCharacter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.ApiRepository
import com.thekeeperofpie.artistalleydatabase.utils_compose.StringResourceCompose
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class CharacterRepository(
    scope: ApplicationScope,
    private val json: Json,
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
        StringResourceCompose(Res.string.aniList_error_fetching_character) to e
    }

    private fun makeEntry(character: AniListCharacter) = CharacterEntry(
        character = character,
        json = json,
    )

    fun search(query: String) = characterEntryDao.getEntries(query)
}
