package com.thekeeperofpie.artistalleydatabase.utils_compose.paging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeUiDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import app.cash.paging.CombinedLoadStates as CombinedLoadStatesMultiplatform

actual class LazyPagingItems<T : Any> internal constructor(
    private val flow: Flow<PagingData<T>>
) {
    private val mainDispatcher = ComposeUiDispatcher.Main
    private val pagingDataPresenter = object : PagingDataPresenter<T>(
        mainContext = mainDispatcher,
        cachedPagingData =
            if (flow is SharedFlow<PagingData<T>>) flow.replayCache.firstOrNull() else null
    ) {
        override suspend fun presentPagingDataEvent(
            event: PagingDataEvent<T>,
        ) {
            updateItemSnapshotList()
        }
    }

    private var _itemSnapshotList by mutableStateOf(
        pagingDataPresenter.snapshot()
    )

    @Suppress("USELESS_CAST")
    actual val itemSnapshotList get() = _itemSnapshotList as ItemSnapshotList

    actual val itemCount: Int get() = itemSnapshotList.size

    private fun updateItemSnapshotList() {
        _itemSnapshotList = pagingDataPresenter.snapshot()
    }

    actual operator fun get(index: Int): T? {
        pagingDataPresenter[index] // this registers the value load
        return itemSnapshotList[index]
    }

    actual fun peek(index: Int): T? = itemSnapshotList[index]

    actual fun retry() = pagingDataPresenter.retry()

    actual fun refresh() = pagingDataPresenter.refresh()

    private var _loadState: CombinedLoadStates by mutableStateOf(
        pagingDataPresenter.loadStateFlow.value
            ?: CombinedLoadStates(
                refresh = InitialLoadStates.refresh,
                prepend = InitialLoadStates.prepend,
                append = InitialLoadStates.append,
                source = InitialLoadStates
            )
    )

    @Suppress("USELESS_CAST")
    actual val loadState get() = _loadState as CombinedLoadStatesMultiplatform

    internal suspend fun collectLoadState() {
        pagingDataPresenter.loadStateFlow.filterNotNull().collect {
            _loadState = it
        }
    }

    internal suspend fun collectPagingData() {
        flow.collectLatest {
            pagingDataPresenter.collectFrom(it)
        }
    }
}

private val IncompleteLoadState = LoadState.NotLoading(false)
private val InitialLoadStates = LoadStates(
    LoadState.Loading,
    IncompleteLoadState,
    IncompleteLoadState
)

@Composable
actual fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(
    context: CoroutineContext
): LazyPagingItems<T> {
    val lazyPagingItems = remember(this) { LazyPagingItems(this) }

    LaunchedEffect(lazyPagingItems) {
        if (context == EmptyCoroutineContext) {
            lazyPagingItems.collectPagingData()
        } else {
            withContext(context) {
                lazyPagingItems.collectPagingData()
            }
        }
    }

    LaunchedEffect(lazyPagingItems) {
        if (context == EmptyCoroutineContext) {
            lazyPagingItems.collectLoadState()
        } else {
            withContext(context) {
                lazyPagingItems.collectLoadState()
            }
        }
    }

    return lazyPagingItems
}
