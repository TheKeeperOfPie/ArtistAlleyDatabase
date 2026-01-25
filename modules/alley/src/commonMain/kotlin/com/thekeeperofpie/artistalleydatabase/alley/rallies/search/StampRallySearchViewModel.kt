package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.hoc081098.flowext.defer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.StampRallyDetails
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
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
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
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class StampRallySearchViewModel(
    dispatchers: CustomDispatchers,
    seriesEntryDao: SeriesEntryDao,
    seriesImagesStore: SeriesImagesStore,
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val userEntryDao: UserEntryDao,
    private val settings: ArtistAlleySettings,
    @Assisted lockedYear: DataYear?,
    @Assisted lockedSeries: String?,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : EntrySearchViewModel<StampRallySearchQuery, StampRallyEntryGridModel>() {
    override val sections = emptyList<EntrySection>()

    val displayType = settings.displayType
    val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<StampRallyUserEntry>(5, 5)

    val dataYear = if (lockedYear != null) {
        MutableStateFlow(lockedYear)
    } else {
        savedStateHandle.getMutableStateFlow("dataYear", settings.dataYear.value)
    }

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
    ) = dataYear
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

    fun toggleFavorite(entry: StampRallyEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }

    fun toggleIgnored(entry: StampRallyEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(ignored = ignored))
    }

    @AssistedFactory
    interface Factory {
        fun create(
            lockedYear: DataYear?,
            lockedSeries: String?,
            savedStateHandle: SavedStateHandle,
        ): StampRallySearchViewModel
    }
}
