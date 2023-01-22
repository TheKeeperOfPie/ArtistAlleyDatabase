package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ArtEntryAddViewModel @Inject constructor(
    application: Application,
    appJson: AppJson,
    artEntryDao: ArtEntryDetailsDao,
    dataConverter: DataConverter,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    aniListAutocompleter: AniListAutocompleter,
    settings: ArtSettings,
) : ArtEntryDetailsViewModel(
    application,
    appJson,
    artEntryDao,
    dataConverter,
    mediaRepository,
    characterRepository,
    aniListAutocompleter,
    settings,
) {

    val imageUris = mutableStateListOf<Uri>()

    var saving by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = settings.loadArtEntryTemplate() ?: return@launch
            withContext(Dispatchers.Main) {
                initializeForm(buildModel(entry))
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
        if (saving) return
        saving = true

        viewModelScope.launch(Dispatchers.IO) {
            val success = if (imageUris.isEmpty()) {
                saveEntry(null, UUID.randomUUID().toString())
            } else {
                imageUris.all {
                    saveEntry(it, UUID.randomUUID().toString())
                }
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    navHostController.popBackStack()
                } else {
                    saving = false
                }
            }
        }
    }

    fun onClickSaveTemplate() {
        viewModelScope.launch(Dispatchers.Main) {
            val entry = makeEntry(null, "template") ?: return@launch
            withContext(Dispatchers.IO) {
                settings.saveArtEntryTemplate(entry)
            }
        }
    }
}