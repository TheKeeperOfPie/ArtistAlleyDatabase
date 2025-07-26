package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.thekeeperofpie.artistalleydatabase.alley.Destinations.ArtistDetails
import com.thekeeperofpie.artistalleydatabase.alley.Destinations.Merch
import com.thekeeperofpie.artistalleydatabase.alley.Destinations.Series
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
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
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ArtistSearchViewModel(
    private val artistEntryDao: ArtistEntryDao,
    dispatchers: CustomDispatchers,
    private val merchEntryDao: MerchEntryDao,
    private val seriesEntryCache: SeriesEntryCache,
    private val seriesEntryDao: SeriesEntryDao,
    private val seriesImagesStore: SeriesImagesStore,
    private val userEntryDao: UserEntryDao,
    val settings: ArtistAlleySettings,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Serializable
    data class InternalRoute(
        val year: DataYear? = null,
        val series: String? = null,
        val merch: String? = null,
        val isRoot: Boolean = false,
    )

    private val route = savedStateHandle.toDestination<InternalRoute>(navigationTypeMap)

    val year = if (route.isRoot) {
        settings.dataYear
    } else {
        savedStateHandle.getMutableStateFlow("dataYear", settings.dataYear.value)
    }

    val lockedYear = route.year
    val lockedSeries = route.series
    val lockedMerch = route.merch

    val searchState = SearchScreen.State(
        columns = ArtistSearchScreen.ArtistColumn.entries,
        displayType = settings.displayType,
        showGridByDefault = settings.showGridByDefault,
        showRandomCatalogImage = settings.showRandomCatalogImage,
        forceOneDisplayColumn = settings.forceOneDisplayColumn,
    )

    val lockedSeriesEntry = flowOf(lockedSeries)
        .flatMapLatest {
            if (it == null) flowOf(null) else seriesEntryDao.getSeriesById(it)
        }
        .mapLatest { it?.series }
        .flowOn(dispatchers.io)
        .stateInForCompose(this, null)

    val sortFilterController = ArtistSortFilterController(
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        dataYear = year,
        lockedMerchId = lockedMerch,
        lockedSeriesEntry = lockedSeriesEntry,
        dispatchers = dispatchers,
        settings = settings,
        merchEntryDao = merchEntryDao,
        seriesEntryDao = seriesEntryDao,
        seriesImagesStore = seriesImagesStore,
        allowSettingsBasedToggles = lockedMerch == null && lockedSeries == null,
    )

    val displayType = settings.displayType
    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    val query = MutableStateFlow("")

    val unfilteredCount = combine(year, query, ::Pair)
        .flatMapLatest { (year, query) ->
            artistEntryDao.searchCount(
                year = year,
                query = query,
                searchQuery = ArtistSearchQuery(
                    ArtistSortFilterController.FilterParams(
                        sortOption = ArtistSearchSortOption.BOOTH,
                        sortAscending = true,
                        seriesIn = setOfNotNull(lockedSeries),
                        merchIn = setOfNotNull(lockedMerch),
                        commissionsIn = emptySet(),
                        linkTypesIn = emptySet(),
                        exhibitorTagsIn = emptySet(),
                        showOnlyWithCatalog = false,
                        showOnlyConfirmedTags = false,
                        hideFavorited = false,
                        hideIgnored = false,
                    ),
                    randomSeed = randomSeed,
                ),
            )
        }
        .stateInForCompose(0)

    val results = combine(
        year,
        sortFilterController.state.filterParams.mapLatest {
            ArtistSearchQuery(filterParams = it, randomSeed = randomSeed)
        },
        query,
        ::SearchParams
    )
        .flatMapLatest { (year, searchQuery, query) ->
            createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                artistEntryDao.searchPagingSource(
                    year = year,
                    query = query,
                    searchQuery = searchQuery,
                )
            }.flow
                .map {
                    it.filterOnIO {
                        val passesFavorite =
                            !it.userEntry.favorite || !searchQuery.filterParams.hideFavorited
                        val passesIgnore =
                            !it.userEntry.ignored || !searchQuery.filterParams.hideIgnored
                        passesFavorite && passesIgnore
                    }
                }
                .map {
                    it.mapOnIO {
                        val (series, hasMoreSeries) = ArtistEntryGridModel.getSeriesAndHasMore(
                            randomSeed = randomSeed,
                            showOnlyConfirmedTags = searchQuery.filterParams.showOnlyConfirmedTags,
                            entry = it,
                            seriesEntryCache = seriesEntryCache,
                        )
                        ArtistEntryGridModel.buildFromEntry(
                            randomSeed = randomSeed,
                            showOnlyConfirmedTags = searchQuery.filterParams.showOnlyConfirmedTags,
                            entry = it,
                            series = series,
                            hasMoreSeries = hasMoreSeries,
                        )
                    }
                }
        }
        .flowOn(dispatchers.io)
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())

    val hasRallies = if (lockedSeries == null) {
        ReadOnlyStateFlow(false)
    } else {
        year.mapLatest {
            if (it == DataYear.ANIME_EXPO_2025) {
                seriesEntryDao.hasRallies(lockedSeries)
            } else {
                false
            }
        }
            .flowOn(dispatchers.io)
            .stateInForCompose(this, false)
    }

    init {
        viewModelScope.launch(dispatchers.io) {
            mutationUpdates.collectLatest {
                userEntryDao.insertArtistUserEntry(it)
            }
        }
    }

    private data class SearchParams(
        val year: DataYear,
        val searchQuery: ArtistSearchQuery,
        val query: String,
    )

    fun onEvent(navigationController: NavigationController, event: ArtistSearchScreen.Event) =
        when (event) {
            is ArtistSearchScreen.Event.SearchEvent -> when (val searchEvent = event.event) {
                is SearchScreen.Event.FavoriteToggle<ArtistEntryGridModel> ->
                    mutationUpdates.tryEmit(searchEvent.entry.userEntry.copy(favorite = searchEvent.favorite))
                is SearchScreen.Event.IgnoreToggle<ArtistEntryGridModel> ->
                    mutationUpdates.tryEmit(searchEvent.entry.userEntry.copy(ignored = searchEvent.ignored))
                is SearchScreen.Event.OpenEntry<ArtistEntryGridModel> ->
                    navigationController.navigate(
                        ArtistDetails(
                            searchEvent.entry.artist,
                            searchEvent.imageIndex,
                        )
                    )
                is SearchScreen.Event.ClearFilters<*> -> sortFilterController.clear()
            }
            is ArtistSearchScreen.Event.OpenMerch ->
                navigationController.navigate(Merch(lockedYear, event.merch))
            is ArtistSearchScreen.Event.OpenSeries ->
                navigationController.navigate(Series(lockedYear, event.series))
        }
}
