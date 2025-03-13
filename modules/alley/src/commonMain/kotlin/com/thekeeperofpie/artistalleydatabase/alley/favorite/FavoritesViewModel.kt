package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.paging.filter
import app.cash.paging.map
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
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class FavoritesViewModel(
    artistEntryDao: ArtistEntryDao,
    stampRallyEntryDao: StampRallyEntryDao,
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
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

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
}
