package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.hoc081098.flowext.defer
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val userEntryDao: UserEntryDao,
    private val settings: ArtistAlleySettings,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val filterParams: StateFlow<StampRallySortFilterViewModel.FilterParams>,
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
        .mapNotNull {
            it ?: return@mapNotNull null
            withContext(dispatchers.io) {
                seriesEntryDao.getSeriesById(it)
            }
        }
        .stateInForCompose(this, null)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertStampRallyUserEntry(it)
            }
        }
    }

    override fun searchOptions() = defer {
        filterParams.mapLatest {
            StampRallySearchQuery(
                series = route.series,
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
            createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                stampRallyEntryDao.search(year = it, query = query, searchQuery = options)
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

    fun onEvent(navigationController: NavigationController, event: StampRallySearchScreen.Event) = when (event) {
        is StampRallySearchScreen.Event.SearchEvent -> when (val searchEvent = event.event) {
            is SearchScreen.Event.FavoriteToggle<StampRallyEntryGridModel> ->
                mutationUpdates.tryEmit(searchEvent.entry.userEntry.copy(favorite = searchEvent.favorite))
            is SearchScreen.Event.IgnoreToggle<StampRallyEntryGridModel> ->
                mutationUpdates.tryEmit(searchEvent.entry.userEntry.copy(ignored = searchEvent.ignored))
            is SearchScreen.Event.OpenEntry<StampRallyEntryGridModel> ->
                navigationController.navigate(
                    Destinations.StampRallyDetails(
                        entry = searchEvent.entry.stampRally,
                        initialImageIndex = searchEvent.imageIndex.toString(),
                    )
                )
        }
    }
}
