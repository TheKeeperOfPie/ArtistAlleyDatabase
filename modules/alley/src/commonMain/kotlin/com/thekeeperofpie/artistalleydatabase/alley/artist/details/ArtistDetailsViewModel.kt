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
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
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
class ArtistDetailsViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val userNotesDao: UserNotesDao,
    private val seriesImagesStore: SeriesImagesStore,
    private val seriesEntryDao: SeriesEntryDao,
    private val userEntryDao: UserEntryDao,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route =
        savedStateHandle.toDestination<Destinations.ArtistDetails>(navigationTypeMap)
    val year = route.year
    val id = route.id
    val initialImageIndex = route.imageIndex ?: 0

    // Block main to load images as fast as possible so shared transition works
    var catalogImages by mutableStateOf(
        AlleyDataUtils.getImages(
            year = route.year,
            folder = AlleyDataUtils.Folder.CATALOGS,
            file = route.booth,
        )
    )

    var entry by mutableStateOf<Entry?>(null)
        private set

    var otherYears by mutableStateOf(listOf<DataYear>())
        private set

    var seriesImages by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    val userNotes by savedStateHandle.saveable(stateSaver = TextFieldState.Saver) {
        mutableStateOf(TextFieldState())
    }

    init {
        val hasImages = catalogImages.isNotEmpty()
        viewModelScope.launch(CustomDispatchers.IO) {
            val entryWithStampRallies = artistEntryDao.getEntryWithStampRallies(year, id)
                ?: return@launch
            val artistWithUserData = entryWithStampRallies.artist
            val artist = artistWithUserData.artist
            val seriesInferred = seriesEntryDao.getSeriesByIds(artist.seriesInferred)
            val seriesConfirmed = seriesEntryDao.getSeriesByIds(artist.seriesConfirmed)

            val entry = Entry(
                artist = artist,
                userEntry = artistWithUserData.userEntry,
                seriesInferred = seriesInferred,
                seriesConfirmed = seriesConfirmed,
                stampRallies = entryWithStampRallies.stampRallies,
            )

            this@ArtistDetailsViewModel.entry = entry

            // Booth changes, so input route may not have booth, re-fetch using correct year's booth
            if (!hasImages) {
                catalogImages = AlleyDataUtils.getImages(
                    year = route.year,
                    folder = AlleyDataUtils.Folder.CATALOGS,
                    file = artist.booth,
                )
            }

            seriesImages = seriesImagesStore.getImages(entry.seriesInferred + entry.seriesConfirmed)
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            otherYears = (DataYear.entries - year)
                .filter { artistEntryDao.getEntry(it, id) != null }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            userNotesDao.getArtistNotes(id, year)?.notes
                ?.let(userNotes::setTextAndPlaceCursorAtEnd)
            snapshotFlow { userNotes.text }
                .drop(1)
                .debounce(500.milliseconds)
                .collectLatest {
                    userNotesDao.updateArtistNotes(id, year, it.toString())
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
