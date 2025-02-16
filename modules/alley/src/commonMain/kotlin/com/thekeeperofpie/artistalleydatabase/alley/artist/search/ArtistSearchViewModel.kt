package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.paging.filter
import app.cash.paging.map
import com.hoc081098.flowext.defer
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
class ArtistSearchViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val userEntryDao: UserEntryDao,
    private val settings: ArtistAlleySettings,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val filterParams: StateFlow<ArtistSortFilterViewModel.FilterParams>,
) : EntrySearchViewModel<ArtistSearchQuery, ArtistEntryGridModel>() {

    @Serializable
    data class InternalRoute(
        val year: DataYear? = null,
        val series: String? = null,
        val merch: String? = null,
    )

    private val route = savedStateHandle.toDestination<InternalRoute>(navigationTypeMap)

    val year = if (route.year == null) {
        settings.dataYear
    } else {
        ReadOnlyStateFlow(route.year)
    }

    val dataYear = settings.dataYear
    val lockedYear = route.year
    val lockedSeries = route.series
    val lockedMerch = route.merch

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
                        ArtistEntryGridModel.buildFromEntry(
                            randomSeed = randomSeed,
                            showOnlyConfirmedTags = showOnlyConfirmedTags,
                            entry = it,
                        )
                    }
                }
        }
        .flowOn(CustomDispatchers.IO)
        .cachedIn(viewModelScope)

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }

    fun onIgnoredToggle(entry: ArtistEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(ignored = ignored))
    }

    fun onDisplayTypeToggle(displayType: SearchScreen.DisplayType) {
        settings.displayType.value = displayType.name
    }
}
