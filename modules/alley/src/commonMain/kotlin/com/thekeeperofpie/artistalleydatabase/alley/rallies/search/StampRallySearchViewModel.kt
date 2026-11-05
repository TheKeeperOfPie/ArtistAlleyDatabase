package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.appstate.transform.transform
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavigator
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class StampRallySearchViewModel(
    seriesEntryDao: SeriesEntryDao,
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val userEntryDao: UserEntryDao,
    internal val settings: ArtistAlleySettings,
    stampRallySortFilterControllerFactory: StampRallySortFilterController.Factory,
    private val navigator: AlleyNavigator,
    @Assisted val isRoot: Boolean,
    @Assisted val lockedYear: DataYear?,
    @Assisted lockedSeries: String?,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<StampRallyUserEntry>(5, 5)

    private var localSavedYear by savedStateHandle.saveable { mutableStateOf(settings.dataYear) }
    var dataYear: DataYear
        get() = lockedYear ?: if (isRoot) {
            settings.dataYear
        } else {
            localSavedYear
        }
        set(value) {
            if (lockedYear == null) {
                if (isRoot) {
                    settings.dataYear = value
                } else {
                    localSavedYear = value
                }
            }
        }

    val lockedSeriesEntry by transform<SeriesInfo?>(null, viewModelScope) {
        produceState<SeriesInfo?>(null, lockedSeries) {
            if (lockedSeries != null) {
                seriesEntryDao.getSeriesById(lockedSeries)
                    .collectLatest { value = it }
            }
        }.value
    }

    val sortFilterController = stampRallySortFilterControllerFactory.create(
        scope = viewModelScope,
        lockedSeries = ::lockedSeriesEntry,
        dataYear = ::dataYear,
        savedStateHandle = savedStateHandle,
    )

    val query by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed) { TextFieldState() }

    val unfilteredCount by transform(0, viewModelScope) {
        val year = dataYear
        val query = query.text.toString()
        produceState(0, year, query) {
            stampRallyEntryDao.searchCount(
                year = year,
                searchQuery = StampRallySearchQuery(
                    query = query,
                    filterParams = StampRallyFilterParams.unfiltered(lockedSeries),
                    randomSeed = randomSeed,
                ),
            ).collectLatest { value = it }
        }.value
    }

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertStampRallyUserEntry(it)
            }
        }
    }

    val results = snapshotFlow {
        SearchParams(
            year = dataYear,
            searchQuery = StampRallySearchQuery(
                query = query.text.toString(),
                filterParams = sortFilterController.filterParams,
                randomSeed = randomSeed,
            ),
        )
    }.flatMapLatest { (year, searchQuery) ->
        Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
            stampRallyEntryDao.searchPagingSource(
                year = year,
                searchQuery = searchQuery,
            )
        }.flow
    }
        .flowOn(CustomDispatchers.IO)
        .cachedIn(viewModelScope)

    fun onEvent(event: StampRallySearchScreen.Event) {
        StampRallySearchScreenEventHandler.onEvent(
            navigator = navigator,
            sortFilterController = sortFilterController,
            year = ::dataYear,
            onMutationUpdate = mutationUpdates::tryEmit,
            event = event,
        )
    }

    private data class SearchParams(
        val year: DataYear,
        val searchQuery: StampRallySearchQuery,
    )

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            isRoot: Boolean,
            lockedYear: DataYear?,
            lockedSeries: String?,
            savedStateHandle: SavedStateHandle,
        ): StampRallySearchViewModel
    }
}
