package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
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
class CdEntryEditViewModel @Inject constructor(
    application: Application,
    cdEntryDao: CdEntryDetailsDao,
    aniListJson: AniListJson,
    aniListDataConverter: AniListDataConverter,
    aniListAutocompleter: AniListAutocompleter,
    vgmdbApi: VgmdbApi,
    vgmdbJson: VgmdbJson,
    vgmdbDataConverter: VgmdbDataConverter,
    vgmdbAutocompleter: VgmdbAutocompleter,
    albumRepository: AlbumRepository,
    artistRepository: ArtistRepository,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
) : CdEntryDetailsViewModel(
    application,
    cdEntryDao,
    aniListJson,
    aniListDataConverter,
    aniListAutocompleter,
    vgmdbApi,
    vgmdbJson,
    vgmdbDataConverter,
    vgmdbAutocompleter,
    albumRepository,
    artistRepository,
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

    fun initialize(entryId: String) = apply {
        if (this.entryId != null) return@apply
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
            val success = saveEntry(imageUri, entryId!!)
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