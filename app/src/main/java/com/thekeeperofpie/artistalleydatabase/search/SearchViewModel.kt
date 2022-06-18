package com.thekeeperofpie.artistalleydatabase.search

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val application: Application,
    private val artEntryDao: ArtEntryDao,
) : ViewModel() {

    val query = MutableStateFlow("")

    val results = MutableStateFlow(PagingData.empty<ArtEntryModel>())

    val selectedItems = mutableStateListOf<Int>()

    fun onQuery(query: String) {
        if (this.query.value != query) {
            selectedItems.clear()
        }
        this.query.tryEmit(query)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            query.flatMapLatest {
                val entryPagingSource = if (it.isEmpty()) {
                    artEntryDao.getEntries()
                } else {
                    artEntryDao.getEntries("'*$it*'")
                }
                Pager(PagingConfig(pageSize = 20)) { entryPagingSource }
                    .flow.cachedIn(viewModelScope)
            }
                .map { it.map { ArtEntryModel(application, it) } }
                .collect(results)
        }
    }

    fun selectEntry(index: Int) {
        synchronized(selectedItems) {
            if (selectedItems.contains(index)) {
                selectedItems.remove(index)
            } else {
                selectedItems.add(index)
            }
        }
    }
}