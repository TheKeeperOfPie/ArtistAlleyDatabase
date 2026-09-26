package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.StampRallyDetails
import com.thekeeperofpie.artistalleydatabase.alley.AlleyNavStack
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class StampRallySearchViewModel(
    dispatchers: CustomDispatchers,
    seriesEntryDao: SeriesEntryDao,
    private val seriesImageLoader: SeriesImageLoader,
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val userEntryDao: UserEntryDao,
    settings: ArtistAlleySettings,
    stampRallySortFilterControllerFactory: StampRallySortFilterController.Factory,
    private val navStack: AlleyNavStack,
    @Assisted val lockedYear: DataYear?,
    @Assisted lockedSeries: String?,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val displayType = settings.displayType
    val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<StampRallyUserEntry>(5, 5)

    val dataYear = if (lockedYear != null) {
        MutableStateFlow(lockedYear)
    } else {
        savedStateHandle.getMutableStateFlow("dataYear", settings.dataYear.value)
    }

    val lockedSeriesEntry = flowOf(lockedSeries)
        .flatMapLatest {
            if (it == null) flowOf(null) else seriesEntryDao.getSeriesById(it)
        }
        .flowOn(dispatchers.io)
        .stateInForCompose(this, null)

    val sortFilterController = stampRallySortFilterControllerFactory.create(
        scope = viewModelScope,
        lockedSeriesEntry = lockedSeriesEntry,
        dataYear = dataYear,
        savedStateHandle = savedStateHandle,
    )

    val seriesAutocompleteResults get() = sortFilterController.seriesAutocompleteResults

    val query = savedStateHandle.getMutableStateFlow("query", "")

    val unfilteredCount = combine(dataYear, query, ::Pair)
        .flatMapLatest { (year, query) ->
            stampRallyEntryDao.searchCount(
                year = year,
                query = query,
                searchQuery = StampRallySearchQuery(
                    filterParams = StampRallyFilterParams.unfiltered(lockedSeries),
                    randomSeed = randomSeed,
                ),
            )
        }
        .stateInForCompose(0)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertStampRallyUserEntry(it)
            }
        }
    }

    val results = combine(
        dataYear,
        snapshotFlow { sortFilterController.filterParams }.mapLatest {
            StampRallySearchQuery(
                filterParams = it,
                randomSeed = randomSeed,
            )
        },
        query,
        ::SearchParams
    )
        .flatMapLatest { (year, searchQuery, query) ->
            Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                stampRallyEntryDao.searchPagingSource(
                    year = year,
                    query = query,
                    searchQuery = searchQuery,
                )
            }.flow
                .map {
                    it.filterOnIO {
                        val passesFavorite = !it.userEntry.favorite || !searchQuery.filterParams.hideFavorited
                        val passesIgnore = !it.userEntry.ignored || !searchQuery.filterParams.hideIgnored
                        passesFavorite && passesIgnore
                    }
                }
        }
        .flowOn(CustomDispatchers.IO)
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())

    fun seriesImage(info: SeriesInfo) = seriesImageLoader.getSeriesImage(info)
    fun seriesImage(info: SeriesImageInfo) = seriesImageLoader.getSeriesImage(info)

    fun onEvent(event: StampRallySearchScreen.Event) = when (event) {
        is StampRallySearchScreen.Event.FavoriteToggle ->
            mutationUpdates.tryEmit(event.stampRally.userEntry.copy(favorite = event.favorite))
        is StampRallySearchScreen.Event.IgnoreToggle ->
            mutationUpdates.tryEmit(event.stampRally.userEntry.copy(ignored = event.ignored))
        is StampRallySearchScreen.Event.OpenEntry ->
            navStack.navigate(
                StampRallyDetails(
                    entry = event.stampRally.stampRally,
                    initialImageIndex = event.imageIndex
                )
            )
        is StampRallySearchScreen.Event.OpenImageFullscreen ->
            navStack.navigate(
                AlleyDestination.Images.fromStampRally(
                    stampRallyWithUserData = event.stampRally,
                    imageIndex = event.imageIndex,
                )
            )
        is StampRallySearchScreen.Event.ClearFilters -> sortFilterController.clear()
        StampRallySearchScreen.Event.Back -> navStack.onBack()
        StampRallySearchScreen.Event.OpenChangelog -> navStack.navigate(AlleyDestination.StampRallyChangelog(dataYear.value))
        StampRallySearchScreen.Event.OpenSettings -> navStack.navigate(AlleyDestination.Settings)
    }

    private data class SearchParams(
        val year: DataYear,
        val searchQuery: StampRallySearchQuery,
        val query: String,
    )

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            lockedYear: DataYear?,
            lockedSeries: String?,
            savedStateHandle: SavedStateHandle,
        ): StampRallySearchViewModel
    }
}
