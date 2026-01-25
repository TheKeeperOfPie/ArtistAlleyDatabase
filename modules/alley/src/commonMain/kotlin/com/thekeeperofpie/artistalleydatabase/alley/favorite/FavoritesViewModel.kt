package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.ArtistDetails
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Merch
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Series
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.StampRallyDetails
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesFilterOption
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class FavoritesViewModel(
    artistEntryDao: ArtistEntryDao,
    stampRallyEntryDao: StampRallyEntryDao,
    merchEntryDao: MerchEntryDao,
    val seriesEntryCache: SeriesEntryCache,
    seriesEntryDao: SeriesEntryDao,
    seriesImagesStore: SeriesImagesStore,
    userEntryDao: UserEntryDao,
    settings: ArtistAlleySettings,
    dispatchers: CustomDispatchers,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val year = settings.dataYear

    val artistSortFilterController = ArtistSortFilterController(
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        dataYear = year,
        lockedMerchId = null,
        lockedSeriesEntry = ReadOnlyStateFlow(null),
        dispatchers = dispatchers,
        settings = settings,
        merchEntryDao = merchEntryDao,
        seriesEntryDao = seriesEntryDao,
        seriesImagesStore = seriesImagesStore,
        allowHideFavorited = false,
    )

    val stampRallySortFilterController = StampRallySortFilterController(
        scope = viewModelScope,
        lockedSeriesEntry = ReadOnlyStateFlow(null),
        dispatchers = dispatchers,
        seriesEntryDao = seriesEntryDao,
        seriesImagesStore = seriesImagesStore,
        settings = settings,
        savedStateHandle = savedStateHandle,
        allowHideFavorited = false,
    )

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

    val tab = MutableStateFlow(FavoritesScreen.EntryTab.ARTISTS)
    val query = MutableStateFlow("")
    val displayType = settings.displayType
    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    private val inputs = combineStates(query, year, settings.showOnlyConfirmedTags, ::Triple)

    val artistsUnfilteredCount = combine(year, query, ::Pair)
        .flatMapLatest { (year, query) ->
            artistEntryDao.searchCount(
                year = year,
                query = query,
                searchQuery = ArtistSearchQuery(
                    ArtistSortFilterController.FilterParams(
                        sortOption = ArtistSearchSortOption.BOOTH,
                        sortAscending = true,
                        seriesIn = emptySet(),
                        merchIn = emptySet(),
                        commissionsIn = emptySet(),
                        linkTypesIn = emptySet(),
                        exhibitorTagsIn = emptySet(),
                        showOnlyWithCatalog = false,
                        showOnlyConfirmedTags = false,
                        showOutdatedCatalogs = false,
                        hideFavorited = false,
                        hideIgnored = false,
                    ),
                    randomSeed = randomSeed,
                ),
                onlyFavorites = true,
            )
        }
        .stateInForCompose(0)

    val artistEntries = combine(
        inputs,
        artistSortFilterController.state.filterParams,
        ::Pair,
    ).flatMapLatest { (inputs, filterParams) ->
        val (query, year, showOnlyConfirmedTags) = inputs
        Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
            artistEntryDao.searchPagingSource(
                year = year,
                query = query,
                searchQuery = ArtistSearchQuery(filterParams, randomSeed),
                onlyFavorites = true,
            )
        }.flow
            .map { it.filterOnIO { !it.userEntry.ignored || !filterParams.hideIgnored } }
            .map {
                it.mapOnIO {
                    ArtistEntryGridModel.buildFromEntry(
                        randomSeed = randomSeed,
                        showOnlyConfirmedTags = showOnlyConfirmedTags,
                        entry = it,
                        showOutdatedCatalogs = filterParams.showOutdatedCatalogs,
                        fallbackCatalog = artistEntryDao.getFallbackImages(it.artist),
                    )
                }
            }
    }
        .flowOn(dispatchers.io)
        .cachedIn(viewModelScope)

    val stampRallyUnfilteredCount = combine(year, query, ::Pair)
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
                onlyFavorites = true,
            )
        }
        .stateInForCompose(0)

    val stampRallyEntries =
        combine(inputs, stampRallySortFilterController.state.filterParams, ::Pair)
            .flatMapLatest { (inputs, filterParams) ->
                val (query, year, showOnlyConfirmedTags) = inputs
                Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                    stampRallyEntryDao.searchPagingSource(
                        year = year,
                        query = query,
                        searchQuery = StampRallySearchQuery(filterParams, randomSeed),
                        onlyFavorites = true,
                    )
                }.flow.map { it.filterOnIO { !it.userEntry.ignored || !filterParams.hideIgnored } }
            }
            .map { it.mapOnIO { StampRallyEntryGridModel.buildFromEntry(it) } }
            .flowOn(dispatchers.io)
            .cachedIn(viewModelScope)

    val seriesSortFilterController =
        SeriesSortFilterController(viewModelScope, settings, savedStateHandle)

    data class SeriesInputs(
        val query: String,
        val year: DataYear,
        val languageOption: AniListLanguageOption,
        val filterParams: SeriesSortFilterController.FilterParams,
    )

    val seriesEntries = combine(
        query,
        year,
        settings.languageOption,
        seriesSortFilterController.state.filterParams,
        ::SeriesInputs,
    )
        .flatMapLatest { (query, year, languageOption, filterParams) ->
            if (year == DataYear.ANIME_EXPO_2023) {
                flowOf(PagingData.empty())
            } else {
                Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                    val seriesFilterState = listOf(SeriesFilterOption.ALL to true)
                    seriesEntryDao.searchSeries(
                        languageOption = languageOption,
                        year = year,
                        query = query,
                        randomSeed = randomSeed,
                        seriesFilterParams = filterParams,
                        favoriteOnly = true,
                    )
                }.flow
            }
        }
        .enforceUniqueIds { it.series.id }
        .flowOn(dispatchers.io)
        .cachedIn(viewModelScope)

    val merchEntries = combine(query, year, ::Pair)
        .flatMapLatest { (query, year) ->
            if (year == DataYear.ANIME_EXPO_2023) {
                flowOf(PagingData.empty())
            } else {
                Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                    if (query.isBlank()) {
                        merchEntryDao.getMerch(year, favoriteOnly = true)
                    } else {
                        merchEntryDao.searchMerch(year, query, favoriteOnly = true)
                    }
                }.flow
            }
        }
        .enforceUniqueIds { it.merch.name }
        .flowOn(dispatchers.io)
        .cachedIn(viewModelScope)

    private val artistMutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)
    private val rallyMutationUpdates = MutableSharedFlow<StampRallyUserEntry>(5, 5)
    private val seriesMutationUpdates = MutableSharedFlow<SeriesUserEntry>(5, 5)
    private val merchMutationUpdates = MutableSharedFlow<MerchUserEntry>(5, 5)

    private val seriesImageLoader =
        SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

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
        viewModelScope.launch(dispatchers.io) {
            seriesMutationUpdates.collectLatest {
                userEntryDao.insertSeriesUserEntry(it)
            }
        }

        viewModelScope.launch(dispatchers.io) {
            merchMutationUpdates.collectLatest {
                userEntryDao.insertMerchUserEntry(it)
            }
        }
    }

    fun getSeriesImage(series: SeriesInfo) = seriesImageLoader.getSeriesImage(series)

    fun onEvent(
        navigationController: NavigationController,
        event: FavoritesScreen.Event,
        onNavigateToArtists: () -> Unit,
        onNavigateToRallies: () -> Unit,
        onNavigateToSeries: () -> Unit,
        onNavigateToMerch: () -> Unit,
    ) = when (event) {
        is FavoritesScreen.Event.OpenMerch ->
            navigationController.navigate(Merch(merch = event.merch))
        is FavoritesScreen.Event.OpenSeries ->
            navigationController.navigate(Series(series = event.series))
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
                    ArtistDetails(
                        searchEvent.entry.artist,
                        searchEvent.imageIndex,
                    )
                )
                is StampRallyEntryGridModel -> navigationController.navigate(
                    StampRallyDetails(
                        entry = searchEvent.entry.stampRally,
                        initialImageIndex = searchEvent.imageIndex.toString(),
                    )
                )
                else -> throw IllegalArgumentException(
                    "Entry model not supported: ${searchEvent.entry}"
                )
            }
            is SearchScreen.Event.ClearFilters<*> -> when (tab.value) {
                FavoritesScreen.EntryTab.ARTISTS ->
                    artistSortFilterController.clear()
                FavoritesScreen.EntryTab.RALLIES ->
                    stampRallySortFilterController.clear()
                FavoritesScreen.EntryTab.SERIES ->
                    seriesSortFilterController.state.sections.value.forEach { it.clear() }
                FavoritesScreen.EntryTab.MERCH -> Unit
            }
        }
        FavoritesScreen.Event.NavigateToArtists -> onNavigateToArtists()
        FavoritesScreen.Event.NavigateToRallies -> onNavigateToRallies()
        FavoritesScreen.Event.NavigateToSeries -> onNavigateToSeries()
        FavoritesScreen.Event.NavigateToMerch -> onNavigateToMerch()
        is FavoritesScreen.Event.SeriesFavoriteToggle ->
            seriesMutationUpdates.tryEmit(event.series.userEntry.copy(favorite = event.favorite))
        is FavoritesScreen.Event.MerchFavoriteToggle ->
            merchMutationUpdates.tryEmit(event.merch.userEntry.copy(favorite = event.favorite))
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): FavoritesViewModel
    }
}
