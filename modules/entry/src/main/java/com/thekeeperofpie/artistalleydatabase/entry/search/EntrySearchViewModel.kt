package com.thekeeperofpie.artistalleydatabase.entry.search

import androidx.annotation.MainThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.WeakHashMap

abstract class EntrySearchViewModel<SearchQuery : EntrySearchQuery, GridModel : EntryGridModel> :
    ViewModel(), EntryGridViewModel<GridModel> {

    var query by mutableStateOf<SearchQuery?>(null)
    var entriesSize by mutableStateOf(0)

    val results = MutableStateFlow(PagingData.empty<GridModel>())

    abstract val options: List<EntrySearchOption>

    private val weakMap = WeakHashMap<PagingSource<*, *>, Unit>()
    private val weakMapLock = Mutex(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            @Suppress("OPT_IN_USAGE")
            snapshotFlow { query }
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { mapQuery(it) }
                .collect(results)
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            entriesSize()
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entriesSize = it }
        }
    }

    fun onQuery(query: String) {
        val currentValue = this.query
        if (currentValue?.query != query) {
            clearSelected()
        }
        this.query = buildQueryWrapper(query)
    }

    fun refreshQuery() {
        clearSelected()
        query = buildQueryWrapper(query?.query)
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

    abstract fun entriesSize(): Flow<Int>
}
