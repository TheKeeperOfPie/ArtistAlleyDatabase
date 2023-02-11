package com.thekeeperofpie.artistalleydatabase.search.advanced

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.search.ArtAdvancedSearchQuery
import com.thekeeperofpie.artistalleydatabase.art.sections.PrintSize
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvancedSearchViewModel @Inject constructor(
    application: Application,
    appJson: AppJson,
    artEntryDao: ArtEntryDetailsDao,
    dataConverter: DataConverter,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    aniListAutocompleter: AniListAutocompleter,
    private val searchRepository: AdvancedSearchRepository,
    private val settingsProvider: SettingsProvider,
) : ArtEntryDetailsViewModel(
    application,
    appJson,
    artEntryDao,
    dataConverter,
    mediaRepository,
    characterRepository,
    aniListAutocompleter,
    settingsProvider,
    settingsProvider,
) {

    init {
        printSizeSection.setOptions((PrintSize.PORTRAITS + PrintSize.LANDSCAPES.distinct()))
        sourceSection.addDifferent()
        sections.forEach {
            it.lockState = EntrySection.LockState.DIFFERENT
        }

        viewModelScope.launch(Dispatchers.IO) {
            val model = settingsProvider.loadSearchQuery()?.let(::buildModel) ?: return@launch
            viewModelScope.launch(Dispatchers.Main) {
                initializeForm(model)
            }
        }
    }

    fun onClickClear() {
        initializeForm(
            buildModel(ArtEntry(locks = ArtEntry.Locks(locked = null)))
                .copy(source = SourceType.Different)
        )
    }

    fun onClickSearch(): String {
        val sourceItem = sourceSection.selectedItem().toSource()

        val seriesContents = seriesSection.finalContents()
        val characterContents = characterSection.finalContents()
        val query = ArtAdvancedSearchQuery(
            artists = artistSection.finalContents().map { it.serializedValue },
            source = sourceItem,
            series = seriesContents.filterIsInstance<Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            seriesById = seriesContents
                .filterIsInstance<Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::mediaId),
            characters = characterContents
                .filterIsInstance<Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            charactersById = characterContents
                .filterIsInstance<Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::characterId),
            tags = tagSection.finalContents().map { it.serializedValue },
            printWidth = printSizeSection.finalWidth(),
            printHeight = printSizeSection.finalHeight(),
            notes = notesSection.value.trim(),
            artistsLocked = artistSection.lockState?.toSerializedValue(),
            seriesLocked = seriesSection.lockState?.toSerializedValue(),
            charactersLocked = characterSection.lockState?.toSerializedValue(),
            sourceLocked = sourceSection.lockState?.toSerializedValue(),
            tagsLocked = tagSection.lockState?.toSerializedValue(),
            notesLocked = notesSection.lockState?.toSerializedValue(),
            printSizeLocked = printSizeSection.lockState?.toSerializedValue(),
        )
        searchRepository.registerQuery(query)
        viewModelScope.launch(Dispatchers.IO) {
            makeBaseEntry().let(settingsProvider::saveSearchQuery)
        }
        return query.id
    }
}