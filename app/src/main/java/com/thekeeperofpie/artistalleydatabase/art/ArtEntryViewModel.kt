package com.thekeeperofpie.artistalleydatabase.art

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.JsonUtils
import com.thekeeperofpie.artistalleydatabase.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ArtEntryViewModel(
    protected val artEntryDao: ArtEntryDao
) : ViewModel() {

    val artistSection = ArtEntrySection.MultiText(
        R.string.art_entry_artists_header_zero,
        R.string.art_entry_artists_header_one,
        R.string.art_entry_artists_header_many,
        locked = false,
    )
    val seriesSection = ArtEntrySection.MultiText(
        R.string.art_entry_series_header_zero,
        R.string.art_entry_series_header_one,
        R.string.art_entry_series_header_many,
        locked = false,
    )
    val characterSection = ArtEntrySection.MultiText(
        R.string.art_entry_characters_header_zero,
        R.string.art_entry_characters_header_one,
        R.string.art_entry_characters_header_many,
        locked = false,
    )
    val tagSection = ArtEntrySection.MultiText(
        R.string.art_entry_tags_header_zero,
        R.string.art_entry_tags_header_one,
        R.string.art_entry_tags_header_many,
        locked = false,
    )

    val printSizeSection = PrintSizeDropdown()

    val sourceSection = SourceDropdown(locked = false)

    val notesSection = ArtEntrySection.LongText(R.string.art_entry_notes_header, locked = false)

    val sections = listOf(
        artistSection,
        sourceSection,
        seriesSection,
        characterSection,
        printSizeSection,
        tagSection,
        notesSection,
    )

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun onImageSizeResult(width: Int, height: Int) {
        printSizeSection.onSizeChange(width, height)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(artistSection, artEntryDao::queryArtists)
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(seriesSection, artEntryDao::querySeries)
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(characterSection, artEntryDao::queryCharacters)
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(tagSection, artEntryDao::queryTags)
        }
    }

    suspend fun subscribeMultiTextSection(
        section: ArtEntrySection.MultiText,
        databaseCall: suspend (String) -> List<String>
    ) {
        section.valueUpdates()
            .collectLatest {
                val predictions = databaseCall(it).flatMap(JsonUtils::readStringList)
                withContext(Dispatchers.Main) {
                    section.predictions = predictions
                }
            }
    }
}