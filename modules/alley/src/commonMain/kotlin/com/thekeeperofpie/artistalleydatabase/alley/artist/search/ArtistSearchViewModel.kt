package com.thekeeperofpie.artistalleydatabase.alley.artist.search

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
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavigator
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class ArtistSearchViewModel(
    private val artistEntryDao: ArtistEntryDao,
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesEntryDao: SeriesEntryDao,
    private val userEntryDao: UserEntryDao,
    val settings: ArtistAlleySettings,
    private val navigator: AlleyNavigator,
    artistSortFilterControllerFactory: ArtistSortFilterController.Factory,
    @Assisted private val isRoot: Boolean,
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

    private var localSavedYear by savedStateHandle.saveable { mutableStateOf(settings.dataYear) }
    var year: DataYear
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

    val sortFilterController = artistSortFilterControllerFactory.create(
        scope = viewModelScope,
        savedStateHandle = savedStateHandle,
        dataYear = ::year,
        lockedMerchId = lockedMerch,
        lockedSeries = ::lockedSeriesEntry,
        allowSettingsBasedToggles = lockedMerch == null && lockedSeries == null,
    )

    val seriesAutocompleteResults get() = sortFilterController.seriesAutocompleteResults

    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    val query by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed) { TextFieldState() }

    val unfilteredCount by transform(0, viewModelScope) {
        val year = year
        val query = query.text.toString()
        val lockedSeriesEntry = lockedSeriesEntry
        produceState(0, year, query) {
            artistEntryDao.searchCount(
                year = year,
                searchQuery = ArtistSearchQuery(
                    query = "",
                    ArtistSortFilterParams.unfiltered(
                        lockedSeriesIn = lockedSeriesEntry?.rowid,
                        lockedMerchIn = lockedMerch,
                    ),
                    randomSeed = randomSeed,
                ),
                lockedBooths = lockedBooths,
            ).collectLatest { value = it }
        }.value
    }

    val results = snapshotFlow {
        SearchParams(
            year = year,
            searchQuery = ArtistSearchQuery(
                query = query.text.toString(),
                filterParams = sortFilterController.filterParams,
                randomSeed = randomSeed,
            ),
        )
    }.flatMapLatest { (year, searchQuery) ->
        Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
            artistEntryDao.searchPagingSource(
                year = year,
                searchQuery = searchQuery,
                lockedBooths = lockedBooths,
            )
        }.flow
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

    val hasRallies by transform(false, viewModelScope) {
        val year = year
        produceState(false, year) {
            if (lockedSeries != null) {
                when (year) {
                    DataYear.ANIME_EXPO_2023,
                    DataYear.ANIME_EXPO_2024,
                    DataYear.ANIME_NYC_2024,
                    DataYear.ANIME_NYC_2025
                        -> Unit
                    else -> value = seriesEntryDao.hasRallies(year, lockedSeries)
                }
            }
        }.value
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
    )

    fun onEvent(event: ArtistSearchScreen.Event) {
        ArtistSearchScreenEventHandler.onEvent(
            navigator = navigator,
            sortFilterController = sortFilterController,
            year = ::year,
            onMutationUpdate = mutationUpdates::tryEmit,
            event = event,
        )
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
