package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.ArtistDetails
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Merch
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Series
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.StampRallyDetails
import com.thekeeperofpie.artistalleydatabase.alley.AlleyNavStack
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterParams
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallyFilterParams
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
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
    userEntryDao: UserEntryDao,
    settings: ArtistAlleySettings,
    dispatchers: CustomDispatchers,
    artistSortFilterControllerFactory: ArtistSortFilterController.Factory,
    stampRallySortFilterControllerFactory: StampRallySortFilterController.Factory,
    private val navStack: AlleyNavStack,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val year = settings.dataYear
    
    val showOutdatedCatalogs = settings.showOutdatedCatalogs

    val artistSortFilterController = artistSortFilterControllerFactory.create(
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        dataYear = year,
        lockedMerchId = null,
        lockedSeriesEntry = ReadOnlyStateFlow(null),
    )

    val seriesAutocompleteResults get() = artistSortFilterController.seriesAutocompleteResults

    val stampRallySortFilterController = stampRallySortFilterControllerFactory.create(
        scope = viewModelScope,
        lockedSeriesEntry = ReadOnlyStateFlow(null),
        dataYear = year,
        savedStateHandle = savedStateHandle,
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
                    ArtistSortFilterParams.unfiltered(),
                    randomSeed = randomSeed,
                ),
                onlyFavorites = true,
            )
        }
        .stateInForCompose(0)

    val artistEntries = combine(
        inputs,
        snapshotFlow { artistSortFilterController.filterParams },
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
                    filterParams = StampRallyFilterParams.unfiltered(),
                    randomSeed = randomSeed,
                ),
                onlyFavorites = true,
            )
        }
        .stateInForCompose(0)

    val stampRallyEntries =
        combine(inputs, snapshotFlow { stampRallySortFilterController.filterParams }, ::Pair)
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

    fun onEvent(
        event: FavoritesScreen.Event,
        onNavigateToArtists: () -> Unit,
        onNavigateToRallies: () -> Unit,
        onNavigateToSeries: () -> Unit,
        onNavigateToMerch: () -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenExport: (DataYear) -> Unit,
        onOpenFavoriteArtistsChangelog: (DataYear) -> Unit,
        onOpenFavoriteStampRalliesChangelog: (DataYear) -> Unit,
        onOpenFavoriteSeriesChangelog: (DataYear) -> Unit,
        onOpenFavoriteMerchChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
    ) = when (event) {
        is FavoritesScreen.Event.OpenMerch -> onOpenMerch(year.value, event.merch)
        is FavoritesScreen.Event.OpenSeries -> onOpenSeries(year.value, event.series)
        FavoritesScreen.Event.NavigateToArtists -> onNavigateToArtists()
        FavoritesScreen.Event.NavigateToRallies -> onNavigateToRallies()
        FavoritesScreen.Event.NavigateToSeries -> onNavigateToSeries()
        FavoritesScreen.Event.NavigateToMerch -> onNavigateToMerch()
        is FavoritesScreen.Event.SeriesFavoriteToggle ->
            seriesMutationUpdates.tryEmit(event.series.userEntry.copy(favorite = event.favorite))
        is FavoritesScreen.Event.MerchFavoriteToggle ->
            merchMutationUpdates.tryEmit(event.merch.userEntry.copy(favorite = event.favorite))
        FavoritesScreen.Event.OpenChangelog -> {
            val year = year.value
            when (tab.value) {
                FavoritesScreen.EntryTab.ARTISTS -> onOpenFavoriteArtistsChangelog(year)
                FavoritesScreen.EntryTab.RALLIES -> onOpenFavoriteStampRalliesChangelog(year)
                FavoritesScreen.EntryTab.SERIES -> onOpenFavoriteSeriesChangelog(year)
                FavoritesScreen.EntryTab.MERCH -> onOpenFavoriteMerchChangelog(year)
            }
        }
        is FavoritesScreen.Event.OpenExport -> onOpenExport(event.dataYear)
        FavoritesScreen.Event.OpenSettings -> onOpenSettings()
        is FavoritesScreen.Event.ArtistSearchEvent -> when (val searchEvent = event.event) {
            is ArtistSearchScreen.Event.FavoriteToggle -> artistMutationUpdates.tryEmit(
                searchEvent.entry.userEntry.copy(favorite = searchEvent.favorite)
            )
            is ArtistSearchScreen.Event.IgnoreToggle -> artistMutationUpdates.tryEmit(
                searchEvent.entry.userEntry.copy(ignored = searchEvent.ignored)
            )
            is ArtistSearchScreen.Event.ClearFilters -> artistSortFilterController.clear()
            ArtistSearchScreen.Event.Back -> navStack.onBack()
            ArtistSearchScreen.Event.OpenChangelog ->
                navStack.navigate(AlleyDestination.ArtistChangelog(year.value))
            is ArtistSearchScreen.Event.OpenEntry ->
                navStack.navigate(ArtistDetails(searchEvent.entry.artist, searchEvent.imageIndex))
            ArtistSearchScreen.Event.OpenExport ->
                navStack.navigate(AlleyDestination.Export(year.value))
            is ArtistSearchScreen.Event.OpenImageFullscreen ->
                navStack.navigate(
                    AlleyDestination.Images.fromArtist(
                        artistWithUserData = searchEvent.entry.data,
                        showOutdatedCatalogs =
                            artistSortFilterController.state.persistentState.showOutdatedCatalogs.value,
                        imageIndex = searchEvent.imageIndex,
                    )
                )
            is ArtistSearchScreen.Event.OpenMerch ->
                navStack.navigate(Merch(year.value, searchEvent.merch))
            is ArtistSearchScreen.Event.OpenSeries ->
                navStack.navigate(Series(year.value, searchEvent.series))
            ArtistSearchScreen.Event.OpenSettings -> navStack.navigate(AlleyDestination.Settings)
        }
        is FavoritesScreen.Event.StampRallySearchEvent -> when (val searchEvent = event.event) {
            is StampRallySearchScreen.Event.FavoriteToggle -> rallyMutationUpdates.tryEmit(
                searchEvent.stampRally.userEntry.copy(favorite = searchEvent.favorite)
            )
            is StampRallySearchScreen.Event.IgnoreToggle -> rallyMutationUpdates.tryEmit(
                searchEvent.stampRally.userEntry.copy(ignored = searchEvent.ignored)
            )
            is StampRallySearchScreen.Event.OpenEntry ->
                navStack.navigate(
                    StampRallyDetails(
                        searchEvent.stampRally.stampRally,
                        searchEvent.imageIndex
                    )
                )
            is StampRallySearchScreen.Event.OpenImageFullscreen ->
                navStack.navigate(
                    AlleyDestination.Images.fromStampRally(
                        stampRallyWithUserData = searchEvent.stampRally,
                        imageIndex = searchEvent.imageIndex,
                    )
                )
            is StampRallySearchScreen.Event.ClearFilters -> stampRallySortFilterController.clear()
            StampRallySearchScreen.Event.Back -> navStack.onBack()
            StampRallySearchScreen.Event.OpenChangelog ->
                navStack.navigate(AlleyDestination.StampRallyChangelog(year.value))
            StampRallySearchScreen.Event.OpenSettings -> navStack.navigate(AlleyDestination.Settings)
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(savedStateHandle: SavedStateHandle): FavoritesViewModel
    }
}
