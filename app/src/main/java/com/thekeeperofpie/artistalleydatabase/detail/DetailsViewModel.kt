package com.thekeeperofpie.artistalleydatabase.detail

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
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
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val application: Application,
    private val artEntryDao: ArtEntryDao,
) : ViewModel() {

    var entryId: String? = null
    lateinit var entry: ArtEntry

    var imageUri by mutableStateOf<Uri?>(null)

    val artistSection = ArtEntryForm.FormSection()
    val locationSection = ArtEntryForm.FormSection()
    val seriesSection = ArtEntryForm.FormSection()
    val characterSection = ArtEntryForm.FormSection()
    val tagSection = ArtEntryForm.FormSection()

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    var areSectionsLoading by mutableStateOf(true)

    fun initialize(entryId: String) {
        if (this.entryId != null) return
        this.entryId = entryId
        viewModelScope.launch(Dispatchers.IO) {
            entry = artEntryDao.getEntry(entryId)
            withContext(Dispatchers.Main) {
                artistSection.contents.addAll(entry.artists)
                locationSection.contents.addAll(entry.locations)
                seriesSection.contents.addAll(entry.series)
                characterSection.contents.addAll(entry.characters)
                tagSection.contents.addAll(entry.tags)

                areSectionsLoading = false
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {
            val error = ArtEntryUtils.writeEntryImage(application, entryId!!, imageUri)
            if (error != null) {
                withContext(Dispatchers.Main) {
                    errorResource = error
                }
                return@launch
            }

            artEntryDao.insertEntries(
                entry.copy(
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