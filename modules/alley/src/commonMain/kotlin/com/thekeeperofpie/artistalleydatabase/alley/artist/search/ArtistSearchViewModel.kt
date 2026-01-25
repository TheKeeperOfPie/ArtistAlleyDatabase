package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Named
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
class ArtistSearchViewModel(
    private val artistEntryDao: ArtistEntryDao,
    dispatchers: CustomDispatchers,
    private val merchEntryDao: MerchEntryDao,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesEntryDao: SeriesEntryDao,
    private val seriesImagesStore: SeriesImagesStore,
    private val userEntryDao: UserEntryDao,
    val settings: ArtistAlleySettings,
    @Assisted val lockedYear: DataYear?,
    @Assisted @Named("lockedSeries") lockedSeries: String?,
    @Assisted @Named("lockedMerch") val lockedMerch: String?,
    @Assisted isRoot: Boolean,
    @Assisted @Named("lockedSerializedBooths") lockedSerializedBooths: String?,
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

    val results = combine(
        year,
        sortFilterController.state.filterParams.mapLatest {
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
                            showOutdatedCatalogs = searchQuery.filterParams.showOutdatedCatalogs,
                            fallbackCatalog = artistEntryDao.getFallbackImages(it.artist),
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
            // TODO: Add 2026 support
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

    fun toggleFavorite(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }

    fun toggleIgnored(entry: ArtistEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(ignored = ignored))
    }

    @AssistedFactory
    interface Factory {
        fun create(
            lockedYear: DataYear?,
            @Named("lockedSeries") lockedSeries: String?,
            @Named("lockedMerch") lockedMerch: String?,
            isRoot: Boolean,
            @Named("lockedSerializedBooths") lockedSerializedBooths: String?,
            savedStateHandle: SavedStateHandle,
        ): ArtistSearchViewModel
    }
}
