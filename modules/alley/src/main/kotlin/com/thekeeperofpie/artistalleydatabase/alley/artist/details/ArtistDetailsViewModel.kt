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
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.AppJson
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
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
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {
    private val route = savedStateHandle.toDestination<Destinations.ArtistDetails>(navigationTypeMap)
    val id = route.id
    val initialImageIndex = route.imageIndex?.toIntOrNull() ?: 0

    var entry by mutableStateOf<Entry?>(null)
    var images by mutableStateOf<List<CatalogImage>>(emptyList())

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val (artist, stampRallies) = artistEntryDao.getEntryWithStampRallies(id)
                ?: return@launch
            val catalogImages = ArtistAlleyUtils.getImages(application, "catalogs", artist.booth)
            val seriesConfirmed = artist.seriesConfirmed(appJson)
            val seriesInferred = artist.seriesInferred(appJson)
                .toMutableList()
                .apply { removeAll(seriesConfirmed) }
            withContext(CustomDispatchers.Main) {
                entry = Entry(
                    artist = artist,
                    seriesInferred = seriesInferred,
                    seriesConfirmed = seriesConfirmed,
                    stampRallies = stampRallies,
                )
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
        val seriesInferred: List<Series>,
        val seriesConfirmed: List<Series>,
        val stampRallies: List<StampRallyEntry>,
    ) {
        var favorite by mutableStateOf(artist.favorite)
    }
}
