package com.thekeeperofpie.artistalleydatabase.search

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val application: Application,
    private val artEntryDao: ArtEntryDao,
) : ViewModel() {

    val query = MutableStateFlow("")

    val results = MutableStateFlow(PagingData.empty<ArtEntryModel>())

    val selectedEntries = mutableStateMapOf<Int, ArtEntryModel>()

    fun onQuery(query: String) {
        if (this.query.value != query) {
            clearSelected()
        }
        this.query.tryEmit(query)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            query.flatMapLatest {
                Pager(PagingConfig(pageSize = 20)) {
                    if (it.isEmpty()) {
                        artEntryDao.getEntries()
                    } else {
                        artEntryDao.getEntries("'*$it*'")
                    }
                }
                    .flow.cachedIn(viewModelScope)
                    .onEach {
                        viewModelScope.launch(Dispatchers.Main) {
                            // TODO: There must be a better way to sync selected items
                            clearSelected()
                        }
                    }
            }
                .map { it.map { ArtEntryModel(application, it) } }
                .collect(results)
        }
    }

    fun clearSelected() {
        synchronized(selectedEntries) {
            selectedEntries.clear()
        }
    }

    fun selectEntry(index: Int, entry: ArtEntryModel) {
        synchronized(selectedEntries) {
            if (selectedEntries.containsKey(index)) {
                selectedEntries.remove(index)
            } else {
                selectedEntries.put(index, entry)
            }
        }
    }

    fun deleteSelected() {
        synchronized(selectedEntries) {
            viewModelScope.launch(Dispatchers.IO) {
                val toDelete: List<ArtEntry>
                withContext(Dispatchers.Main) {
                    toDelete = selectedEntries.values.map { it.value }
                    selectedEntries.clear()
                }
                artEntryDao.delete(toDelete)
            }
        }
    }
}