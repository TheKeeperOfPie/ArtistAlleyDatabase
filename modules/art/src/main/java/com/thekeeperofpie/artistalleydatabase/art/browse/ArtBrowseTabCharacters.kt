package com.thekeeperofpie.artistalleydatabase.art.browse

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.JsonUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
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
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

class ArtBrowseTabCharacters @Inject constructor(
    context: Application,
    artEntryDao: ArtEntryBrowseDao,
    artEntryNavigator: ArtEntryNavigator,
    aniListDataConverter: AniListDataConverter,
    appJson: AppJson,
    characterRepository: CharacterRepository,
) : BrowseTabViewModel() {

    override val priorityMajor = 0
    override val priorityMinor = 2

    override val tab = BrowseScreen.TabContent(
        "art_entry_browse_characters",
        { R.string.art_browse_tab_characters },
        { characters },
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
                                //  first 10, also need to fix series
                                artEntryDao.getCharacterFlow(databaseText, limit = 10)
                                    .flatMapLatest { it.asFlow() }
                                    .filter {
                                        it.characters
                                            .map(aniListDataConverter::databaseToCharacterEntry)
                                            .filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                                            .any { it.text.contains(databaseText) }
                                    }
                                    .take(1)
                                    .map {
                                        BrowseEntryModel(
                                            image = ArtEntryUtils.getImageFile(context, it.id)
                                                .toUri().toString(),
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