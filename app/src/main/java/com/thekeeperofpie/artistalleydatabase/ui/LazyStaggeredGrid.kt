package com.thekeeperofpie.artistalleydatabase.ui

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

object LazyStaggeredGrid {

    @Composable
    operator fun <T : Any> invoke(
        columnCount: Int,
        modifier: Modifier,
        content: @Composable LazyStaggeredGridScope<T>.() -> Unit,
    ) {
        val states = (0 until columnCount)
            .map { rememberLazyListState() }
        val flow = remember { MutableStateFlow(0f) }
        states.forEach { state ->
            LaunchedEffect(key1 = Unit) {
                flow.collectLatest { delta ->
                    state.scrollBy(-delta)
                }
            }
        }
        val scroll = rememberScrollableState { delta ->
            flow.tryEmit(delta)
            delta
        }
        val gridScope = LazyStaggeredGridScope<T>()
        content(gridScope)

        val items = gridScope.items
        val itemContent = gridScope.itemContent
        val key = gridScope.key
        if (items == null || itemContent == null || key == null) return

        Row(
            modifier = modifier
                .scrollable(
                    scroll,
                    Orientation.Vertical,
                    flingBehavior = ScrollableDefaults.flingBehavior()
                )
        ) {
            for (columnIndex in 0 until columnCount) {
                LazyColumn(
                    userScrollEnabled = false,
                    state = states[columnIndex],
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