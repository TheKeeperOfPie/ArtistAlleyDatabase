package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.autocomplete.Autocompleter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ArtAddEntryViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDetailsDao,
    aniListApi: AniListApi,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    aniListJson: AniListJson,
    autocompleter: Autocompleter,
    dataConverter: ArtEntryDataConverter,
    private val persister: Persister,
) : ArtEntryDetailsViewModel(
    application,
    artEntryDao,
    aniListApi,
    mediaRepository,
    characterRepository,
    aniListJson,
    autocompleter,
    dataConverter,
) {

    val imageUris = mutableStateListOf<Uri>()

    var saving by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = persister.loadArtEntryTemplate() ?: return@launch
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

            if (success) {
                withContext(Dispatchers.Main) {
                    navHostController.popBackStack()
                }
            }
        }
    }

    fun onClickSaveTemplate() {
        viewModelScope.launch(Dispatchers.Main) {
            val entry = makeEntry(null, "template") ?: return@launch
            withContext(Dispatchers.IO) {
                persister.saveArtEntryTemplate(entry)
            }
        }
    }

    interface Persister {

        fun saveArtEntryTemplate(entry: ArtEntry)

        fun loadArtEntryTemplate(): ArtEntry?
    }
}