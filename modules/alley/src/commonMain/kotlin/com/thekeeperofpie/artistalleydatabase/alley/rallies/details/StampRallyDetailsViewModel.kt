package com.thekeeperofpie.artistalleydatabase.alley.rallies.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class StampRallyDetailsViewModel(
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val userEntryDao: UserEntryDao,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route = savedStateHandle.toDestination<Destinations.StampRallyDetails>(navigationTypeMap)
    val id = route.id
    val initialImageIndex = route.imageIndex?.toIntOrNull() ?: 0

    var entry by mutableStateOf<Entry?>(null)
    var images by mutableStateOf<List<CatalogImage>>(emptyList())

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val entryWithArtists = stampRallyEntryDao.getEntryWithArtists(id) ?: return@launch
            val stampRallyWithUserData = entryWithArtists.stampRally
            val stampRally = stampRallyWithUserData.stampRally
            val userEntry = stampRallyWithUserData.userEntry
            val artists = entryWithArtists.artists
            val catalogImages = AlleyDataUtils.getImages(
                year = stampRally.year,
                folder = AlleyDataUtils.Folder.RALLIES,
                file = stampRally.id.replace("-", " - "),
            )

            // Some stamp rallies have artists in non-AA regions, try and show those
            val otherTables = stampRally.tables
                .filter { table ->
                    artists.none { artist ->
                        artist.booth == table.substringBefore("-").trim()
                    }
                }

            withContext(CustomDispatchers.Main) {
                entry = Entry(
                    stampRally = stampRally,
                    userEntry = userEntry,
                    artists = artists,
                    otherTables = otherTables,
                )
                images = catalogImages
            }
        }
    }

    fun onFavoriteToggle(favorite: Boolean) {
        val entry = entry ?: return
        entry.favorite = favorite
        viewModelScope.launch(CustomDispatchers.IO) {
            userEntryDao.insertStampRallyUserEntry(entry.userEntry.copy(favorite = favorite))
        }
    }

    data class Entry(
        val stampRally: StampRallyEntry,
        val userEntry: StampRallyUserEntry,
        val artists: List<ArtistEntry>,
        val otherTables: List<String>,
    ) {
        var favorite by mutableStateOf(userEntry.favorite)
    }
}
