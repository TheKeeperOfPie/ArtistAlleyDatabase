package com.thekeeperofpie.artistalleydatabase.art.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.art.sections.ArtEntrySections
import com.thekeeperofpie.artistalleydatabase.art.sections.PrintSize
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.WeakHashMap
import javax.inject.Inject

@HiltViewModel
open class ArtSearchViewModel @Inject constructor(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDetailsDao,
    private val dataConverter: DataConverter,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
    private val aniListAutocompleter: AniListAutocompleter,
    protected val appJson: AppJson,
) : ViewModel(), EntryGridViewModel<ArtEntryGridModel> {

    var query by mutableStateOf<String>("")
    var entriesSize by mutableStateOf(0)

    val results = MutableStateFlow(PagingData.empty<ArtEntryGridModel>())

    private val weakMap = WeakHashMap<PagingSource<*, *>, Unit>()
    private val weakMapLock = Mutex(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            @Suppress("OPT_IN_USAGE")
            combine(snapshotFlow { query }, searchOptions(), ::Pair)
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (query, options) -> mapQuery(query, options) }
                .collect(results)
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            entriesSize()
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entriesSize = it }
        }
    }

    fun onQuery(query: String) {
        val currentValue = this.query
        if (currentValue != query) {
            clearSelected()
        }
        this.query = query
    }

    suspend fun invalidate() = weakMapLock.withLock(this) {
        weakMap.keys.toList()
    }.forEach { it.invalidate() }

    protected fun <Key : Any, Value : Any> trackPagingSource(
        block: () -> PagingSource<Key, Value>,
    ) = block().apply {
        runBlocking { weakMapLock.withLock(this) { weakMap[this@apply] = Unit } }
    }

    protected suspend fun <Key : Any, Value : Any> PagingSource<Key, Value>.track() =
        weakMapLock.withLock(this) {
            apply { weakMap[this] = Unit }
        }

    override val entryGridSelectionController =
        EntryGridSelectionController<ArtEntryGridModel>({ viewModelScope }) {
            it.forEach {
                EntryUtils.getEntryImageFolder(application, it.id).deleteRecursively()
                artEntryDao.delete(it.id.valueId)
            }
        }

    val sections = ArtEntrySections(EntrySection.LockState.DIFFERENT)

    init {
        sections.printSizeSection.setOptions((PrintSize.PORTRAITS + PrintSize.LANDSCAPES.distinct()))
        sections.sourceSection.addDifferent()
        sections.sections.forEach {
            it.lockState = EntrySection.LockState.DIFFERENT
        }
        sections.subscribeSectionPredictions(
            viewModelScope,
            artEntryDao,
            dataConverter,
            mediaRepository,
            characterRepository,
            aniListAutocompleter,
            appJson,
        )
    }

    private fun searchOptions() = snapshotFlow {
        val sourceItem = sections.sourceSection.selectedItem().toSource()

        val seriesContents = sections.seriesSection.finalContents()
        val characterContents = sections.characterSection.finalContents()
        ArtAdvancedSearchQuery(
            artists = sections.artistSection.finalContents().map { it.serializedValue },
            source = sourceItem,
            series = seriesContents.filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            seriesById = seriesContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::mediaId),
            characters = characterContents
                .filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            charactersById = characterContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::characterId),
            tags = sections.tagSection.finalContents().map { it.serializedValue },
            printWidth = sections.printSizeSection.finalWidth(),
            printHeight = sections.printSizeSection.finalHeight(),
            notes = sections.notesSection.value.trim(),
            artistsLocked = sections.artistSection.lockState?.toSerializedValue(),
            seriesLocked = sections.seriesSection.lockState?.toSerializedValue(),
            charactersLocked = sections.characterSection.lockState?.toSerializedValue(),
            sourceLocked = sections.sourceSection.lockState?.toSerializedValue(),
            tagsLocked = sections.tagSection.lockState?.toSerializedValue(),
            notesLocked = sections.notesSection.lockState?.toSerializedValue(),
            printSizeLocked = sections.printSizeSection.lockState?.toSerializedValue(),
        )
    }

    // TODO: Actually use text query?
    private fun mapQuery(
        query: String,
        options: ArtAdvancedSearchQuery,
    ): Flow<PagingData<ArtEntryGridModel>> =
        Pager(PagingConfig(pageSize = 20)) {
            trackPagingSource { artEntryDao.search(query, options) }
        }
            .flow.catch { emit(PagingData.empty()) }
            .cachedIn(viewModelScope)
            .onEach {
                viewModelScope.launch(Dispatchers.Main) {
                    // TODO: There must be a better way to sync selected items
                    clearSelected()
                }
            }
            .map {
                it.map {
                    ArtEntryGridModel.buildFromEntry(application, appJson, it)
                }
            }

    open fun entriesSize() = artEntryDao.getEntriesSizeFlow()
}
