package com.thekeeperofpie.artistalleydatabase.entry.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

abstract class EntrySearchViewModel<SearchQuery, GridModel : EntryGridModel> :
    ViewModel(), EntryGridViewModel<GridModel> {

    var query by mutableStateOf("")

    val results = MutableStateFlow(PagingData.empty<GridModel>())

    abstract val sections: List<EntrySection>

    init {
        viewModelScope.launch(PlatformDispatchers.IO) {
            @Suppress("OPT_IN_USAGE")
            combine(snapshotFlow { query }, searchOptions(), ::Pair)
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (query, options) -> mapQuery(query, options) }
                .cachedIn(viewModelScope)
                .collect(results)
        }
    }

    fun onQuery(query: String) {
        val currentValue = this.query
        if (currentValue != query) {
            clearSelected()
        }
        this.query = query
    }

    abstract fun mapQuery(query: String, options: SearchQuery): Flow<PagingData<GridModel>>

    abstract fun searchOptions(): Flow<SearchQuery>
}
