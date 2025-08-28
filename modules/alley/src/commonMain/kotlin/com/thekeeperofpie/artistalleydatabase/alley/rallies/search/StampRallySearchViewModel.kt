package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.hoc081098.flowext.defer
import com.thekeeperofpie.artistalleydatabase.alley.Destinations.StampRallyDetails
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class StampRallySearchViewModel(
    dispatchers: CustomDispatchers,
    navigationTypeMap: NavigationTypeMap,
    seriesEntryDao: SeriesEntryDao,
    seriesImagesStore: SeriesImagesStore,
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val userEntryDao: UserEntryDao,
    private val settings: ArtistAlleySettings,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : EntrySearchViewModel<StampRallySearchQuery, StampRallyEntryGridModel>() {

    @Serializable
    data class InternalRoute(
        val year: DataYear? = null,
        val series: String? = null,
    )

    private val route = savedStateHandle.toDestination<InternalRoute>(navigationTypeMap)

    override val sections = emptyList<EntrySection>()

    val displayType = settings.displayType
    val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<StampRallyUserEntry>(5, 5)

    val dataYear = settings.dataYear

    val lockedYear = route.year
    val lockedSeries = route.series

    val searchState = SearchScreen.State(
        columns = StampRallySearchScreen.StampRallyColumn.entries,
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

    val sortFilterController = StampRallySortFilterController(
        scope = viewModelScope,
        lockedSeriesEntry = lockedSeriesEntry,
        dispatchers = dispatchers,
        seriesEntryDao = seriesEntryDao,
        seriesImagesStore = seriesImagesStore,
        settings = settings,
        savedStateHandle = savedStateHandle,
        allowHideFavorited = true,
    )

    val unfilteredCount = combine(dataYear, query, ::Pair)
        .flatMapLatest { (year, query) ->
            stampRallyEntryDao.searchCount(
                year = year,
                query = query,
                searchQuery = StampRallySearchQuery(
                    filterParams = StampRallySortFilterController.FilterParams(
                        sortOption = StampRallySearchSortOption.MAIN_TABLE,
                        sortAscending = true,
                        seriesIn = emptySet(),
                        totalCost = RangeData(100),
                        prizeLimit = RangeData(50),
                        showUnconfirmed = false,
                        hideFavorited = false,
                        hideIgnored = false,
                    ),
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

    override fun searchOptions() = defer {
        sortFilterController.state.filterParams.mapLatest {
            StampRallySearchQuery(
                filterParams = it,
                randomSeed = randomSeed,
            )
        }
    }

    override fun mapQuery(
        query: String,
        options: StampRallySearchQuery,
    ) = settings.dataYear
        .flatMapLatest {
            Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                stampRallyEntryDao.searchPagingSource(
                    year = it,
                    query = query,
                    searchQuery = options
                )
            }.flow
        }
        .flowOn(CustomDispatchers.IO)
        .map {
            it.filterOnIO {
                val passesFavorite = !it.userEntry.favorite || !options.filterParams.hideFavorited
                val passesIgnore = !it.userEntry.ignored || !options.filterParams.hideIgnored
                passesFavorite && passesIgnore
            }
        }
        .map { it.mapOnIO { StampRallyEntryGridModel.buildFromEntry(it) } }
        .cachedIn(viewModelScope)

    fun onEvent(navigationController: NavigationController, event: StampRallySearchScreen.Event) =
        when (event) {
            is StampRallySearchScreen.Event.SearchEvent -> when (val searchEvent = event.event) {
                is SearchScreen.Event.FavoriteToggle<StampRallyEntryGridModel> ->
                    mutationUpdates.tryEmit(searchEvent.entry.userEntry.copy(favorite = searchEvent.favorite))
                is SearchScreen.Event.IgnoreToggle<StampRallyEntryGridModel> ->
                    mutationUpdates.tryEmit(searchEvent.entry.userEntry.copy(ignored = searchEvent.ignored))
                is SearchScreen.Event.OpenEntry<StampRallyEntryGridModel> ->
                    navigationController.navigate(
                        StampRallyDetails(
                            entry = searchEvent.entry.stampRally,
                            initialImageIndex = searchEvent.imageIndex.toString(),
                        )
                    )
                is SearchScreen.Event.ClearFilters<*> -> sortFilterController.clear()
            }
        }
}
