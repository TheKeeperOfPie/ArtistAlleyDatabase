package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.hoc081098.flowext.defer
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArtistSearchViewModel @Inject constructor(
    private val application: Application,
    private val appFileSystem: AppFileSystem,
    private val artistEntryDao: ArtistEntryDao,
    private val settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : EntrySearchViewModel<ArtistSearchQuery, ArtistEntryGridModel>() {

    @Serializable
    data class InternalRoute(
        val series: String? = null,
        val merch: String? = null,
    )

    private val route = savedStateHandle.toDestination<InternalRoute>(navigationTypeMap)
    val lockedSeries = route.series
    val lockedMerch = route.merch

    val sortFilterController = ArtistSortFilterController(viewModelScope, settings)

    override val sections = emptyList<EntrySection>()

    val displayType = settings.displayType
    val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<ArtistEntry>(5, 5)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                artistEntryDao.insertEntries(it)
            }
        }
    }

    override fun searchOptions() = defer {
        sortFilterController.filterParams.mapLatest {
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
    ) = Pager(PagingConfig(pageSize = 20)) {
        trackPagingSource { artistEntryDao.search(query, options) }
    }.flow
        .flowOn(CustomDispatchers.IO)
        .map { it.filter { !it.ignored || options.filterParams.showIgnored } }
        .map { it.map { ArtistEntryGridModel.buildFromEntry(application, appFileSystem, it) } }
        .cachedIn(viewModelScope)

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.value.copy(favorite = favorite))
    }

    fun onIgnoredToggle(entry: ArtistEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.value.copy(ignored = ignored))
    }

    fun onDisplayTypeToggle(displayType: SearchScreen.DisplayType) {
        settings.displayType.value = displayType.name
    }
}
