package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.paging.filter
import app.cash.paging.map
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class FavoritesViewModel(
    artistEntryDao: ArtistEntryDao,
    stampRallyEntryDao: StampRallyEntryDao,
    seriesEntryDao: SeriesEntryDao,
    userEntryDao: UserEntryDao,
    settings: ArtistAlleySettings,
    dispatchers: CustomDispatchers,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val artistFilterParams: StateFlow<ArtistSortFilterViewModel.FilterParams>,
    @Assisted private val stampRallyFilterParams: StateFlow<StampRallySortFilterViewModel.FilterParams>,
) : ViewModel() {

    val year = settings.dataYear

    val artistSearchState = SearchScreen.State(
        columns = ArtistSearchScreen.ArtistColumn.entries,
        displayType = settings.displayType,
        showGridByDefault = settings.showGridByDefault,
        showRandomCatalogImage = settings.showRandomCatalogImage,
        forceOneDisplayColumn = settings.forceOneDisplayColumn,
    )

    val stampRallySearchState = SearchScreen.State(
        columns = StampRallySearchScreen.StampRallyColumn.entries,
        displayType = settings.displayType,
        showGridByDefault = settings.showGridByDefault,
        showRandomCatalogImage = settings.showRandomCatalogImage,
        forceOneDisplayColumn = settings.forceOneDisplayColumn,
    )

    val query = MutableStateFlow("")
    val displayType = settings.displayType
    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    private val inputs = combineStates(query, year, settings.showOnlyConfirmedTags, ::Triple)
    val artistEntries = inputs.flatMapLatest { (query, year, showOnlyConfirmedTags) ->
        artistFilterParams.flatMapLatest { filterParams ->
            createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                artistEntryDao.search(
                    year = year,
                    query = query,
                    searchQuery = ArtistSearchQuery(filterParams, randomSeed),
                    onlyFavorites = true,
                )
            }.flow.map { it.filter { !it.userEntry.ignored || filterParams.showIgnored } }
        }
            .map {
                it.map {
                    val series = ArtistEntryGridModel.getSeries(
                        showOnlyConfirmedTags = showOnlyConfirmedTags,
                        entry = it,
                        seriesEntryDao = seriesEntryDao,
                    )
                    ArtistEntryGridModel.buildFromEntry(
                        randomSeed = randomSeed,
                        showOnlyConfirmedTags = showOnlyConfirmedTags,
                        entry = it,
                        series = series,
                    )
                }
            }
    }
        .flowOn(dispatchers.io)
        .cachedIn(viewModelScope)

    val stampRallyEntries = inputs.flatMapLatest { (query, year, showOnlyConfirmedTags) ->
        stampRallyFilterParams.flatMapLatest { filterParams ->
            createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                stampRallyEntryDao.search(
                    year = year,
                    query = query,
                    searchQuery = StampRallySearchQuery(filterParams, randomSeed),
                    onlyFavorites = true,
                )
            }.flow.map { it.filter { !it.userEntry.ignored || filterParams.showIgnored } }
        }
            .map { it.map(StampRallyEntryGridModel::buildFromEntry) }
    }
        .flowOn(dispatchers.io)
        .cachedIn(viewModelScope)

    private val artistMutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)
    private val rallyMutationUpdates = MutableSharedFlow<StampRallyUserEntry>(5, 5)

    init {
        viewModelScope.launch(dispatchers.io) {
            artistMutationUpdates.collectLatest {
                userEntryDao.insertArtistUserEntry(it)
            }
        }
        viewModelScope.launch(dispatchers.io) {
            rallyMutationUpdates.collectLatest {
                userEntryDao.insertStampRallyUserEntry(it)
            }
        }
    }

    fun onEvent(
        navigationController: NavigationController,
        event: FavoritesScreen.Event,
    ) = when (event) {
        is FavoritesScreen.Event.OpenMerch ->
            navigationController.navigate(Destinations.Merch(merch = event.merch))
        is FavoritesScreen.Event.OpenSeries ->
            navigationController.navigate(Destinations.Series(series = event.series))
        is FavoritesScreen.Event.SearchEvent -> when (val searchEvent = event.event) {
            is SearchScreen.Event.FavoriteToggle<*> -> when (searchEvent.entry) {
                is ArtistEntryGridModel -> artistMutationUpdates.tryEmit(
                    searchEvent.entry.userEntry.copy(favorite = searchEvent.favorite)
                )
                is StampRallyEntryGridModel -> rallyMutationUpdates.tryEmit(
                    searchEvent.entry.userEntry.copy(favorite = searchEvent.favorite)
                )
                else -> throw IllegalArgumentException(
                    "Entry model not supported: ${searchEvent.entry}"
                )
            }
            is SearchScreen.Event.IgnoreToggle<*> -> when (searchEvent.entry) {
                is ArtistEntryGridModel -> artistMutationUpdates.tryEmit(
                    searchEvent.entry.userEntry.copy(ignored = searchEvent.ignored)
                )
                is StampRallyEntryGridModel -> rallyMutationUpdates.tryEmit(
                    searchEvent.entry.userEntry.copy(ignored = searchEvent.ignored)
                )
                else -> throw IllegalArgumentException(
                    "Entry model not supported: ${searchEvent.entry}"
                )
            }
            is SearchScreen.Event.OpenEntry<*> -> when (searchEvent.entry) {
                is ArtistEntryGridModel -> navigationController.navigate(
                    Destinations.ArtistDetails(
                        searchEvent.entry.artist,
                        searchEvent.imageIndex,
                    )
                )
                is StampRallyEntryGridModel -> navigationController.navigate(
                    Destinations.StampRallyDetails(
                        entry = searchEvent.entry.stampRally,
                        initialImageIndex = searchEvent.imageIndex.toString(),
                    )
                )
                else -> throw IllegalArgumentException(
                    "Entry model not supported: ${searchEvent.entry}"
                )
            }
        }
    }
}
