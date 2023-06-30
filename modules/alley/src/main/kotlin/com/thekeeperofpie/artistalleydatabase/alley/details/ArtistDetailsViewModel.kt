package com.thekeeperofpie.artistalleydatabase.alley.details

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.CatalogImage
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArtistDetailsViewModel @Inject constructor(
    private val application: Application,
    private val artistEntryDao: ArtistEntryDao,
) : ViewModel() {

    var entry by mutableStateOf<Entry?>(null)
    var images by mutableStateOf<List<CatalogImage>>(emptyList())

    private val favoriteUpdates = MutableSharedFlow<ArtistEntry>(1, 1)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            favoriteUpdates.collectLatest {
            }
        }
    }

    fun initialize(id: String) {
        if (entry == null) {
            viewModelScope.launch(CustomDispatchers.IO) {
                val artistEntry = artistEntryDao.getEntry(id) ?: return@launch
                val catalogImages = ArtistAlleyUtils.getImages(application, artistEntry.booth)

                withContext(CustomDispatchers.Main) {
                    entry = Entry(artistEntry)
                    images = catalogImages
                }
            }
        }
    }

    fun onFavoriteToggle(favorite: Boolean) {
        val entry = entry ?: return
        entry.favorite = favorite
        viewModelScope.launch(CustomDispatchers.IO) {
            artistEntryDao.insertEntries(entry.artist.copy(favorite = favorite))
        }
    }

    data class Entry(
        val artist: ArtistEntry,
    ) {
        var favorite by mutableStateOf(artist.favorite)

        val links = (artist.links.flatMap { it.split("\n") } + artist.contactLink)
            .filterNotNull()
            .filterNot(String::isBlank)
            .distinct()

        val catalogLinks = artist.catalogLink.flatMap { it.split("\n") }
            .filterNot(String::isBlank)
            .distinct()
    }
}
