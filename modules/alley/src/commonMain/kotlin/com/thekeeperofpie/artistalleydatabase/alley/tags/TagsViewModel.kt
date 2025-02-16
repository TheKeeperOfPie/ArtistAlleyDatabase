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
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class TagsViewModel(
    tagsEntryDao: TagEntryDao,
    settings: ArtistAlleySettings,
) : ViewModel() {

    val series = MutableStateFlow<PagingData<SeriesEntry>>(PagingData.empty())
    val merch = MutableStateFlow<PagingData<MerchEntry>>(PagingData.empty())

    var seriesQuery by mutableStateOf("")
    var merchQuery by mutableStateOf("")

    val dataYear = settings.dataYear

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(settings.dataYear, snapshotFlow { seriesQuery }, ::Pair)
                .flatMapLatest { (year, query) ->
                    createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                        if (query.isBlank()) {
                            tagsEntryDao.getSeries(year)
                        } else {
                            tagsEntryDao.searchSeries(query)
                        }
                    }
                        .flow
                }
                .enforceUniqueIds { it.name }
                .cachedIn(viewModelScope)
                .collectLatest(series::emit)
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            combine(settings.dataYear, snapshotFlow { merchQuery }, ::Pair)
                .flatMapLatest { (year, query) ->
                    createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                        if (query.isBlank()) {
                            tagsEntryDao.getMerch(year)
                        } else {
                            tagsEntryDao.searchMerch(query)
                        }
                    }
                        .flow
                }
                .enforceUniqueIds { it.name }
                .cachedIn(viewModelScope)
                .collectLatest(merch::emit)
        }
    }
}
