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
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(SavedStateHandleSaveableApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@AssistedInject
class StampRallyDetailsViewModel(
    private val dispatchers: CustomDispatchers,
    private val seriesEntryDao: SeriesEntryDao,
    private val seriesImagesStore: SeriesImagesStore,
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val userNotesDao: UserNotesDao,
    private val userEntryDao: UserEntryDao,
    @Assisted route: AlleyDestination.StampRallyDetails,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val year = route.year
    val id = route.id
    val initialImageIndex = route.initialImageIndex?.toIntOrNull() ?: 0

    internal val images = flow {
        if (route.images.isNullOrEmpty()) {
            val entry = stampRallyEntryDao.getEntry(route.year, route.id)
            emit(
                LoadingResult.success(
                    entry?.stampRally?.images
                        ?.let { AlleyImageUtils.getRallyImages(year = route.year, images = it) }
                        .orEmpty()
                )
            )
        }
    }.stateInForCompose(
        if (route.images == null) {
            LoadingResult.loading()
        } else {
            LoadingResult.success(AlleyImageUtils.getRallyImages(route.year, route.images))
        }
    )

    val entry = flowFromSuspend {
        val entryWithArtists =
            stampRallyEntryDao.getEntryWithArtists(year, id) ?: return@flowFromSuspend null
        val stampRallyWithUserData = entryWithArtists.stampRally
        val stampRally = stampRallyWithUserData.stampRally
        val artists = entryWithArtists.artists.sortedBy { it.booth }

        // Some stamp rallies have artists in non-AA regions, try and show those
        val otherTables = stampRally.tables
            .filter { table ->
                artists.none { artist ->
                    artist.booth == table.substringBefore("-").trim()
                }
            }

        Entry(
            stampRally = stampRally,
            userEntry = stampRallyWithUserData.userEntry,
            artists = artists,
            otherTables = otherTables,
        )
    }.flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val series = entry.filterNotNull()
        .flatMapLatest { seriesEntryDao.observeSeriesByIdsWithUserData(it.stampRally.series) }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userNotes by savedStateHandle.saveable(stateSaver = TextFieldState.Saver.Fixed) {
        mutableStateOf(TextFieldState())
    }

    val seriesImages = series.filterNotNull()
        .flatMapLatest {
            flow {
                val series = it.map { it.series.toImageInfo() }
                val seriesImagesCacheResult = seriesImagesStore.getCachedImages(series)
                emit(seriesImagesCacheResult.seriesIdsToImages)
                emit(seriesImagesStore.getAllImages(series, seriesImagesCacheResult))
            }
        }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val mutationUpdates = MutableSharedFlow<SeriesUserEntry>(5, 5)

    init {
        viewModelScope.launch(dispatchers.io) {
            userNotesDao.getStampRallyNotes(id)?.notes
                ?.let(userNotes::setTextAndPlaceCursorAtEnd)
            snapshotFlow { userNotes.text }
                .drop(1)
                .debounce(500.milliseconds)
                .collectLatest {
                    userNotesDao.updateStampRallyNotes(id, it.toString())
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
            userEntryDao.insertStampRallyUserEntry(entry.userEntry.copy(favorite = favorite))
        }
    }

    fun onSeriesFavoriteToggle(data: SeriesWithUserData, favorite: Boolean) {
        mutationUpdates.tryEmit(data.userEntry.copy(favorite = favorite))
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

    @AssistedFactory
    interface Factory {
        fun create(
            route: AlleyDestination.StampRallyDetails,
            savedStateHandle: SavedStateHandle,
        ): StampRallyDetailsViewModel
    }
}
