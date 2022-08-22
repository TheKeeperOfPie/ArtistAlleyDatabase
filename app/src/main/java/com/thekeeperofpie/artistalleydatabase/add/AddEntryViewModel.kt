package com.thekeeperofpie.artistalleydatabase.add

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.autocomplete.Autocompleter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.art.json.ArtJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDetailsDao,
    aniListApi: com.thekeeperofpie.artistalleydatabase.anilist.AniListApi,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    artJson: ArtJson,
    autocompleter: Autocompleter,
    dataConverter: ArtEntryDataConverter,
    private val settingsProvider: SettingsProvider,
) : ArtEntryDetailsViewModel(
    application,
    artEntryDao,
    aniListApi,
    mediaRepository,
    characterRepository,
    artJson,
    autocompleter,
    dataConverter,
) {

    val imageUris = mutableStateListOf<Uri>()

    var saving by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = settingsProvider.loadArtEntryTemplate() ?: return@launch
            withContext(Dispatchers.Main) {
                initializeForm(buildModel(entry))
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
        if (saving) return
        saving = true

        viewModelScope.launch(Dispatchers.IO) {
            if (imageUris.isEmpty()) {
                saveEntry(null, UUID.randomUUID().toString())
            } else {
                imageUris.forEach {
                    saveEntry(it, UUID.randomUUID().toString())
                }
            }

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }

    fun onClickSaveTemplate() {
        viewModelScope.launch(Dispatchers.Main) {
            val entry = makeEntry(null, "template") ?: return@launch
            withContext(Dispatchers.IO) {
                settingsProvider.saveArtEntryTemplate(entry)
            }
        }
    }
}