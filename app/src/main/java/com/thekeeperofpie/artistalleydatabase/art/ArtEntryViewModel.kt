package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date

abstract class ArtEntryViewModel(
    protected val application: Application,
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
        seriesSection,
        characterSection,
        sourceSection,
        artistSection,
        tagSection,
        printSizeSection,
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

    private suspend fun subscribeMultiTextSection(
        section: ArtEntrySection.MultiText,
        databaseCall: suspend (String) -> List<String>
    ) {
        section.valueUpdates()
            .collectLatest { query ->
                val predictions = databaseCall(query)
                withContext(Dispatchers.Main) {
                    section.predictions = predictions
                }
            }
    }

    protected fun initializeForm(entry: ArtEntry) {
        artistSection.contents.addAll(entry.artists)
        artistSection.locked = entry.locks.artistsLocked

        sourceSection.initialize(entry)
        sourceSection.locked = entry.locks.sourceLocked

        seriesSection.contents.addAll(entry.series)
        seriesSection.locked = entry.locks.seriesLocked

        characterSection.contents.addAll(entry.characters)
        characterSection.locked = entry.locks.charactersLocked

        printSizeSection.initialize(entry.printWidth, entry.printHeight)
        printSizeSection.locked = entry.locks.printSizeLocked

        tagSection.contents.addAll(entry.tags)
        tagSection.locked = entry.locks.tagsLocked

        notesSection.value = entry.notes.orEmpty()
        notesSection.locked = entry.locks.notesLocked
    }

    protected suspend fun makeEntry(imageUri: Uri?, id: String): ArtEntry? {
        val outputFile = ArtEntryUtils.getImageFile(application, id)
        val error = ArtEntryUtils.writeEntryImage(application, outputFile, imageUri)
        if (error != null) {
            withContext(Dispatchers.Main) {
                errorResource = error
            }
            return null
        }
        val (imageWidth, imageHeight) = ArtEntryUtils.getImageSize(outputFile)
        val (sourceType, sourceValue) = sourceSection.finalTypeToValue()

        return ArtEntry(
            id = id,
            artists = artistSection.finalContents(),
            sourceType = sourceType,
            sourceValue = sourceValue,
            series = seriesSection.finalContents(),
            characters = characterSection.finalContents(),
            tags = tagSection.finalContents(),
            lastEditTime = Date.from(Instant.now()),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            printWidth = printSizeSection.finalWidth(),
            printHeight = printSizeSection.finalHeight(),
            notes = notesSection.value.trim(),
            locks = ArtEntry.Locks(
                artistsLocked = artistSection.locked ?: false,
                seriesLocked = seriesSection.locked ?: false,
                charactersLocked = characterSection.locked ?: false,
                sourceLocked = sourceSection.locked ?: false,
                tagsLocked = tagSection.locked ?: false,
                notesLocked = notesSection.locked ?: false,
                printSizeLocked = printSizeSection.locked ?: false,
            )
        )
    }

    suspend fun saveEntry(imageUri: Uri?, id: String) {
        val entry = makeEntry(imageUri, id) ?: return
        artEntryDao.insertEntries(entry)
    }
}