package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.NotesDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(SavedStateHandleSaveableApi::class, FlowPreview::class)
@Inject
class ArtistDetailsViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val notesDao: NotesDao,
    private val userEntryDao: UserEntryDao,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route =
        savedStateHandle.toDestination<Destinations.ArtistDetails>(navigationTypeMap)
    val year = route.year
    val id = route.id
    val initialImageIndex = route.imageIndex?.toIntOrNull() ?: 0

    var entry by mutableStateOf<Entry?>(null)
    var otherYears by mutableStateOf(listOf<DataYear>())
    var images by mutableStateOf<List<CatalogImage>>(emptyList())

    val notes by savedStateHandle.saveable(stateSaver = TextFieldState.Saver) {
        mutableStateOf(TextFieldState())
    }

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val entryWithStampRallies = artistEntryDao.getEntryWithStampRallies(year, id)
                ?: return@launch
            val artistWithUserData = entryWithStampRallies.artist
            val artist = artistWithUserData.artist
            val userEntry = artistWithUserData.userEntry
            val stampRallies = entryWithStampRallies.stampRallies

            val catalogImages = AlleyDataUtils.getImages(
                year = artist.year,
                folder = AlleyDataUtils.Folder.CATALOGS,
                file = artist.booth,
            )
            withContext(CustomDispatchers.Main) {
                entry = Entry(
                    artist = artist,
                    userEntry = userEntry,
                    seriesInferred = artist.seriesInferred,
                    seriesConfirmed = artist.seriesConfirmed,
                    stampRallies = stampRallies,
                )
                images = catalogImages
            }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            otherYears = withContext(CustomDispatchers.IO) {
                (DataYear.entries - year).filter { artistEntryDao.getEntry(it, id) != null }
            }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            notesDao.getArtistNotes(id, year)?.notes
                ?.let(notes::setTextAndPlaceCursorAtEnd)
            snapshotFlow { notes.text }
                .drop(1)
                .debounce(500.milliseconds)
                .collectLatest {
                    notesDao.updateArtistNotes(id, year, it.toString())
                }
        }
    }

    fun onFavoriteToggle(favorite: Boolean) {
        val entry = entry ?: return
        entry.favorite = favorite
        viewModelScope.launch(CustomDispatchers.IO) {
            userEntryDao.insertArtistUserEntry(entry.userEntry.copy(favorite = favorite))
        }
    }

    @Stable
    class Entry(
        val artist: ArtistEntry,
        val userEntry: ArtistUserEntry,
        val seriesInferred: List<String>,
        val seriesConfirmed: List<String>,
        val stampRallies: List<StampRallyEntry>,
    ) {
        var favorite by mutableStateOf(userEntry.favorite)
    }
}
