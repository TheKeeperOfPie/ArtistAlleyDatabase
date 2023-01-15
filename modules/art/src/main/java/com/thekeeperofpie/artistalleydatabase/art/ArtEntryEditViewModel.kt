package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArtEntryEditViewModel @Inject constructor(
    application: Application,
    appJson: AppJson,
    artEntryDao: ArtEntryDetailsDao,
    dataConverter: DataConverter,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    aniListAutocompleter: AniListAutocompleter,
) : ArtEntryDetailsViewModel(
    application,
    appJson,
    artEntryDao,
    dataConverter,
    mediaRepository,
    characterRepository,
    aniListAutocompleter,
) {

    var entryId: String? = null
    lateinit var entry: ArtEntry

    var imageUri by mutableStateOf<Uri?>(null)

    var areSectionsLoading by mutableStateOf(true)

    private var deleting = false
    var saving by mutableStateOf(false)
        private set

    fun initialize(entryId: String, entryImageRatio: Float) = apply {
        if (this.entryId != null) return@apply
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
        save(navHostController, skipIgnoreableErrors = false)
    }

    fun onLongClickSave(navHostController: NavHostController) {
        save(navHostController, skipIgnoreableErrors = true)
    }

    private fun save(navHostController: NavHostController, skipIgnoreableErrors: Boolean) {
        if (saving || deleting) return
        saving = true

        viewModelScope.launch(Dispatchers.IO) {
            val success =
                saveEntry(imageUri, entryId!!, skipIgnoreableErrors = skipIgnoreableErrors)
            withContext(Dispatchers.Main) {
                if (success) {
                    navHostController.popBackStack()
                } else {
                    saving = false
                }
            }
        }
    }
}