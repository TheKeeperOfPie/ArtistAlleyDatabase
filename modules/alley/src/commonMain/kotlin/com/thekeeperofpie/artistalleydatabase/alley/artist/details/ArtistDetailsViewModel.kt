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
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.details.DetailsScreenCatalog
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
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
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(SavedStateHandleSaveableApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@AssistedInject
class ArtistDetailsViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val dispatchers: CustomDispatchers,
    private val userNotesDao: UserNotesDao,
    private val seriesImagesStore: SeriesImagesStore,
    private val seriesEntryDao: SeriesEntryDao,
    private val settings: ArtistAlleySettings,
    private val userEntryDao: UserEntryDao,
    @Assisted private val route: AlleyDestination.ArtistDetails,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val year = route.year
    val initialImageIndex = route.imageIndex ?: 0

    val requestedShowFallback = savedStateHandle.getMutableStateFlow("requestedShowFallback", false)
    val showFallbackImages =
        combineStates(requestedShowFallback, settings.showOutdatedCatalogs, Boolean::or)

    val entry = flowFromSuspend {
        val entryWithStampRallies = if (route.id == null) {
            if (route.booth == null) {
                null
            } else {
                artistEntryDao.getEntriesByBooth(year, route.booth).firstOrNull()?.id?.let {
                    artistEntryDao.getEntryWithStampRallies(year, it)
                }
            }
        } else {
            artistEntryDao.getEntryWithStampRallies(year, route.id)
        }
        entryWithStampRallies ?: return@flowFromSuspend null
        val artistWithUserData = entryWithStampRallies.artist
        val artist = artistWithUserData.artist

        Entry(
            artist = artist,
            userEntry = artistWithUserData.userEntry,
            stampRallies = entryWithStampRallies.stampRallies,
        )
    }.flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val catalog = combineStates(
        showFallbackImages,
        entry.mapState(viewModelScope) { it?.artist },
    ) { showFallbackImages, artist ->
        val year = artist?.year ?: route.year
        val images = (artist?.images ?: route.images).orEmpty()
        val fallbackImages = (artist?.fallbackImages ?: route.fallbackImages).orEmpty()
        val fallbackImageYear = artist?.fallbackImageYear ?: route.fallbackImageYear
        val tempImages = (artist?.tempImages ?: route.tempImages).orEmpty()
        val embeds = (artist?.embeds ?: route.embeds).orEmpty()
        when {
            images.isNotEmpty() || fallbackImageYear == null -> {
                LoadingResult.success(
                    DetailsScreenCatalog(
                        images = AlleyImageUtils.getArtistImagesWithEmbedFallback(
                            year = year,
                            images = images,
                            tempImages = tempImages,
                            embeds = embeds,
                        ),
                        showOutdatedCatalogs = null,
                        fallbackYear = null,
                    )
                )
            }
            !showFallbackImages && tempImages.isNotEmpty() ->
                LoadingResult.success(
                    DetailsScreenCatalog(
                        images = AlleyImageUtils.getTempImages(tempImages),
                        showOutdatedCatalogs = false,
                        fallbackYear = fallbackImageYear,
                    )
                )
            !showFallbackImages && embeds.isNotEmpty() ->
                LoadingResult.success(
                    DetailsScreenCatalog(
                        images = AlleyImageUtils.getEmbedImages(embeds),
                        showOutdatedCatalogs = false,
                        fallbackYear = fallbackImageYear,
                    )
                )
            !showFallbackImages ->
                LoadingResult.success(
                    DetailsScreenCatalog(
                        images = emptyList(),
                        showOutdatedCatalogs = false,
                        fallbackYear = fallbackImageYear,
                    )
                )
            else -> LoadingResult.success(
                DetailsScreenCatalog(
                    images = AlleyImageUtils.getArtistImages(fallbackImageYear, fallbackImages),
                    showOutdatedCatalogs = showFallbackImages,
                    fallbackYear = fallbackImageYear,
                )
            )
        }
    }

    val otherArtists = entry.mapNotNull { it?.artist?.booth?.ifBlank { null } }
        .mapLatest {
            val id = id()
            artistEntryDao.getEntriesByBooth(year, it).filter { it.id != id }
        }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val seriesInferred = entry
        .filterNotNull()
        .flatMapLatest { seriesEntryDao.observeSeriesByIdsWithUserData(it.artist.seriesInferred) }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val seriesConfirmed = entry
        .filterNotNull()
        .flatMapLatest { seriesEntryDao.observeSeriesByIdsWithUserData(it.artist.seriesConfirmed) }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var otherYears by mutableStateOf(listOf<DataYear>())
        private set

    val seriesImages =
        combine(seriesInferred.filterNotNull(), seriesConfirmed.filterNotNull(), ::Pair)
            .flatMapLatest { (seriesInferred, seriesConfirmed) ->
                flow {
                    val series = (seriesInferred + seriesConfirmed).map { it.series.toImageInfo() }
                    val seriesImagesCacheResult = seriesImagesStore.getCachedImages(series)
                    emit(seriesImagesCacheResult.seriesIdsToImages)
                    emit(seriesImagesStore.getAllImages(series, seriesImagesCacheResult))
                }
            }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val userNotes by savedStateHandle.saveable(stateSaver = TextFieldState.Saver.Fixed) {
        mutableStateOf(TextFieldState())
    }

    private val mutationUpdates = MutableSharedFlow<SeriesUserEntry>(5, 5)

    init {
        viewModelScope.launch(dispatchers.io) {
            otherYears = (DataYear.entries - year)
                .filter { artistEntryDao.getEntry(it, id()) != null }
        }

        viewModelScope.launch(dispatchers.io) {
            val id = id()
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

    private suspend fun id() = route.id ?: entry.filterNotNull().first().artist.id

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

    fun onShowFallback() {
        requestedShowFallback.value = true
    }

    fun onAlwaysShowFallback() {
        settings.showOutdatedCatalogs.value = true
    }

    @Stable
    class Entry(
        val artist: ArtistEntry,
        val userEntry: ArtistUserEntry,
        val stampRallies: List<StampRallyDatabaseEntry>,
    ) {
        var favorite by mutableStateOf(userEntry.favorite)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            route: AlleyDestination.ArtistDetails,
            savedStateHandle: SavedStateHandle,
        ): ArtistDetailsViewModel
    }
}
