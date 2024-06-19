package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.data.Series
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArtistDetailsViewModel @Inject constructor(
    private val application: Application,
    private val artistEntryDao: ArtistEntryDao,
    private val appJson: AppJson,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val id = savedStateHandle.get<String>("id")!!
    val initialImageIndex = savedStateHandle.get<String>("imageIndex")?.toIntOrNull() ?: 0

    var entry by mutableStateOf<Entry?>(null)
    var images by mutableStateOf<List<CatalogImage>>(emptyList())

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val artistEntry = artistEntryDao.getEntry(id) ?: return@launch
            val catalogImages = ArtistAlleyUtils.getImages(application,"catalogs", artistEntry.booth)
            val series = artistEntry.seriesConfirmed(appJson)
                .ifEmpty { artistEntry.seriesInferred(appJson) }
            withContext(CustomDispatchers.Main) {
                entry = Entry(artistEntry, series)
                images = catalogImages
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
        val series: List<Series>,
    ) {
        var favorite by mutableStateOf(artist.favorite)
    }
}
