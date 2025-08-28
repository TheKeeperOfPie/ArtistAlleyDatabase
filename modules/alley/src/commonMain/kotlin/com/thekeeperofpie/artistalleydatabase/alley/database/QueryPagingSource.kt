package com.thekeeperofpie.artistalleydatabase.alley.database

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.TransactionCallbacks
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.PlatformType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

private val globalDatabaseMutex = Mutex()

// Copied out from androidx-paging3-extensions since it doesn't publish a wasmJs target
class OffsetQueryPagingSource<RowType : Any>(
    private val queryProvider: suspend (limit: Int, offset: Int) -> Query<RowType>,
    private val countQuery: suspend () -> Query<Int>,
    private val transacter: suspend () -> SuspendingTransacter,
    private val context: CoroutineContext,
    private val initialOffset: Int,
) : QueryPagingSource<Int, RowType>() {

    override val jumpingSupported get() = true

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, RowType> = withContext(context) {
        val key = params.key ?: initialOffset
        val limit = when (params) {
            is LoadParams.Prepend<*> -> minOf(key, params.loadSize)
            else -> params.loadSize
        }
        val getPagingSourceLoadResult: suspend TransactionCallbacks.() -> LoadResult.Page<Int, RowType> = {
            val count = countQuery().awaitAsOne()
            val offset = when (params) {
                is LoadParams.Prepend<*> -> maxOf(0, key - params.loadSize)
                is LoadParams.Append<*> -> key
                is LoadParams.Refresh<*> -> if (key >= count) maxOf(0, count - params.loadSize) else key
            }
            val data = queryProvider(limit, offset)
                .also { currentQuery = it }
                .awaitAsList()
            val nextPosToLoad = offset + data.size
            LoadResult.Page(
                data = data,
                prevKey = offset.takeIf { it > 0 && data.isNotEmpty() },
                nextKey = nextPosToLoad.takeIf { data.isNotEmpty() && data.size >= limit && it < count },
                itemsBefore = offset,
                itemsAfter = maxOf(0, count - nextPosToLoad),
            )
        }

        // TODO: Multi-threading not safe with parallel transactions. This doesn't block other
        //  database accesses, and is a really bad hack.
        //  https://github.com/eygraber/sqldelight-androidx-driver/issues/25
        val loadResult = if (PlatformSpecificConfig.type == PlatformType.ANDROID) {
            globalDatabaseMutex.withLock {
                transacter().transactionWithResult(bodyWithReturn = getPagingSourceLoadResult)
            }
        } else {
            transacter().transactionWithResult(bodyWithReturn = getPagingSourceLoadResult)
        }
        (if (invalid) LoadResult.Invalid() else loadResult)
    }

    override fun getRefreshKey(state: PagingState<Int, RowType>) =
        state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }
}

abstract class QueryPagingSource<Key : Any, RowType : Any> :
    PagingSource<Key, RowType>(),
    Query.Listener {

    protected var currentQuery: Query<RowType>? by Delegates.observable(null) { _, old, new ->
        old?.removeListener(this)
        new?.addListener(this)
    }

    init {
        registerInvalidatedCallback {
            currentQuery?.removeListener(this)
            currentQuery = null
        }
    }

    final override fun queryResultsChanged() = invalidate()
}
