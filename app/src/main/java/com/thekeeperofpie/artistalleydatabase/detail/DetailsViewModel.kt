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
import com.thekeeperofpie.artistalleydatabase.art.ArtEntrySection
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryViewModel
import com.thekeeperofpie.artistalleydatabase.art.PrintSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val application: Application,
    private val artEntryDao: ArtEntryDao,
) : ArtEntryViewModel() {

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
            withContext(Dispatchers.Main) {
                artistSection.contents.addAll(entry.artists)
                locationSection.contents.addAll(entry.locations)
                seriesSection.contents.addAll(entry.series)
                characterSection.contents.addAll(entry.characters)
                tagSection.contents.addAll(entry.tags)

                var indexOfSize = printSizeSection.options.indexOfFirst {
                    if (it !is ArtEntrySection.Dropdown.Item.Basic<*>) return@indexOfFirst false
                    @Suppress("UNCHECKED_CAST")
                    it as ArtEntrySection.Dropdown.Item.Basic<PrintSize>
                    it.value.printWidth == entry.printWidth
                            && it.value.printHeight == entry.printHeight
                }

                if (indexOfSize < 0) {
                    if (entry.printWidth != null && entry.printHeight != null) {
                        indexOfSize = printSizeSection.options.size - 1
                        (printSizeSection.options.last() as ArtEntrySection.Dropdown.Item.TwoFields)
                            .run {
                                customValue0 = entry.printWidth.toString()
                                customValue1 = entry.printHeight.toString()
                            }
                    } else {
                        indexOfSize = 0
                    }
                }
                printSizeSection.selectedIndex = indexOfSize

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

            artEntryDao.insertEntries(
                entry.copy(
                    artists = artistSection.finalContents(),
                    locations = locationSection.finalContents(),
                    series = seriesSection.finalContents(),
                    characters = characterSection.finalContents(),
                    tags = tagSection.finalContents(),
                    lastEditTime = Date.from(Instant.now()),
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    printWidth = printSizeSection.finalWidth(),
                    printHeight = printSizeSection.finalHeight(),
                )
            )

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}