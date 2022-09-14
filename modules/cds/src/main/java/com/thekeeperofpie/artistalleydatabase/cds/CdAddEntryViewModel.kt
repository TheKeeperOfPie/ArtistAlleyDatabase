package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
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
    aniListAutocompleter: AniListAutocompleter,
    vgmdbApi: VgmdbApi,
    vgmdbJson: VgmdbJson,
    albumRepository: AlbumRepository,
    artistRepository: ArtistRepository,
    dataConverter: CdEntryDataConverter,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
) : CdEntryDetailsViewModel(
    application,
    cdEntryDao,
    aniListApi,
    aniListJson,
    aniListAutocompleter,
    vgmdbApi,
    vgmdbJson,
    albumRepository,
    artistRepository,
    dataConverter,
    mediaRepository,
    characterRepository,
) {

    val imageUris = mutableStateListOf<Uri>()

    var saving by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch(Dispatchers.Main) {
            catalogIdSection.predictionChosen
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<AlbumEntry>>()
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    val imageUrl = it.value.coverFull?.toUri() ?: return@collectLatest
                    imageUris.clear()
                    imageUris.add(imageUrl)
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
}