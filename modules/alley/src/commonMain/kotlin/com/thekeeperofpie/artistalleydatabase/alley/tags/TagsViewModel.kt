package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_settings_series_language
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesFilterOption
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.settings.ui.SettingsSection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalCoroutinesApi::class, SavedStateHandleSaveableApi::class)
@Inject
class TagsViewModel(
    dispatchers: CustomDispatchers,
    seriesEntryDao: SeriesEntryDao,
    tagsEntryDao: TagEntryDao,
    settings: ArtistAlleySettings,
    seriesImagesStore: SeriesImagesStore,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val seriesLanguageSection = SettingsSection.Dropdown(
        labelTextRes = Res.string.alley_settings_series_language,
        options = AniListLanguageOption.entries,
        optionToText = { stringResource(it.textWithExplanation) },
        property = settings.languageOption,
    )

    val series =
        combine(
            settings.dataYear,
            settings.languageOption,
            snapshotFlow { seriesQuery to seriesFiltersState },
            ::Triple
        )
            .flatMapLatest { (year, languageOption, pair) ->
                val (query, seriesFilterState) = pair
                if (year == DataYear.YEAR_2023) {
                    flowOf(PagingData.empty())
                } else {
                    createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                        if (query.isBlank()) {
                            seriesEntryDao.getSeries(languageOption, year, seriesFilterState)
                        } else {
                            seriesEntryDao.searchSeries(languageOption, year, query)
                        }
                    }
                        .flow
                }
            }
            .enforceUniqueIds { it.id }
            .cachedIn(viewModelScope)

    val merch = combine(settings.dataYear, snapshotFlow { merchQuery }, ::Pair)
        .flatMapLatest { (year, query) ->
            if (year == DataYear.YEAR_2023) {
                flowOf(PagingData.empty())
            } else {
                createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                    if (query.isBlank()) {
                        tagsEntryDao.getMerch(year)
                    } else {
                        tagsEntryDao.searchMerch(year, query)
                    }
                }
                    .flow
            }
        }
        .enforceUniqueIds { it.name }
        .cachedIn(viewModelScope)

    var seriesQuery by mutableStateOf("")
    var merchQuery by mutableStateOf("")

    val dataYear = settings.dataYear

    val defaultSeriesFiltersState =
        SeriesFilterOption.entries.map { it to (it == SeriesFilterOption.ALL) }
    var seriesFiltersState by savedStateHandle.saveable {
        mutableStateOf(defaultSeriesFiltersState)
    }
    var previouslyClickedOption by savedStateHandle.saved<SeriesFilterOption> { SeriesFilterOption.ALL }

    private val seriesImageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    fun getSeriesImage(series: SeriesEntry) = seriesImageLoader.getSeriesImage(series)

    fun onSeriesFilterClick(option: SeriesFilterOption) {
        val state = if (option == SeriesFilterOption.ALL) {
            defaultSeriesFiltersState
        } else {
            val oldState = seriesFiltersState
            // If re-clicking an already active filter, make it exclusive and disable all others
            if (previouslyClickedOption == option &&
                oldState.count { it.second } >= 2 &&
                oldState.first { it.first == option }.second) {
                SeriesFilterOption.entries.map { it to (it == option) }
            } else {
                oldState.toMutableList().map {
                    if (it.first == SeriesFilterOption.ALL) {
                        it.first to false
                    } else {
                        it.first to if (it.first == option) {
                            !it.second
                        } else {
                            it.second
                        }
                    }
                }
            }
        }

        seriesFiltersState = state.takeUnless { it.none { it.second } }
            ?: defaultSeriesFiltersState
        previouslyClickedOption = option
    }
}
