package com.thekeeperofpie.artistalleydatabase.alley.rallies.details

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
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.NotesDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(SavedStateHandleSaveableApi::class, FlowPreview::class)
@Inject
class StampRallyDetailsViewModel(
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val notesDao: NotesDao,
    private val userEntryDao: UserEntryDao,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route = savedStateHandle.toDestination<Destinations.StampRallyDetails>(navigationTypeMap)
    val year = route.year
    val id = route.id
    val initialImageIndex = route.initialImageIndex?.toIntOrNull() ?: 0

    // Block main to load images as fast as possible so shared transition works
    val images = AlleyDataUtils.getImages(
        year = route.year,
        folder = AlleyDataUtils.Folder.RALLIES,
        file = route.let { "${it.hostTable}${it.fandom}" },
    )

    var entry by mutableStateOf<Entry?>(null)

    val notes by savedStateHandle.saveable(stateSaver = TextFieldState.Saver) {
        mutableStateOf(TextFieldState())
    }

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val entryWithArtists = stampRallyEntryDao.getEntryWithArtists(year, id) ?: return@launch
            val stampRallyWithUserData = entryWithArtists.stampRally
            val stampRally = stampRallyWithUserData.stampRally
            val artists = entryWithArtists.artists

            // Some stamp rallies have artists in non-AA regions, try and show those
            val otherTables = stampRally.tables
                .filter { table ->
                    artists.none { artist ->
                        artist.booth == table.substringBefore("-").trim()
                    }
                }

            entry = Entry(
                stampRally = stampRally,
                userEntry = stampRallyWithUserData.userEntry,
                artists = artists,
                otherTables = otherTables,
            )
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            notesDao.getStampRallyNotes(id)?.notes
                ?.let(notes::setTextAndPlaceCursorAtEnd)
            snapshotFlow { notes.text }
                .drop(1)
                .debounce(500.milliseconds)
                .collectLatest {
                    notesDao.updateStampRallyNotes(id, it.toString())
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

    @Stable
    class Entry(
        val stampRally: StampRallyEntry,
        val userEntry: StampRallyUserEntry,
        val artists: List<ArtistEntry>,
        val otherTables: List<String>,
    ) {
        var favorite by mutableStateOf(userEntry.favorite)
    }
}
