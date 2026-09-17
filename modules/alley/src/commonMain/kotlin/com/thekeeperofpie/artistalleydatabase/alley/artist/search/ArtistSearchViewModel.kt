package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.thekeeperofpie.artistalleydatabase.alley.AlleyNavStack
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchCache
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class ArtistSearchViewModel(
    private val artistEntryDao: ArtistEntryDao,
    dispatchers: CustomDispatchers,
    private val merchCache: MerchCache,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesEntryDao: SeriesEntryDao,
    private val seriesImageLoader: SeriesImageLoader,
    private val userEntryDao: UserEntryDao,
    val settings: ArtistAlleySettings,
    private val navStack: AlleyNavStack,
    @Assisted isRoot: Boolean,
    @Assisted val lockedYear: DataYear?,
    @Assisted lockedSeries: String?,
    @Assisted val lockedMerch: String?,
    @Assisted lockedSerializedBooths: String?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val lockedBooths = lockedSerializedBooths?.let { serializedBooths ->
        val booths = mutableSetOf<String>()
        var currentLetter = serializedBooths.first()
        var firstNumber: Char? = null
        serializedBooths.forEach {
            if (it.isLetter()) {
                currentLetter = it
            } else if (firstNumber == null) {
                firstNumber = it
            } else {
                booths += "$currentLetter$firstNumber$it"
                firstNumber = null
            }
        }
        booths
    }.orEmpty()

    val year = if (lockedYear != null) {
        MutableStateFlow(lockedYear)
    } else if (isRoot) {
        settings.dataYear
    } else {
        savedStateHandle.getMutableStateFlow("dataYear", settings.dataYear.value)
    }

    val searchState = SearchScreen.State(
        columns = ArtistSearchColumn.entries,
        displayType = settings.displayType,
        showGridByDefault = settings.showGridByDefault,
        showRandomCatalogImage = settings.showRandomCatalogImage,
        forceOneDisplayColumn = settings.forceOneDisplayColumn,
    )

    val lockedSeriesEntry = flowOf(lockedSeries)
        .flatMapLatest {
            if (it == null) flowOf(null) else seriesEntryDao.getSeriesById(it)
        }
        .flowOn(dispatchers.io)
        .stateInForCompose(this, null)

    val sortFilterController = ArtistSortFilterController2(
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        dataYear = year,
        lockedMerchId = lockedMerch,
        lockedSeriesEntry = lockedSeriesEntry,
        settings = settings,
        merchCache = merchCache,
        allowSettingsBasedToggles = lockedMerch == null && lockedSeries == null,
    )

    val displayType = settings.displayType
    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    val query = MutableStateFlow("")

    val unfilteredCount = combine(lockedSeriesEntry, year, query, ::Triple)
        .flatMapLatest { (seriesInfo, year, query) ->
            artistEntryDao.searchCount(
                year = year,
                query = query,
                searchQuery = ArtistSearchQuery(
                    ArtistSortFilterController.FilterParams(
                        sortOption = ArtistSearchSortOption.BOOTH,
                        sortAscending = true,
                        seriesIn = setOfNotNull(seriesInfo?.rowid),
                        merchIn = setOfNotNull(lockedMerch),
                        commissionsIn = emptySet(),
                        linkTypesIn = emptySet(),
                        exhibitorTagsIn = emptySet(),
                        artistTagsIn = emptySet(),
                        artistTagsNotIn = emptySet(),
                        showOnlyConfirmedTags = false,
                        showOutdatedCatalogs = false,
                        hideFavorited = false,
                        hideIgnored = false,
                    ),
                    randomSeed = randomSeed,
                ),
                lockedBooths = lockedBooths,
            )
        }
        .stateInForCompose(0)

    private val filterParams by transform(viewModelScope) {
        val persistentState = sortFilterController.state.persistentState
        val saveableState = sortFilterController.state.saveableState
        val sortOption by persistentState.sortOption.collectAsState()
        val sortAscending by persistentState.sortAscending.collectAsState()
        val seriesIn = setOfNotNull(lockedSeriesEntry.collectAsState().value?.rowid) +
                saveableState.series.seriesIn.toSet().map { it.rowid }
        val artistTagsIn by persistentState.artistTagsIn.collectAsState()
        val artistTagsNotIn by persistentState.artistTagsNotIn.collectAsState()
        val showOnlyConfirmedTags by persistentState.showOnlyConfirmedTags.collectAsState()
        val showOutdatedCatalogs by persistentState.showOutdatedCatalogs.collectAsState()
        ArtistSortFilterController.FilterParams(
            sortOption = sortOption,
            sortAscending = sortAscending,
            seriesIn = seriesIn,
            merchIn = saveableState.merch.tags.tagIdIn.toSet() + setOfNotNull(lockedMerch),
            commissionsIn = saveableState.commissions.filterIn.toSet(),
            linkTypesIn = saveableState.links.links.tagIdIn.toSet().map(Link.Type::valueOf).toSet(),
            exhibitorTagsIn = emptySet(), // TODO
            artistTagsIn = artistTagsIn,
            artistTagsNotIn = artistTagsNotIn,
            showOnlyConfirmedTags = showOnlyConfirmedTags,
            showOutdatedCatalogs = showOutdatedCatalogs,
            hideFavorited = saveableState.hideFavorited,
            hideIgnored = saveableState.hideIgnored,
        )
    }

    val results = combine(
        year,
        snapshotFlow { filterParams }.mapLatest {
            ArtistSearchQuery(filterParams = it, randomSeed = randomSeed)
        },
        query,
        ::SearchParams
    )
        .flatMapLatest { (year, searchQuery, query) ->
            Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                artistEntryDao.searchPagingSource(
                    year = year,
                    query = query,
                    searchQuery = searchQuery,
                    lockedBooths = lockedBooths,
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
                        ArtistEntryGridModel.buildFromEntry(
                            randomSeed = randomSeed,
                            showOnlyConfirmedTags = searchQuery.filterParams.showOnlyConfirmedTags,
                            entry = it,
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
            when (it) {
                DataYear.ANIME_EXPO_2023,
                DataYear.ANIME_EXPO_2024,
                DataYear.ANIME_NYC_2024,
                DataYear.ANIME_NYC_2025,
                    -> false
                else -> seriesEntryDao.hasRallies(it, lockedSeries)
            }
        }
            .flowOn(dispatchers.io)
            .stateInForCompose(this, false)
    }

    val seriesAutocompleteResults =
        snapshotFlow { sortFilterController.state.saveableState.series.query.text.toString() }
            .debounce(500.milliseconds)
            .mapLatest(seriesEntryDao::searchSeriesForAutocomplete)
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun seriesImage(seriesInfo: SeriesInfo) = seriesImageLoader.getSeriesImage(seriesInfo)

    fun toggleFavorite(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }

    fun toggleIgnored(entry: ArtistEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(ignored = ignored))
    }

    fun onEvent(event: ArtistSearchScreen.Event) {
        when (event) {
            ArtistSearchScreen.Event.Back -> navStack.onBack()
            ArtistSearchScreen.Event.ClearFilters -> sortFilterController.clear()
            is ArtistSearchScreen.Event.FavoriteToggle ->
                toggleFavorite(event.entry, event.favorite)
            is ArtistSearchScreen.Event.IgnoreToggle ->
                toggleIgnored(event.entry, event.ignored)
            ArtistSearchScreen.Event.OpenChangelog ->
                navStack.navigate(AlleyDestination.ArtistChangelog(year.value))
            is ArtistSearchScreen.Event.OpenEntry ->
                navStack.navigate(ArtistDetails(event.entry.artist, event.imageIndex))
            ArtistSearchScreen.Event.OpenExport ->
                navStack.navigate(AlleyDestination.Export(year.value))
            is ArtistSearchScreen.Event.OpenImageFullscreen ->
                navStack.navigate(
                    AlleyDestination.Images.fromArtist(
                        artistWithUserData = event.entry.data,
                        showOutdatedCatalogs =
                            sortFilterController.state.persistentState.showOutdatedCatalogs.value,
                        imageIndex = event.imageIndex,
                    )
                )
            is ArtistSearchScreen.Event.OpenMerch ->
                navStack.navigate(Merch(year.value, event.merch))
            is ArtistSearchScreen.Event.OpenSeries ->
                navStack.navigate(Series(year.value, event.series))
            ArtistSearchScreen.Event.OpenSettings -> navStack.navigate(AlleyDestination.Settings)
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            isRoot: Boolean = false,
            lockedYear: DataYear? = null,
            lockedSeries: String? = null,
            lockedMerch: String? = null,
            lockedSerializedBooths: String? = null,
            savedStateHandle: SavedStateHandle,
        ): ArtistSearchViewModel
    }
}
