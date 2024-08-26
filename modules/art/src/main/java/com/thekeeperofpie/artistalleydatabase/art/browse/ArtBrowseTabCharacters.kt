package com.thekeeperofpie.artistalleydatabase.art.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.JsonUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.toUri
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtBrowseTabCharacters @Inject constructor(
    appFileSystem: AppFileSystem,
    artEntryDao: ArtEntryBrowseDao,
    artEntryNavigator: ArtEntryNavigator,
    appJson: AppJson,
    characterRepository: CharacterRepository,
) : BrowseTabViewModel() {

    override val priorityMajor = 0
    override val priorityMinor = 2

    override val tab = BrowseScreen.TabContent(
        "art_entry_browse_characters",
        { R.string.art_browse_tab_characters },
        { Either.Left(characters) },
        artEntryNavigator::navigate,
    )

    private var characters by mutableStateOf(emptyList<BrowseEntryModel>())

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class)
            initializationBarrier()
                .flatMapLatest { artEntryDao.getCharacters() }
                .flatMapLatest {
                    it.flatMap(JsonUtils::readStringList)
                        .map { databaseText ->
                            val entry = databaseText.takeIf { it.contains("{") }
                                ?.let<String, CharacterColumnEntry>(appJson.json::decodeFromString)
                            if (entry == null) {
                                // TODO: Search through the entire database rather than just the
                                //  first 10, also need to fix other data types
                                artEntryDao.getCharacterFlow(databaseText, limit = 10)
                                    .flatMapLatest { it.asFlow() }
                                    .filter {
                                        it.characters(appJson)
                                            .filterIsInstance<Character.Custom>()
                                            .any { it.text.contains(databaseText) }
                                    }
                                    .take(1)
                                    .map {
                                        BrowseEntryModel(
                                            image = EntryUtils.getImageFile(appFileSystem, it.entryId)
                                                ?.toUri()?.toString(),
                                            text = databaseText,
                                            queryType = ArtEntryColumn.CHARACTERS.toString(),
                                        )
                                    }
                            } else {
                                characterRepository.getEntry(entry.id)
                                    .filterNotNull()
                                    .map {
                                        BrowseEntryModel(
                                            image = it.image?.medium,
                                            link = AniListUtils.characterUrl(it.id),
                                            text = CharacterUtils.buildCanonicalName(it)
                                                ?: databaseText,
                                            queryType = ArtEntryColumn.CHARACTERS.toString(),
                                            queryIdOrString = Either.Left(it.id),
                                        )
                                    }
                            }
                                .onStart {
                                    if (entry == null) {
                                        BrowseEntryModel(
                                            text = databaseText,
                                            queryType = ArtEntryColumn.CHARACTERS.toString(),
                                        )
                                    } else {
                                        BrowseEntryModel(
                                            link = AniListUtils.characterUrl(entry.id),
                                            text = CharacterUtils.buildCanonicalName(entry)
                                                ?: databaseText,
                                            queryType = ArtEntryColumn.CHARACTERS.toString(),
                                            queryIdOrString = Either.Left(entry.id),
                                        )
                                    }.let { emit(it) }
                                }
                        }
                        .let {
                            combine(it) {
                                it.distinctBy { it.queryIdOrString }.sortedByText()
                            }
                        }
                }
                .flowOn(Dispatchers.IO)
                .collectLatest { characters = it }
        }
    }
}
