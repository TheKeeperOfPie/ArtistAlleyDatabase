package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(tagsEntryDao: TagEntryDao) : ViewModel() {

    val series = MutableStateFlow<PagingData<SeriesEntry>>(PagingData.empty())
    val merch = MutableStateFlow<PagingData<MerchEntry>>(PagingData.empty())

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            Pager(PagingConfig(pageSize = 20)) { tagsEntryDao.getSeries() }
                .flow
                .cachedIn(viewModelScope)
                .collectLatest(series::emit)
        }
        viewModelScope.launch(CustomDispatchers.IO) {
            Pager(PagingConfig(pageSize = 20)) { tagsEntryDao.getMerch() }
                .flow
                .cachedIn(viewModelScope)
                .collectLatest(merch::emit)
        }
    }
}
