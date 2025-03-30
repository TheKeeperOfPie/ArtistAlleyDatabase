package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.paging.filter
import app.cash.paging.map
import com.hoc081098.flowext.defer
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
class ArtistSearchViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val tagEntryDao: TagEntryDao,
    private val userEntryDao: UserEntryDao,
    val settings: ArtistAlleySettings,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val filterParams: StateFlow<ArtistSortFilterViewModel.FilterParams>,
) : EntrySearchViewModel<ArtistSearchQuery, ArtistEntryGridModel>() {

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
        // TODO: Connect to SavedStateHandle
        MutableStateFlow(settings.dataYear.value)
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
        .mapNotNull {
            it ?: return@mapNotNull null
            withContext(CustomDispatchers.IO) {
                tagEntryDao.getSeriesById(it)
            }
        }
        .stateInForCompose(null)

    override val sections = emptyList<EntrySection>()

    val displayType = settings.displayType
    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertArtistUserEntry(it)
            }
        }
    }

    // TODO: Remove defer?
    override fun searchOptions() = defer {
        filterParams.mapLatest {
            ArtistSearchQuery(
                filterParams = it,
                randomSeed = randomSeed,
                lockedSeries = lockedSeries,
                lockedMerch = lockedMerch,
            )
        }
    }

    override fun mapQuery(
        query: String,
        options: ArtistSearchQuery,
    ) = combine(year, settings.showOnlyConfirmedTags, ::Pair)
        .flatMapLatest { (year, showOnlyConfirmedTags) ->
            createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                artistEntryDao.search(
                    year = year,
                    query = query,
                    searchQuery = options
                )
            }.flow
                .map { it.filter { !it.userEntry.ignored || options.filterParams.showIgnored } }
                .map {
                    it.map {
                        val series = ArtistEntryGridModel.getSeries(
                            showOnlyConfirmedTags = showOnlyConfirmedTags,
                            entry = it,
                            tagEntryDao = tagEntryDao,
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
        .flowOn(CustomDispatchers.IO)
        .cachedIn(viewModelScope)

    fun onEvent(navigationController: NavigationController, event: ArtistSearchScreen.Event) =
        when (event) {
            is ArtistSearchScreen.Event.SearchEvent -> when (val searchEvent = event.event) {
                is SearchScreen.Event.FavoriteToggle<ArtistEntryGridModel> ->
                    mutationUpdates.tryEmit(searchEvent.entry.userEntry.copy(favorite = searchEvent.favorite))
                is SearchScreen.Event.IgnoreToggle<ArtistEntryGridModel> ->
                    mutationUpdates.tryEmit(searchEvent.entry.userEntry.copy(ignored = searchEvent.ignored))
                is SearchScreen.Event.OpenEntry<ArtistEntryGridModel> ->
                    navigationController.navigate(
                        Destinations.ArtistDetails(
                            year = searchEvent.entry.artist.year,
                            id = searchEvent.entry.id.valueId,
                            imageIndex = searchEvent.imageIndex.toString(),
                        )
                    )
            }
            is ArtistSearchScreen.Event.OpenMerch ->
                navigationController.navigate(Destinations.Merch(lockedYear, event.merch))
            is ArtistSearchScreen.Event.OpenSeries ->
                navigationController.navigate(Destinations.Series(lockedYear, event.series))
        }
}
