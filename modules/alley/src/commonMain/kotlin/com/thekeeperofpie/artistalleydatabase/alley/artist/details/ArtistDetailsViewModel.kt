package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ArtistDetailsViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val json: Json,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route =
        savedStateHandle.toDestination<Destinations.ArtistDetails>(navigationTypeMap)
    val id = route.id
    val initialImageIndex = route.imageIndex?.toIntOrNull() ?: 0

    var entry by mutableStateOf<Entry?>(null)
    var images by mutableStateOf<List<CatalogImage>>(emptyList())

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val entryWithStampRallies = artistEntryDao.getEntryWithStampRallies(id) ?: return@launch
            val artist = entryWithStampRallies.artist
            val stampRallies = entryWithStampRallies.stampRallies

            val catalogImages = ArtistAlleyUtils.getImages(
                folder = ArtistAlleyUtils.Folder.CATALOGS,
                file = artist.booth,
            )
            val seriesConfirmed = artist.seriesConfirmed
            val seriesInferred = artist.seriesInferred
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
        val seriesInferred: List<String>,
        val seriesConfirmed: List<String>,
        val stampRallies: List<StampRallyEntry>,
    ) {
        var favorite by mutableStateOf(artist.favorite)
    }
}
