package com.thekeeperofpie.artistalleydatabase.form.search

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

abstract class EntrySearchViewModel<SearchQuery : EntrySearchQuery, GridModel : EntryGridModel>() :
    ViewModel(), EntryGridViewModel<GridModel> {

    val query = MutableStateFlow<SearchQuery?>(null)

    val results = MutableStateFlow(PagingData.empty<GridModel>())

    abstract val options: List<EntrySearchOption>

    init {
        viewModelScope.launch(Dispatchers.IO) {
            @Suppress("OPT_IN_USAGE")
            query.flatMapLatest { mapQuery(it) }
                .collect(results)
        }
    }

    fun onQuery(query: String) {
        if (this.query.value?.query != query) {
            clearSelected()
        }
        this.query.tryEmit(buildQueryWrapper(query))
    }

    fun refreshQuery() {
        clearSelected()
        this.query.tryEmit(buildQueryWrapper(query.value?.query))
    }

    @MainThread
    abstract fun buildQueryWrapper(query: String?): SearchQuery

    abstract fun mapQuery(query: SearchQuery?): Flow<PagingData<GridModel>>
}