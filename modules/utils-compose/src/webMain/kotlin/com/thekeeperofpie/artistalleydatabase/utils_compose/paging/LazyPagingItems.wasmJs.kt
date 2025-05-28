package com.thekeeperofpie.artistalleydatabase.utils_compose.paging

import androidx.compose.runtime.Composable
import app.cash.paging.CombinedLoadStates
import app.cash.paging.ItemSnapshotList
import app.cash.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import app.cash.paging.compose.LazyPagingItems as LazyPagingItemsMultiplatform
import app.cash.paging.compose.collectAsLazyPagingItems as collectAsLazyPagingItemsMultiplatform

actual class LazyPagingItems<T : Any> internal constructor(
    private val lazyPagingItems: LazyPagingItemsMultiplatform<T>,
) {
    actual val loadState: CombinedLoadStates get() = lazyPagingItems.loadState
    actual val itemSnapshotList: ItemSnapshotList<T> get() = lazyPagingItems.itemSnapshotList
    actual val itemCount: Int get() = lazyPagingItems.itemCount

    actual operator fun get(index: Int): T? = lazyPagingItems[index]

    actual fun peek(index: Int): T? = lazyPagingItems.peek(index)
    actual fun retry(): Unit = lazyPagingItems.retry()
    actual fun refresh(): Unit = lazyPagingItems.refresh()
}

@Composable
actual fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(
    context: CoroutineContext,
): LazyPagingItems<T> = LazyPagingItems(collectAsLazyPagingItemsMultiplatform())
