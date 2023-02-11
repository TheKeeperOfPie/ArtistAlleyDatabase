package com.thekeeperofpie.artistalleydatabase.entry.search

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.WeakHashMap

abstract class EntrySearchViewModel<SearchQuery : EntrySearchQuery, GridModel : EntryGridModel> :
    ViewModel(), EntryGridViewModel<GridModel> {

    val query = MutableStateFlow<SearchQuery?>(null)

    val results = MutableStateFlow(PagingData.empty<GridModel>())

    abstract val options: List<EntrySearchOption>

    private val weakMap = WeakHashMap<PagingSource<*, *>, Unit>()
    private val weakMapLock = Mutex(false)

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

    @MainThread
    abstract fun buildQueryWrapper(query: String?): SearchQuery

    abstract fun mapQuery(query: SearchQuery?): Flow<PagingData<GridModel>>
}