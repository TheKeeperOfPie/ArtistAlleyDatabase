package com.thekeeperofpie.artistalleydatabase.entry.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.WeakHashMap

abstract class EntrySearchViewModel<SearchQuery, GridModel : EntryGridModel> :
    ViewModel(), EntryGridViewModel<GridModel> {

    var query by mutableStateOf("")

    val results = MutableStateFlow(PagingData.empty<GridModel>())

    abstract val sections: List<EntrySection>

    private val weakMap = WeakHashMap<PagingSource<*, *>, Unit>()
    private val weakMapLock = Mutex(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
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

    suspend fun invalidate() = weakMapLock.withLock(this) {
        weakMap.keys.toList()
    }.forEach { it.invalidate() }

    protected fun <Key : Any, Value : Any> trackPagingSource(
        block: () -> PagingSource<Key, Value>
    ) = block().apply {
        runBlocking { weakMapLock.withLock(this) { weakMap[this@apply] = Unit } }
    }

    protected suspend fun <Key : Any, Value : Any> PagingSource<Key, Value>.track() =
        weakMapLock.withLock(this) {
            apply { weakMap[this] = Unit }
        }

    abstract fun mapQuery(query: String, options: SearchQuery): Flow<PagingData<GridModel>>

    abstract fun searchOptions(): Flow<SearchQuery>
}
