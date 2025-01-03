package com.thekeeperofpie.artistalleydatabase.utils_compose.paging

import androidx.compose.runtime.Composable
import app.cash.paging.CombinedLoadStates
import app.cash.paging.ItemSnapshotList
import app.cash.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

expect class LazyPagingItems<T : Any> {
    val loadState: CombinedLoadStates
    val itemSnapshotList: ItemSnapshotList<T>
    val itemCount: Int

    operator fun get(index: Int): T?

    fun peek(index: Int): T?
    fun retry()
    fun refresh()
}

@Composable
expect fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(
    context: CoroutineContext = EmptyCoroutineContext,
): LazyPagingItems<T>
