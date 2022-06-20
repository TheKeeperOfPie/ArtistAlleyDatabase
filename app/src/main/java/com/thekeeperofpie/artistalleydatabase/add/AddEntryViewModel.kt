package com.thekeeperofpie.artistalleydatabase.add

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val application: Application,
    private val artEntryDao: ArtEntryDao,
) : ArtEntryViewModel() {

    val imageUris = mutableStateListOf<Uri>()

    fun onClickSave(navHostController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {
            imageUris.forEach {
                val id = UUID.randomUUID().toString()
                val outputFile = ArtEntryUtils.getImageFile(application, id)
                val error = ArtEntryUtils.writeEntryImage(application, outputFile, it)
                if (error != null) {
                    withContext(Dispatchers.Main) {
                        errorResource = error
                    }
                    return@launch
                }

                val (imageWidth, imageHeight) = ArtEntryUtils.getImageSize(outputFile)

                artEntryDao.insertEntries(
                    ArtEntry(
                        id = id,
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
            }

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}