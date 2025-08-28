package com.thekeeperofpie.artistalleydatabase.entry.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

abstract class EntrySearchViewModel<SearchQuery, GridModel : EntryGridModel> :
    ViewModel(), EntryGridViewModel<GridModel> {

    val query = MutableStateFlow("")

    val results = MutableStateFlow(PagingData.empty<GridModel>())

    abstract val sections: List<EntrySection>

    init {
        viewModelScope.launch(PlatformDispatchers.IO) {
            @Suppress("OPT_IN_USAGE")
            combine(query, searchOptions(), ::Pair)
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (query, options) -> mapQuery(query, options) }
                .cachedIn(viewModelScope)
                .collect(results)
        }

        viewModelScope.launch {
            @Suppress("OPT_IN_USAGE")
            query.drop(1)
                .collectLatest { clearSelected() }
        }
    }

    abstract fun mapQuery(query: String, options: SearchQuery): Flow<PagingData<GridModel>>

    abstract fun searchOptions(): Flow<SearchQuery>
}
