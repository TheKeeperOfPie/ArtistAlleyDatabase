package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Merch
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Series
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavStack
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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
    private val navStack: AlleyNavStack,
    artistSortFilterControllerFactory: ArtistSortFilterController.Factory,
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

    val lockedSeriesEntry by transform(viewModelScope) {
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
        dataYear = year,
        lockedMerchId = lockedMerch,
        lockedSeries = ::lockedSeriesEntry,
        allowSettingsBasedToggles = lockedMerch == null && lockedSeries == null,
    )

    val seriesAutocompleteResults get() = sortFilterController.seriesAutocompleteResults

    val displayType = settings.displayType
    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    val query by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed) { TextFieldState() }

    val unfilteredCount by transform(viewModelScope) {
        val year by year.collectAsStateWithLifecycle()
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

    val results = combine(
        year,
        snapshotFlow {
            ArtistSearchQuery(
                query = query.text.toString(),
                filterParams = sortFilterController.filterParams,
                randomSeed = randomSeed,
            )
        },
        ::SearchParams
    )
        .flatMapLatest { (year, searchQuery) ->
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
                navStack.navigate(AlleyDestination.ArtistDetails(event.entry.artist, event.imageIndex))
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
