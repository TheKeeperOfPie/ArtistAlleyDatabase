package com.thekeeperofpie.artistalleydatabase.search.advanced

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.PrintSize
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvancedSearchViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDetailsDao,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    aniListJson: AniListJson,
    aniListAutocompleter: AniListAutocompleter,
    aniListDataConverter: AniListDataConverter,
    private val searchRepository: AdvancedSearchRepository,
    private val settingsProvider: SettingsProvider,
) : ArtEntryDetailsViewModel(
    application,
    artEntryDao,
    mediaRepository,
    characterRepository,
    aniListJson,
    aniListAutocompleter,
    aniListDataConverter,
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
        initializeForm(buildModel(ArtEntry()))
    }

    fun onClickSearch(): String {
        val sourceItem = sourceSection.selectedItem().toSource()

        val seriesContents = seriesSection.finalContents()
        val characterContents = characterSection.finalContents()
        val query = AdvancedSearchQuery(
            artists = artistSection.finalContents().map { it.serializedValue },
            source = sourceItem,
            series = seriesContents.filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            seriesById = seriesContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .map { it.id },
            characters = characterContents
                .filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            charactersById = characterContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .map { it.id },
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
            makeEntry(null, query.id)?.let(settingsProvider::saveSearchQuery)
        }
        return query.id
    }
}