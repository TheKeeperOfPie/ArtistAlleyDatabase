package com.thekeeperofpie.artistalleydatabase.art.search

import android.app.Application
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.map
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
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.AppJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
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
) : EntrySearchViewModel<ArtSearchQuery, ArtEntryGridModel>() {

    private val weakMap = WeakHashMap<PagingSource<*, *>, Unit>()
    private val weakMapLock = Mutex(false)

    override val entryGridSelectionController =
        EntryGridSelectionController<ArtEntryGridModel>({ viewModelScope }) {
            it.forEach {
                EntryUtils.getEntryImageFolder(application, it.id).deleteRecursively()
                artEntryDao.delete(it.id.valueId)
            }
        }

    private val entrySections = ArtEntrySections(EntrySection.LockState.DIFFERENT)
    override val sections get() = entrySections.sections

    init {
        entrySections.printSizeSection.setOptions((PrintSize.PORTRAITS + PrintSize.LANDSCAPES.distinct()))
        entrySections.sourceSection.addDifferent()
        entrySections.subscribeSectionPredictions(
            viewModelScope,
            artEntryDao,
            dataConverter,
            mediaRepository,
            characterRepository,
            aniListAutocompleter,
            appJson,
        )
    }

    override fun searchOptions() = snapshotFlow {
        val sourceItem = entrySections.sourceSection.selectedItem().toSource()

        val seriesContents = entrySections.seriesSection.finalContents()
        val characterContents = entrySections.characterSection.finalContents()
        ArtSearchQuery(
            artists = entrySections.artistSection.finalContents().map { it.serializedValue },
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
            tags = entrySections.tagSection.finalContents().map { it.serializedValue },
            printWidth = entrySections.printSizeSection.finalWidth(),
            printHeight = entrySections.printSizeSection.finalHeight(),
            notes = entrySections.notesSection.value.trim(),
            artistsLocked = entrySections.artistSection.lockState?.toSerializedValue(),
            seriesLocked = entrySections.seriesSection.lockState?.toSerializedValue(),
            charactersLocked = entrySections.characterSection.lockState?.toSerializedValue(),
            sourceLocked = entrySections.sourceSection.lockState?.toSerializedValue(),
            tagsLocked = entrySections.tagSection.lockState?.toSerializedValue(),
            notesLocked = entrySections.notesSection.lockState?.toSerializedValue(),
            printSizeLocked = entrySections.printSizeSection.lockState?.toSerializedValue(),
        )
    }

    // TODO: Actually use text query?
    override fun mapQuery(
        query: String,
        options: ArtSearchQuery,
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
}
