package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadParamsAppend
import app.cash.paging.PagingSourceLoadParamsPrepend
import app.cash.paging.PagingSourceLoadParamsRefresh
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultInvalid
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.TransactionCallbacks
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

// Copied out from androidx-paging3-extensions since it doesn't publish a wasmJs target
class OffsetQueryPagingSource<RowType : Any>(
    private val queryProvider: (limit: Int, offset: Int) -> Query<RowType>,
    private val countQuery: Query<Int>,
    private val transacter: suspend () -> SuspendingTransacter,
    private val context: CoroutineContext,
    private val initialOffset: Int,
) : QueryPagingSource<Int, RowType>() {

    override val jumpingSupported get() = true

    override suspend fun load(
        params: PagingSourceLoadParams<Int>,
    ): PagingSourceLoadResult<Int, RowType> = withContext(context) {
        val key = params.key ?: initialOffset
        val limit = when (params) {
            is PagingSourceLoadParamsPrepend<*> -> minOf(key, params.loadSize)
            else -> params.loadSize
        }
        val getPagingSourceLoadResult: suspend TransactionCallbacks.() -> PagingSourceLoadResultPage<Int, RowType> = {
            val count = countQuery.awaitAsOne()
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            val offset = when (params) {
                is PagingSourceLoadParamsPrepend<*> -> maxOf(0, key - params.loadSize)
                is PagingSourceLoadParamsAppend<*> -> key
                is PagingSourceLoadParamsRefresh<*> -> if (key >= count) maxOf(0, count - params.loadSize) else key
                else -> error("Unknown PagingSourceLoadParams ${params::class}")
            }
            val data = queryProvider(limit, offset)
                .also { currentQuery = it }
                .awaitAsList()
            val nextPosToLoad = offset + data.size
            PagingSourceLoadResultPage(
                data = data,
                prevKey = offset.takeIf { it > 0 && data.isNotEmpty() },
                nextKey = nextPosToLoad.takeIf { data.isNotEmpty() && data.size >= limit && it < count },
                itemsBefore = offset,
                itemsAfter = maxOf(0, count - nextPosToLoad),
            )
        }
        val loadResult = transacter().transactionWithResult(bodyWithReturn = getPagingSourceLoadResult)
        @Suppress("USELESS_CAST")
        (if (invalid) PagingSourceLoadResultInvalid<Int, RowType>() else loadResult) as PagingSourceLoadResult<Int, RowType>
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
