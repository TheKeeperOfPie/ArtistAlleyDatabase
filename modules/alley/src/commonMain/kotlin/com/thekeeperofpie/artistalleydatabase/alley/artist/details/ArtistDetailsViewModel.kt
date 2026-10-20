package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.details.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.details.DetailsScreenCatalog
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavigator
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(SavedStateHandleSaveableApi::class, FlowPreview::class)
@AssistedInject
class ArtistDetailsViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val dispatchers: CustomDispatchers,
    private val userNotesDao: UserNotesDao,
    private val seriesImagesStore: SeriesImagesStore,
    private val seriesEntryDao: SeriesEntryDao,
    private val settings: ArtistAlleySettings,
    private val userEntryDao: UserEntryDao,
    private val navigator: AlleyNavigator,
    @Assisted private val route: AlleyDestination.ArtistDetails,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val year = route.year
    val initialImageIndex = route.imageIndex ?: 0

    var requestedShowFallback by savedStateHandle.saveable { mutableStateOf(false) }
    val showFallbackImages by derivedStateOf {
        requestedShowFallback or settings.showOutdatedCatalogs
    }

    val entry by transform(viewModelScope) {
        val entryWithStampRallies by produceState<ArtistWithStampRalliesEntry?>(null) {
            when {
                route.id == null && route.booth != null -> {
                    val artistId =
                        artistEntryDao.getEntriesByBooth(year, route.booth).firstOrNull()?.id
                    artistId?.let { artistEntryDao.getEntryWithStampRallies(year, it) }
                }
                route.id != null -> artistEntryDao.getEntryWithStampRallies(year, route.id)
                else -> null
            }?.collectLatest { value = it }
        }
        val (artistWithUserData, stampRallies) = entryWithStampRallies ?: return@transform null
        Entry(
            artistWithUserData = artistWithUserData,
            stampRallies = stampRallies,
        )
    }

    private val id = route.id ?: entry?.artist?.id

    val catalog by transform(viewModelScope) {
        val artist = entry?.artist
        val year = artist?.year ?: route.year
        val images = (artist?.images ?: route.images).orEmpty()
        val fallbackImages = (artist?.fallbackImages ?: route.fallbackImages).orEmpty()
        val fallbackImageYear = artist?.fallbackImageYear ?: route.fallbackImageYear
        val tempImages = (artist?.tempImages ?: route.tempImages).orEmpty()
        val embeds = (artist?.embeds ?: route.embeds).orEmpty()
        when {
            images.isNotEmpty() || fallbackImageYear == null -> {
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
            }
            !showFallbackImages && tempImages.isNotEmpty() ->
                DetailsScreenCatalog(
                    images = AlleyImageUtils.getTempImages(tempImages),
                    showOutdatedCatalogs = false,
                    fallbackYear = fallbackImageYear,
                )
            !showFallbackImages && embeds.isNotEmpty() ->
                DetailsScreenCatalog(
                    images = AlleyImageUtils.getEmbedImages(embeds),
                    showOutdatedCatalogs = false,
                    fallbackYear = fallbackImageYear,
                )
            !showFallbackImages ->
                DetailsScreenCatalog(
                    images = emptyList(),
                    showOutdatedCatalogs = false,
                    fallbackYear = fallbackImageYear,
                )
            else ->
                DetailsScreenCatalog(
                    images = AlleyImageUtils.getArtistImages(fallbackImageYear, fallbackImages),
                    showOutdatedCatalogs = showFallbackImages,
                    fallbackYear = fallbackImageYear,
                )
        }
    }

    val otherArtists by transform(viewModelScope) {
        val booth = entry?.artist?.booth?.ifBlank { null } ?: return@transform emptyList()
        val artistId = id
        produceState(emptyList()) {
            value = artistEntryDao.getEntriesByBooth(year, booth).filter { it.id != artistId }
        }.value
    }

    val seriesInferred by transform(viewModelScope) {
        val entry = entry ?: return@transform null
        produceState<List<SeriesWithUserData>?>(null, entry) {
            seriesEntryDao.observeSeriesByIdsWithUserData(entry.artist.seriesInferred)
                .collectLatest { value = it }
        }.value
    }

    val seriesConfirmed by transform(viewModelScope) {
        val entry = entry ?: return@transform null
        produceState<List<SeriesWithUserData>?>(null, entry) {
            seriesEntryDao.observeSeriesByIdsWithUserData(entry.artist.seriesConfirmed)
                .collectLatest { value = it }
        }.value
    }

    val otherYears by transform(viewModelScope) {
        val id = id ?: return@transform emptyList()
        produceState(emptyList()) {
            value = (DataYear.entries - year)
                .filter { artistEntryDao.getEntry(it, id).firstOrNull() != null }
        }.value
    }

    val seriesImages by transform(viewModelScope) {
        val seriesInferred = seriesInferred ?: return@transform emptyMap()
        val seriesConfirmed = seriesConfirmed ?: return@transform emptyMap()
        produceState(emptyMap(), seriesInferred, seriesConfirmed) {
            val series = (seriesInferred + seriesConfirmed).map { it.series.toImageInfo() }
            val seriesImagesCacheResult = seriesImagesStore.getCachedImages(series)
            value = seriesImagesCacheResult.seriesIdsToImages
            value = seriesImagesStore.getAllImages(series, seriesImagesCacheResult)
        }.value
    }

    val userNotes by savedStateHandle.saveable(stateSaver = TextFieldState.Saver.Fixed) {
        mutableStateOf(TextFieldState())
    }

    init {
        viewModelScope.launch(dispatchers.io) {
            val id = snapshotFlow { id }.filterNotNull().first()
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

    fun onEvent(event: ArtistDetailsScreen.Event) {
        when (event) {
            is ArtistDetailsScreen.Event.DetailsEvent ->
                when (val detailsEvent = event.event) {
                    is DetailsScreen.Event.FavoriteToggle ->
                        onFavoriteToggle(detailsEvent.favorite)
                    DetailsScreen.Event.NavigateUp -> navigator.goUp()
                    is DetailsScreen.Event.OpenImage -> {
                        val showingOutdatedCatalogs =
                            catalog.showOutdatedCatalogs == true
                        val artistWithUserData = entry?.artistWithUserData
                        if (artistWithUserData != null) {
                            navigator.navigate(
                                AlleyDestination.Images.fromArtist(
                                    artistWithUserData = artistWithUserData,
                                    showOutdatedCatalogs = showingOutdatedCatalogs,
                                    imageIndex = detailsEvent.imageIndex,
                                )
                            )
                        }
                    }
                    DetailsScreen.Event.OpenMap ->
                        entry?.artist?.id?.let {
                            navigator.navigate(AlleyDestination.ArtistMap(year, it))
                        }
                }
            ArtistDetailsScreen.Event.ShowFallback -> requestedShowFallback = true
            ArtistDetailsScreen.Event.AlwaysShowFallback -> settings.showOutdatedCatalogs = true
        }
    }

    private fun onFavoriteToggle(favorite: Boolean) {
        val entry = entry ?: return
        viewModelScope.launch(dispatchers.io) {
            userEntryDao.insertArtistUserEntry(entry.userEntry.copy(favorite = favorite))
        }
    }

    @Stable
    class Entry(
        val artistWithUserData: ArtistWithUserData,
        val stampRallies: List<StampRallyDatabaseEntry>,
    ) {
        val artist get() = artistWithUserData.artist
        val userEntry get() = artistWithUserData.userEntry
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            route: AlleyDestination.ArtistDetails,
            savedStateHandle: SavedStateHandle,
        ): ArtistDetailsViewModel
    }
}
