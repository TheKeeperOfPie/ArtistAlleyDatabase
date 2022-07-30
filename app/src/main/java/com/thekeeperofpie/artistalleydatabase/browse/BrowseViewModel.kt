package com.thekeeperofpie.artistalleydatabase.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.utils.JsonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KMutableProperty0

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BrowseViewModel @Inject constructor(
    artEntryDao: ArtEntryBrowseDao,
    appMoshi: AppMoshi,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    private val aniListSeriesEntryAdapter = appMoshi.aniListSeriesEntryAdapter
    private val aniListCharacterEntryAdapter = appMoshi.aniListCharacterEntryAdapter

    var artists by mutableStateOf(emptyList<BrowseEntryModel>())
    var series by mutableStateOf(emptyList<BrowseEntryModel>())
    var characters by mutableStateOf(emptyList<BrowseEntryModel>())
    var tags by mutableStateOf(emptyList<BrowseEntryModel>())

    val tabs = listOf(
        BrowseScreen.TabContent(
            ArtEntryColumn.ARTISTS,
            { R.string.browse_tab_artists },
            { artists }),
        BrowseScreen.TabContent(
            ArtEntryColumn.SERIES,
            { R.string.browse_tab_series },
            { series }
        ),
        BrowseScreen.TabContent(
            ArtEntryColumn.CHARACTERS,
            { R.string.browse_tab_characters },
            { characters }
        ),
        BrowseScreen.TabContent(
            ArtEntryColumn.TAGS,
            { R.string.browse_tab_tags },
            { tags }
        ),
    )

    init {
        subscribeColumn(this::artists) {
            artEntryDao.getArtists()
                .map {
                    it.flatMap(JsonUtils::readStringList).distinct()
                        .sortedWith(String.CASE_INSENSITIVE_ORDER)
                        .map { BrowseEntryModel(image = null, text = it) }
                }
        }
        subscribeColumn(this::characters) {
            artEntryDao.getCharacters()
                .flatMapLatest {
                    it.flatMap(JsonUtils::readStringList)
                        .distinct()
                        .map { databaseText ->
                            val entry = databaseText.takeIf { it.contains("{") }
                                ?.let(aniListCharacterEntryAdapter::fromJson)
                            entry?.let {
                                characterRepository.getEntry(it.id)
                                    .filterNotNull()
                                    .map {
                                        BrowseEntryModel(
                                            image = it.image?.medium,
                                            link = AniListUtils.characterUrl(it.id),
                                            text = CharacterUtils.buildCanonicalName(it)
                                                ?: databaseText,
                                            query = it.id.toString(),
                                        )
                                    }
                            }
                                .let { it ?: emptyFlow() }
                                .onStart {
                                    if (entry == null) {
                                        BrowseEntryModel(text = databaseText)
                                    } else {
                                        BrowseEntryModel(
                                            link = AniListUtils.characterUrl(entry.id),
                                            text = CharacterUtils.buildCanonicalName(entry)
                                                ?: databaseText,
                                            query = entry.id.toString(),
                                        )
                                    }.let { emit(it) }
                                }
                        }
                        .let { combine(it) { it.sortedByText() } }
                }
        }
        subscribeColumn(this::series) {
            artEntryDao.getSeries()
                .flatMapLatest {
                    it.flatMap(JsonUtils::readStringList)
                        .distinct()
                        .map { databaseText ->
                            val entry = databaseText.takeIf { it.contains("{") }
                                ?.let(aniListSeriesEntryAdapter::fromJson)
                            entry?.let {
                                mediaRepository.getEntry(it.id)
                                    .filterNotNull()
                                    .map {
                                        BrowseEntryModel(
                                            image = it.image?.medium,
                                            link = AniListUtils.mediaUrl(it.type, it.id),
                                            text = it.title?.romaji ?: databaseText,
                                            query = it.id.toString(),
                                        )
                                    }
                            }
                                .let { it ?: emptyFlow() }
                                .onStart {
                                    if (entry == null) {
                                        BrowseEntryModel(text = databaseText)
                                    } else {
                                        BrowseEntryModel(
                                            text = entry.title,
                                            query = entry.id.toString(),
                                        )
                                    }.let { emit(it) }
                                }
                        }
                        .let { combine(it) { it.sortedByText() } }
                }
        }
        subscribeColumn(this::tags) {
            artEntryDao.getTags()
                .map {
                    it.flatMap(JsonUtils::readStringList).distinct()
                        .sortedWith(String.CASE_INSENSITIVE_ORDER)
                        .map { BrowseEntryModel(image = null, text = it) }
                }
        }
    }

    private fun subscribeColumn(
        property: KMutableProperty0<List<BrowseEntryModel>>,
        query: () -> Flow<List<BrowseEntryModel>>,
    ) = viewModelScope.launch(Dispatchers.Main) {
        query()
            .flowOn(Dispatchers.IO)
            .collectLatest(property::set)
    }

    private fun Array<BrowseEntryModel>.sortedByText() = toList().sortedWith { first, second ->
        String.CASE_INSENSITIVE_ORDER.compare(first.text, second.text)
    }
}