package com.thekeeperofpie.artistalleydatabase.search.advanced

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.PrintSize
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntrySection
import com.thekeeperofpie.artistalleydatabase.autocomplete.Autocompleter
import com.thekeeperofpie.artistalleydatabase.json.AppJson
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdvancedSearchViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDetailsDao,
    aniListApi: AniListApi,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    appMoshi: AppMoshi,
    appJson: AppJson,
    autocompleter: Autocompleter,
    dataConverter: ArtEntryDataConverter,
    private val searchRepository: AdvancedSearchRepository,
) : ArtEntryDetailsViewModel(
    application,
    artEntryDao,
    aniListApi,
    mediaRepository,
    characterRepository,
    appMoshi,
    appJson,
    autocompleter,
    dataConverter,
) {

    init {
        printSizeSection.setOptions((PrintSize.PORTRAITS + PrintSize.LANDSCAPES.distinct()))
        sourceSection.addDifferent()
        sections.forEach {
            it.lockState = ArtEntrySection.LockState.DIFFERENT
        }
    }

    fun onClickSearch(): String {
        val sourceItem = sourceSection.selectedItem().toSource()

        val seriesContents = seriesSection.finalContents()
        val characterContents = characterSection.finalContents()
        val query = AdvancedSearchQuery(
            artists = artistSection.finalContents().map { it.serializedValue },
            source = sourceItem,
            series = seriesContents.filterIsInstance<ArtEntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            seriesById = seriesContents.filterIsInstance<ArtEntrySection.MultiText.Entry.Prefilled>()
                .mapNotNull { it.id.toIntOrNull() },
            characters = characterContents
                .filterIsInstance<ArtEntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            charactersById = characterContents
                .filterIsInstance<ArtEntrySection.MultiText.Entry.Prefilled>()
                .mapNotNull { it.id.toIntOrNull() },
            tags = tagSection.finalContents().map { it.serializedValue },
            printWidth = printSizeSection.finalWidth(),
            printHeight = printSizeSection.finalHeight(),
            notes = notesSection.value.trim(),
            artistsLocked = artistSection.lockState?.toNullableBoolean(),
            seriesLocked = seriesSection.lockState?.toNullableBoolean(),
            charactersLocked = characterSection.lockState?.toNullableBoolean(),
            sourceLocked = sourceSection.lockState?.toNullableBoolean(),
            tagsLocked = tagSection.lockState?.toNullableBoolean(),
            notesLocked = notesSection.lockState?.toNullableBoolean(),
            printSizeLocked = printSizeSection.lockState?.toNullableBoolean(),
        )
        searchRepository.registerQuery(query)
        return query.id
    }
}