package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.AlleyAniListApi
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.NotesDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
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
    private val aniListApi: AlleyAniListApi,
    private val artistEntryDao: ArtistEntryDao,
    private val notesDao: NotesDao,
    private val tagEntryDao: TagEntryDao,
    private val userEntryDao: UserEntryDao,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route =
        savedStateHandle.toDestination<Destinations.ArtistDetails>(navigationTypeMap)
    val year = route.year
    val id = route.id
    val initialImageIndex = route.imageIndex ?: 0

    var entry by mutableStateOf<Entry?>(null)
        private set

    var otherYears by mutableStateOf(listOf<DataYear>())
        private set

    var catalogImages by mutableStateOf<List<CatalogImage>>(emptyList())
        private set

    var seriesImages by mutableStateOf<Map<Int, String>>(emptyMap())
        private set

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
            val seriesInferred = artist.seriesInferred.map { tagEntryDao.getSeriesById(it) }
            val seriesConfirmed = artist.seriesConfirmed.map { tagEntryDao.getSeriesById(it) }

            val entry = Entry(
                artist = artist,
                userEntry = userEntry,
                seriesInferred = seriesInferred,
                seriesConfirmed = seriesConfirmed,
                stampRallies = stampRallies,
            )

            Snapshot.withMutableSnapshot {
                this@ArtistDetailsViewModel.entry = entry
                this@ArtistDetailsViewModel.catalogImages = catalogImages
            }

            seriesImages = aniListApi.getSeriesImages(entry.seriesInferred + entry.seriesConfirmed)
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
        val seriesInferred: List<SeriesEntry>,
        val seriesConfirmed: List<SeriesEntry>,
        val stampRallies: List<StampRallyEntry>,
    ) {
        var favorite by mutableStateOf(userEntry.favorite)
    }
}
