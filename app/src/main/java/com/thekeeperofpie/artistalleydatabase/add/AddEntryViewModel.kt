package com.thekeeperofpie.artistalleydatabase.add

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val application: Application,
    private val artEntryDao: ArtEntryDao,
) : ViewModel() {

    var imageUri by mutableStateOf<Uri?>(null)

    val artistSection = AddScreen.FormSection()
    val locationSection = AddScreen.FormSection()
    val seriesSection = AddScreen.FormSection()
    val characterSection = AddScreen.FormSection()
    val tagSection = AddScreen.FormSection()

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun onClickSave(navHostController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            imageUri?.let {
                val imageStream = try {
                    application.contentResolver.openInputStream(it)
                } catch (e: Exception) {
                    errorResource = R.string.error_fail_to_load_image to e
                    return@launch
                } ?: run {
                    errorResource = R.string.error_fail_to_load_image to null
                    return@launch
                }

                val output = try {
                    application.filesDir.resolve("entry_images/${id}").outputStream()
                } catch (e: Exception) {
                    errorResource = R.string.error_fail_to_open_file_output to e
                    return@launch
                }

                output.use {
                    imageStream.use {
                        imageStream.copyTo(output)
                    }
                }
            }

            artEntryDao.insertEntries(
                ArtEntry(
                    id = id,
                    artists = artistSection.finalContents(),
                    locations = locationSection.finalContents(),
                    series = seriesSection.finalContents(),
                    characters = characterSection.finalContents(),
                    tags = tagSection.finalContents(),
                )
            )

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}