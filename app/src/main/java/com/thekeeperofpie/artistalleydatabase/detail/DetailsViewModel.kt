package com.thekeeperofpie.artistalleydatabase.detail

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.autocomplete.Autocompleter
import com.thekeeperofpie.artistalleydatabase.json.AppJson
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDetailsDao,
    aniListApi: AniListApi,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    appMoshi: AppMoshi,
    appJson: AppJson,
    autocompleter: Autocompleter,
    dataConverter: ArtEntryDataConverter,
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

    var entryId: String? = null
    lateinit var entry: ArtEntry

    var imageUri by mutableStateOf<Uri?>(null)

    var areSectionsLoading by mutableStateOf(true)

    private var deleting = false
    private var saving = false

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
                initializeForm(buildModel(entry))
                areSectionsLoading = false
            }
        }
    }

    fun onConfirmDelete(navHostController: NavHostController) {
        if (deleting || saving) return
        deleting = true

        viewModelScope.launch(Dispatchers.IO) {
            artEntryDao.delete(entryId!!)

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
        if (saving || deleting) return
        saving = true

        viewModelScope.launch(Dispatchers.IO) {
            saveEntry(imageUri, entryId!!)

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}