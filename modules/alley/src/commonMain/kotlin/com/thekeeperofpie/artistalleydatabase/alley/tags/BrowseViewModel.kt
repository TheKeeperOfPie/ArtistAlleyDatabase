package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Merch
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.MerchChangelog
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Series
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.SeriesChangelog
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.navigation.AlleyNavigator
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@AssistedInject
class BrowseViewModel(
    dispatchers: CustomDispatchers,
    merchEntryDao: MerchEntryDao,
    private val navigator: AlleyNavigator,
    seriesEntryDao: SeriesEntryDao,
    private val settings: ArtistAlleySettings,
    userEntryDao: UserEntryDao,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    val seriesQuery by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed) { TextFieldState() }

    val seriesSortFilterController = SeriesSortFilterController(settings, savedStateHandle)

    val series =
        combine(
            snapshotFlow {
                SeriesInputs(
                    dataYear = settings.dataYear,
                    languageOption = settings.languageOption,
                    seriesFilterParams = seriesSortFilterController.filterParams
                )
            },
            snapshotFlow { seriesQuery.text.toString() }
                .debounce(250.milliseconds),
            ::Pair
        )
            .flatMapLatest { [inputs, query] ->
                val (dataYear, languageOption, seriesFilterParams) = inputs
                if (dataYear == DataYear.ANIME_EXPO_2023) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                        seriesEntryDao.searchSeries(
                            languageOption = languageOption,
                            year = dataYear,
                            query = query,
                            randomSeed = randomSeed,
                            seriesFilterParams = seriesFilterParams,
                        )
                    }.flow
                }
            }
            .cachedIn(viewModelScope)

    val merchQuery by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed) { TextFieldState() }

    val merch = snapshotFlow { settings.dataYear to merchQuery.text.toString() }
        .flatMapLatest { [year, query] ->
            if (year == DataYear.ANIME_EXPO_2023) {
                flowOf(PagingData.empty())
            } else {
                Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                    if (query.isBlank()) {
                        merchEntryDao.getMerch(year)
                    } else {
                        merchEntryDao.searchMerch(year, query)
                    }
                }
                    .flow
            }
        }
        .enforceUniqueIds { it.merch.name }
        .cachedIn(viewModelScope)

    private val seriesMutationUpdates = MutableSharedFlow<SeriesUserEntry>(5, 5)
    private val merchMutationUpdates = MutableSharedFlow<MerchUserEntry>(5, 5)

    private var selectedTabIndex by savedStateHandle.saveable { mutableIntStateOf(0) }

    val state = BrowseScreen.State(
        dataYear = { settings.dataYear },
        onDataYearChange = { settings.dataYear = it },
        selectedTabIndex = ::selectedTabIndex,
        onSelectedTabIndexChange = { selectedTabIndex = it },
        seriesQuery = seriesQuery,
        merchQuery = merchQuery,
        languageOption = { settings.languageOption },
        onLanguageOptionChange = { settings.languageOption = it },
        seriesSortFilterState = seriesSortFilterController.state,
    )

    init {
        viewModelScope.launch(dispatchers.io) {
            seriesMutationUpdates.collectLatest {
                userEntryDao.insertSeriesUserEntry(it)
            }
        }

        viewModelScope.launch(dispatchers.io) {
            merchMutationUpdates.collectLatest {
                userEntryDao.insertMerchUserEntry(it)
            }
        }
    }

    fun onEvent(event: BrowseScreen.Event) = when (event) {
        is BrowseScreen.Event.OpenSeries ->
            navigator.navigate(Series(settings.dataYear, event.seriesInfo.id))
        is BrowseScreen.Event.OpenMerch ->
            navigator.navigate(Merch(settings.dataYear, event.merch.name))
        BrowseScreen.Event.OpenSeriesChangelog ->
            navigator.navigate(SeriesChangelog(settings.dataYear))
        BrowseScreen.Event.OpenMerchChangelog ->
            navigator.navigate(MerchChangelog(settings.dataYear))
        BrowseScreen.Event.OpenSettings -> navigator.navigate(AlleyDestination.Settings)
        is BrowseScreen.Event.ToggleSeriesFavorite ->
            seriesMutationUpdates.tryEmit(event.data.userEntry.copy(favorite = event.favorite))
        is BrowseScreen.Event.ToggleMerchFavorite ->
            merchMutationUpdates.tryEmit(event.data.userEntry.copy(favorite = event.favorite))
    }

    private data class SeriesInputs(
        val dataYear: DataYear,
        val languageOption: AniListLanguageOption,
        val seriesFilterParams: SeriesSortFilterController.FilterParams,
    )

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(savedStateHandle: SavedStateHandle): BrowseViewModel
    }
}
