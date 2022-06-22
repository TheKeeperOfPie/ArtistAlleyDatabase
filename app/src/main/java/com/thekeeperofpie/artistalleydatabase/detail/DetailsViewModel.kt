package com.thekeeperofpie.artistalleydatabase.detail

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val application: Application,
    artEntryDao: ArtEntryDao,
) : ArtEntryViewModel(artEntryDao) {

    var entryId: String? = null
    lateinit var entry: ArtEntry

    var imageUri by mutableStateOf<Uri?>(null)

    var areSectionsLoading by mutableStateOf(true)

    var showDeleteDialog by mutableStateOf(false)

    private var deleting = false

    fun initialize(entryId: String, entryImageRatio: Float) {
        if (this.entryId != null) return
        this.entryId = entryId

        if (entryImageRatio > 1f) {
            onImageSizeResult(1, 2)
        } else {
            onImageSizeResult(2, 1)
        }

        viewModelScope.launch(Dispatchers.IO) {
            entry = artEntryDao.getEntry(entryId)
            delay(350)
            withContext(Dispatchers.Main) {
                artistSection.contents.addAll(entry.artists)
                artistSection.locked = entry.locks.artistsLocked

                sourceSection.initialize(entry.sourceType, entry.sourceValue)
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

                areSectionsLoading = false
            }
        }
    }

    fun onConfirmDelete(navHostController: NavHostController) {
        if (deleting) return
        deleting = true

        viewModelScope.launch(Dispatchers.IO) {
            artEntryDao.delete(entryId!!)

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {
            val outputFile = ArtEntryUtils.getImageFile(application, entryId!!)
            val error = ArtEntryUtils.writeEntryImage(application, outputFile, imageUri)
            if (error != null) {
                withContext(Dispatchers.Main) {
                    errorResource = error
                }
                return@launch
            }

            val (imageWidth, imageHeight) = ArtEntryUtils.getImageSize(outputFile)
            val (sourceType, sourceValue) = sourceSection.finalTypeToValue()

            artEntryDao.insertEntries(
                entry.copy(
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
                    notes = notesSection.value,
                )
            )

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}