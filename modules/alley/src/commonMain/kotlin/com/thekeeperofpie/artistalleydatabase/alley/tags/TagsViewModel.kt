package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_settings_series_language
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, SavedStateHandleSaveableApi::class, FlowPreview::class)
@AssistedInject
class TagsViewModel(
    dispatchers: CustomDispatchers,
    merchEntryDao: MerchEntryDao,
    seriesEntryDao: SeriesEntryDao,
    settings: ArtistAlleySettings,
    seriesImagesStore: SeriesImagesStore,
    userEntryDao: UserEntryDao,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    val seriesQuery = MutableStateFlow("")

    val seriesLanguageSection = SettingsSection.Dropdown(
        labelTextRes = Res.string.alley_settings_series_language,
        options = AniListLanguageOption.entries,
        optionToText = { stringResource(it.textWithExplanation) },
        property = settings.languageOption,
    )

    val seriesSortFilterController =
        SeriesSortFilterController(viewModelScope, settings, savedStateHandle)

    val series =
        combine(
            settings.dataYear,
            settings.languageOption,
            seriesQuery.debounce(250.milliseconds),
            seriesSortFilterController.state.filterParams,
            ::SeriesInputs
        )
            .flatMapLatest { (year, languageOption, query, filterParams) ->
                if (year == DataYear.ANIME_EXPO_2023) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(PagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                        seriesEntryDao.searchSeries(
                            languageOption = languageOption,
                            year = year,
                            query = query,
                            randomSeed = randomSeed,
                            seriesFilterParams = filterParams,
                        )
                    }.flow
                }
            }
            .enforceUniqueIds { it.series.id }
            .cachedIn(viewModelScope)

    var merchQuery by mutableStateOf("")

    val merch = combine(settings.dataYear, snapshotFlow { merchQuery }, ::Pair)
        .flatMapLatest { (year, query) ->
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

    val dataYear = settings.dataYear

    private val seriesImageLoader =
        SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    private val seriesMutationUpdates = MutableSharedFlow<SeriesUserEntry>(5, 5)
    private val merchMutationUpdates = MutableSharedFlow<MerchUserEntry>(5, 5)

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

    fun getSeriesImage(series: SeriesInfo) = seriesImageLoader.getSeriesImage(series)

    fun onSeriesFavoriteToggle(data: SeriesWithUserData, favorite: Boolean) {
        seriesMutationUpdates.tryEmit(data.userEntry.copy(favorite = favorite))
    }

    fun onMerchFavoriteToggle(data: MerchWithUserData, favorite: Boolean) {
        merchMutationUpdates.tryEmit(data.userEntry.copy(favorite = favorite))
    }

    private data class SeriesInputs(
        val dataYear: DataYear,
        val languageOption: AniListLanguageOption,
        val seriesQuery: String,
        val seriesFilterParams: SeriesSortFilterController.FilterParams,
    )

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): TagsViewModel
    }
}
