package com.thekeeperofpie.artistalleydatabase.cds

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CdAddEntryViewModel @Inject constructor(
    application: Application,
    cdEntryDao: CdEntryDetailsDao,
    aniListApi: AniListApi,
    aniListJson: AniListJson,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
) : CdEntryDetailsViewModel(
    application,
    cdEntryDao,
    aniListApi,
    aniListJson,
    mediaRepository,
    characterRepository,
) {

    val imageUris = mutableStateListOf<Uri>()

    var saving by mutableStateOf(false)
        private set

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
}