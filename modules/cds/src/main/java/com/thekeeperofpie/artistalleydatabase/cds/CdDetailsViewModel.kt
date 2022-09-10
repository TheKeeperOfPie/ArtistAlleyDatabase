package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CdDetailsViewModel @Inject constructor(
    application: Application,
    cdEntryDao: CdEntryDetailsDao,
    aniListApi: AniListApi,
    aniListJson: AniListJson,
    vgmdbJson: VgmdbJson,
    dataConverter: CdEntryDataConverter,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
) : CdEntryDetailsViewModel(
    application,
    cdEntryDao,
    aniListApi,
    aniListJson,
    vgmdbJson,
    dataConverter,
    mediaRepository,
    characterRepository,
) {

    var entryId: String? = null
    lateinit var entry: CdEntry

    var imageUri by mutableStateOf<Uri?>(null)

    var areSectionsLoading by mutableStateOf(true)

    private var deleting = false
    var saving by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch(Dispatchers.Main) {
            catalogIdSection.predictionChosen
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<AlbumEntry>>()
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    imageUri = it.value.coverFull?.toUri()
                }
        }
    }

    fun initialize(entryId: String) {
        if (this.entryId != null) return
        this.entryId = entryId

        viewModelScope.launch(Dispatchers.IO) {
            entry = cdEntryDao.getEntry(entryId)
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
            cdEntryDao.delete(entryId!!)

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
        if (saving || deleting) return
        saving = true

        viewModelScope.launch(Dispatchers.IO) {
            if (saveEntry(imageUri, entryId!!)) {
                withContext(Dispatchers.Main) {
                    navHostController.popBackStack()
                }
            }
        }
    }
}