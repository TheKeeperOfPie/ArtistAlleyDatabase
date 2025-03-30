package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class TagsViewModel(
    tagsEntryDao: TagEntryDao,
    settings: ArtistAlleySettings,
) : ViewModel() {

    val series = combine(settings.dataYear, settings.languageOption, snapshotFlow { seriesQuery }, ::Triple)
        .flatMapLatest { (year, languageOption, query) ->
            if (year == DataYear.YEAR_2023) {
                flowOf(PagingData.empty())
            } else {
                createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                    if (query.isBlank()) {
                        tagsEntryDao.getSeries(languageOption, year)
                    } else {
                        tagsEntryDao.searchSeries(languageOption, query)
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
                        tagsEntryDao.searchMerch(query)
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
}
