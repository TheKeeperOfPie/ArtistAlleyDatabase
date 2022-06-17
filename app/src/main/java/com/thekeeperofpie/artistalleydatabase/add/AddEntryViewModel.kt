package com.thekeeperofpie.artistalleydatabase.add

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.ui.ArtEntryForm
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

    val imageUris = mutableStateListOf<Uri>()

    val artistSection = ArtEntryForm.FormSection()
    val locationSection = ArtEntryForm.FormSection()
    val seriesSection = ArtEntryForm.FormSection()
    val characterSection = ArtEntryForm.FormSection()
    val tagSection = ArtEntryForm.FormSection()

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun onClickSave(navHostController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {
            imageUris.forEach {
                val id = UUID.randomUUID().toString()
                val error = ArtEntryUtils.writeEntryImage(application, id, it)
                if (error != null) {
                    withContext(Dispatchers.Main) {
                        errorResource = error
                    }
                    return@launch
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
            }

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}