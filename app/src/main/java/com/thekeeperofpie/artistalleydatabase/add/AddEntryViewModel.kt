package com.thekeeperofpie.artistalleydatabase.add

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
    aniListApi: AniListApi,
    private val settingsProvider: SettingsProvider,
) : ArtEntryViewModel(application, artEntryDao, aniListApi) {

    val imageUris = mutableStateListOf<Uri>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = settingsProvider.loadArtEntryTemplate() ?: return@launch
            withContext(Dispatchers.Main) {
                initializeForm(entry)
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
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