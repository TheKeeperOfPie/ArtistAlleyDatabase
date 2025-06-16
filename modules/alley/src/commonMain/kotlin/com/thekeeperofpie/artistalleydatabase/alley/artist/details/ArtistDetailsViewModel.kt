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
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(SavedStateHandleSaveableApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@Inject
class ArtistDetailsViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val dispatchers: CustomDispatchers,
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
        AlleyDataUtils.getArtistImages(
            year = route.year,
            booth = route.booth,
            name = route.name,
        )
    )

    val entry = flowFromSuspend {
        val entryWithStampRallies = artistEntryDao.getEntryWithStampRallies(year, id)
            ?: return@flowFromSuspend null
        val artistWithUserData = entryWithStampRallies.artist
        val artist = artistWithUserData.artist

        Entry(
            artist = artist,
            userEntry = artistWithUserData.userEntry,
            stampRallies = entryWithStampRallies.stampRallies,
        )
    }.flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val seriesInferred = entry
        .filterNotNull()
        .flatMapLatest { seriesEntryDao.observeSeriesByIds(it.artist.seriesInferred) }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val seriesConfirmed = entry
        .filterNotNull()
        .flatMapLatest { seriesEntryDao.observeSeriesByIds(it.artist.seriesConfirmed) }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var otherYears by mutableStateOf(listOf<DataYear>())
        private set

    val seriesImages =
        combine(seriesInferred.filterNotNull(), seriesConfirmed.filterNotNull(), ::Pair)
            .flatMapLatest { (seriesInferred, seriesConfirmed) ->
                flow {
                    val series = (seriesInferred + seriesConfirmed).map { it.series }
                    val seriesImagesCacheResult = seriesImagesStore.getCachedImages(series)
                    emit(seriesImagesCacheResult.seriesIdsToImages)
                    emit(seriesImagesStore.getAllImages(series, seriesImagesCacheResult))
                }
            }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val userNotes by savedStateHandle.saveable(stateSaver = TextFieldState.Saver) {
        mutableStateOf(TextFieldState())
    }

    private val mutationUpdates = MutableSharedFlow<SeriesUserEntry>(5, 5)

    init {
        val hasImages = catalogImages.isNotEmpty()
        // Booth changes, so input route may not have booth, re-fetch using correct year's booth
        if (!hasImages) {
            viewModelScope.launch(dispatchers.io) {
                val artist = entry.filterNotNull().first().artist
                catalogImages = AlleyDataUtils.getArtistImages(
                    year = route.year,
                    booth = artist.booth,
                    name = artist.name,
                )
            }
        }

        viewModelScope.launch(dispatchers.io) {
            otherYears = (DataYear.entries - year)
                .filter { artistEntryDao.getEntry(it, id) != null }
        }

        viewModelScope.launch(dispatchers.io) {
            userNotesDao.getArtistNotes(id, year)?.notes
                ?.let(userNotes::setTextAndPlaceCursorAtEnd)
            snapshotFlow { userNotes.text }
                .drop(1)
                .debounce(500.milliseconds)
                .collectLatest {
                    userNotesDao.updateArtistNotes(id, year, it.toString())
                }
        }

        viewModelScope.launch(dispatchers.io) {
            mutationUpdates.collectLatest {
                userEntryDao.insertSeriesUserEntry(it)
            }
        }
    }

    fun onFavoriteToggle(favorite: Boolean) {
        val entry = entry.value ?: return
        entry.favorite = favorite
        viewModelScope.launch(dispatchers.io) {
            userEntryDao.insertArtistUserEntry(entry.userEntry.copy(favorite = favorite))
        }
    }

    fun onSeriesFavoriteToggle(data: SeriesWithUserData, favorite: Boolean) {
        mutationUpdates.tryEmit(data.userEntry.copy(favorite = favorite))
    }

    @Stable
    class Entry(
        val artist: ArtistEntry,
        val userEntry: ArtistUserEntry,
        val stampRallies: List<StampRallyEntry>,
    ) {
        var favorite by mutableStateOf(userEntry.favorite)
    }
}
