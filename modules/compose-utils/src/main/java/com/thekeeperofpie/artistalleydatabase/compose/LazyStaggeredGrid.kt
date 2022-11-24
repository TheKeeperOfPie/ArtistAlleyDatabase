package com.thekeeperofpie.artistalleydatabase.compose

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

object LazyStaggeredGrid {

    @Composable
    operator fun <T : Any> invoke(
        state: LazyStaggeredGridState,
        modifier: Modifier,
        content: @Composable LazyStaggeredGridScope<T>.() -> Unit,
    ) {
        val gridScope = LazyStaggeredGridScope<T>()
        content(gridScope)

        val items = gridScope.items
        val itemContent = gridScope.itemContent
        val key = gridScope.key
        if (items == null || itemContent == null || key == null) return

        Row(
            modifier = modifier
                .scrollable(
                    state.scrollableState,
                    Orientation.Vertical,
                    flingBehavior = ScrollableDefaults.flingBehavior()
                )
        ) {
            val columnCount = state.columnCount
            for (columnIndex in 0 until columnCount) {
                val listState = rememberLazyListState()
                state.addListState(listState)
                LaunchedEffect(key1 = Unit) {
                    state.scrollFlow.collectLatest { delta ->
                        listState.dispatchRawDelta(-delta)
                    }
                }
                LaunchedEffect(key1 = Unit) {
                    state.scrollToIndexFlows[columnIndex].collectLatest { (index, offset) ->
                        listState.animateScrollToItem(index, offset)
                    }
                }
                LazyColumn(
                    userScrollEnabled = false,
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    val itemCount = items.itemCount
                    items(
                        count = itemCount / columnCount + (itemCount % columnCount - columnIndex)
                            .coerceAtLeast(0),
                        key = { index ->
                            val item = items.peek(index)
                            if (item == null) {
                                PagingPlaceholderKey(index)
                            } else {
                                key(item)
                            }
                        }
                    ) { index ->
                        val itemIndex = index * columnCount + columnIndex
                        itemContent(itemIndex, items[itemIndex])
                    }
                }
            }
        }
    }

    class LazyStaggeredGridScope<T : Any> {
        var items: LazyPagingItems<T>? = null
        var key: ((item: T) -> Any)? = null
        var itemContent: @Composable (LazyItemScope.(index: Int, T?) -> Unit)? = null

        fun items(
            items: LazyPagingItems<T>,
            key: ((item: T) -> Any),
            itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit
        ) {
            this.items = items
            this.key = key
            this.itemContent = itemContent
        }
    }

    @Composable
    fun rememberLazyStaggeredGridState(columnCount: Int): LazyStaggeredGridState {
        val scrollFlow = remember { MutableSharedFlow<Float>(1, 1) }
        val scrollableState = rememberScrollableState { delta ->
            scrollFlow.tryEmit(delta)
            delta
        }
        val scrollToTopFlows = (0 until columnCount)
            .map { remember { MutableSharedFlow<Pair<Int, Int>>(1, 1) } }
        return LazyStaggeredGridState(columnCount, scrollFlow, scrollableState, scrollToTopFlows)
    }

    class LazyStaggeredGridState(
        val columnCount: Int,
        internal val scrollFlow: SharedFlow<Float>,
        internal val scrollableState: ScrollableState,
        internal val scrollToIndexFlows: List<MutableSharedFlow<Pair<Int, Int>>>,
    ) {
        private val lastScrollPositions = Array(columnCount) { 0 }
        private val lastScrollOffsets = Array(columnCount) { 0 }
        val lazyListStates = mutableListOf<LazyListState>()

        fun scrollToTop() {
            if (lazyListStates.first().firstVisibleItemIndex == 0) {
                scrollToIndexFlows.forEachIndexed { index, flow ->
                    flow.tryEmit(lastScrollPositions[index] to lastScrollOffsets[index])
                }
            } else {
                lazyListStates.forEachIndexed { index, lazyListState ->
                    lastScrollPositions[index] = lazyListState.firstVisibleItemIndex
                    lastScrollOffsets[index] = lazyListState.firstVisibleItemScrollOffset
                }
                scrollToIndexFlows.forEach { it.tryEmit(0 to 0) }
            }
        }

        fun addListState(state: LazyListState) {
            if (lazyListStates.size < columnCount) {
                lazyListStates.add(state)
            }
        }
    }

    @SuppressLint("BanParcelableUsage")
    private data class PagingPlaceholderKey(private val index: Int) : Parcelable {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(index)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
                object : Parcelable.Creator<PagingPlaceholderKey> {
                    override fun createFromParcel(parcel: Parcel) =
                        PagingPlaceholderKey(parcel.readInt())

                    override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
                }
        }
    }
}