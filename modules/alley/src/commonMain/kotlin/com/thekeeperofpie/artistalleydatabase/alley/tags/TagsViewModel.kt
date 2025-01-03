package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class TagsViewModel(tagsEntryDao: TagEntryDao) : ViewModel() {

    val series = MutableStateFlow<PagingData<SeriesEntry>>(PagingData.empty())
    val merch = MutableStateFlow<PagingData<MerchEntry>>(PagingData.empty())

    var seriesQuery by mutableStateOf("")
    var seriesSize by mutableIntStateOf(0)

    var merchQuery by mutableStateOf("")
    var merchSize by mutableIntStateOf(0)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { seriesQuery }
                .flatMapLatest { query ->
                    Pager(PagingConfig(pageSize = 20)) {
                        if (query.isBlank()) {
                            tagsEntryDao.getSeries()
                        } else {
                            tagsEntryDao.searchSeries(query)
                        }
                    }
                        .flow
                }
                .cachedIn(viewModelScope)
                .collectLatest(series::emit)
        }
        viewModelScope.launch(CustomDispatchers.Main) {
            tagsEntryDao.getSeriesSize()
                .flowOn(CustomDispatchers.IO)
                .collectLatest { seriesSize = it }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { merchQuery }
                .flatMapLatest { query ->
                    Pager(PagingConfig(pageSize = 20)) {
                        if (query.isBlank()) {
                            tagsEntryDao.getMerch()
                        } else {
                            tagsEntryDao.searchMerch(query)
                        }
                    }
                        .flow
                }
                .cachedIn(viewModelScope)
                .collectLatest(merch::emit)
        }
        viewModelScope.launch(CustomDispatchers.Main) {
            tagsEntryDao.getMerchSize()
                .flowOn(CustomDispatchers.IO)
                .collectLatest { merchSize = it }
        }
    }
}
