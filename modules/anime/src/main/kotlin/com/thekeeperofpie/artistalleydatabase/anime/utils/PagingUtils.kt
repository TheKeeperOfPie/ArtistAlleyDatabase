@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.utils

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.CheckResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.paging.filter
import androidx.paging.map
import com.apollographql.apollo3.api.Optional
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

inline fun <T : Any> Flow<PagingData<T>>.enforceUniqueIds(
    crossinline id: suspend (value: T) -> String?,
) = map {
    // AniList can return duplicates across pages, manually enforce uniqueness
    val seenIds = mutableSetOf<String>()
    it.filterOnIO {
        @Suppress("NAME_SHADOWING") val id = id(it)
        if (id == null) false else seenIds.add(id)
    }
}

inline fun <T : Any> Flow<PagingData<T>>.enforceUniqueIntIds(
    crossinline id: suspend (value: T) -> Int?,
) = map {
    // AniList can return duplicates across pages, manually enforce uniqueness
    val seenIds = mutableSetOf<Int>()
    it.filterOnIO {
        @Suppress("NAME_SHADOWING") val id = id(it)
        if (id == null) false else seenIds.add(id)
    }
}

fun <Input : Any, Output : Any> PagingData<Input>.mapNotNull(
    transform: suspend (Input) -> Output?,
): PagingData<Output> = mapOnIO { Optional.presentIfNotNull(transform(it)) }
    .filterOnIO { it is Optional.Present }
    .mapOnIO { it.getOrThrow() }

data class PagingPlaceholderKey(private val index: Int) : Parcelable {
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

@CheckResult
@JvmSynthetic
fun <T : Any> PagingData<T>.filterOnIO(predicate: suspend (T) -> Boolean) = filter {
    withContext(CustomDispatchers.IO) { predicate(it) }
}

@CheckResult
fun <T : Any, R : Any> PagingData<T>.mapOnIO(transform: suspend (T) -> R) = map {
    withContext(CustomDispatchers.IO) {
        transform(it)
    }
}

object PagingPlaceholderContentType

fun <T : Any> LazyListScope.items(
    data: LazyPagingItems<T>,
    placeholderCount: Int,
    key: (T) -> Any,
    contentType: (T?) -> String,
    itemContent: @Composable LazyItemScope.(item: T?) -> Unit,
) {
    val mockingPlaceholder =
        data.loadState.refresh is LoadState.Loading && data.itemCount == 0
    val itemCount = if (mockingPlaceholder) placeholderCount else data.itemCount
    val itemKey = data.itemKey { key(it) }
    val itemContentType = data.itemContentType { contentType(it) }
    items(
        count = itemCount,
        key = { if (mockingPlaceholder) PagingPlaceholderKey(it) else itemKey(it) },
        contentType = { if (mockingPlaceholder) contentType(null) else itemContentType(it) },
    ) {
        val item = if (mockingPlaceholder) null else data[it]
        itemContent(item)
    }
}

fun <T : Any> LazyGridScope.items(
    data: LazyPagingItems<T>,
    placeholderCount: Int,
    key: (T) -> Any,
    contentType: (T) -> String,
    itemContent: @Composable LazyGridItemScope.(item: T?) -> Unit,
) {
    val mockingPlaceholder =
        data.loadState.refresh is LoadState.Loading && data.itemCount == 0
    val itemCount = if (mockingPlaceholder) placeholderCount else data.itemCount
    val itemKey = data.itemKey { key(it) }
    val itemContentType = data.itemContentType { contentType(it) }
    items(
        count = itemCount,
        key = { if (mockingPlaceholder) PagingPlaceholderKey(it) else itemKey(it) },
        contentType = {
            if (mockingPlaceholder) {
                PagingPlaceholderContentType
            } else {
                itemContentType(it)
            }
        },
    ) {
        val item = if (mockingPlaceholder) null else data[it]
        itemContent(item)
    }
}

fun <T : Any> LazyGridScope.itemsIndexed(
    data: LazyPagingItems<T>,
    placeholderCount: Int,
    key: (T) -> Any,
    contentType: (T) -> String,
    itemContent: @Composable LazyGridItemScope.(index: Int, item: T?) -> Unit,
) {
    val mockingPlaceholder =
        data.loadState.refresh is LoadState.Loading && data.itemCount == 0
    val itemCount = if (mockingPlaceholder) placeholderCount else data.itemCount
    val itemKey = data.itemKey { key(it) }
    val itemContentType = data.itemContentType { contentType(it) }
    items(
        count = itemCount,
        key = { if (mockingPlaceholder) PagingPlaceholderKey(it) else itemKey(it) },
        contentType = {
            if (mockingPlaceholder) {
                PagingPlaceholderContentType
            } else {
                itemContentType(it)
            }
        },
    ) {
        val item = if (mockingPlaceholder) null else data[it]
        itemContent(it, item)
    }
}

fun <T : Any> LazyListScope.items(
    data: List<T>?,
    placeholderCount: Int,
    key: (T) -> Any,
    contentType: (T?) -> String,
    itemContent: @Composable LazyItemScope.(item: T?) -> Unit,
) {
    if (data == null) {
        items(
            count = placeholderCount,
            key = { PagingPlaceholderKey(it) },
            contentType = { contentType(null) },
        ) {
            itemContent(null)
        }
    } else {
        items(
            items = data,
            key = key,
            contentType = contentType,
        ) {
            itemContent(it)
        }
    }
}

fun <T : Any> LazyGridScope.items(
    data: List<T>?,
    placeholderCount: Int,
    key: (T) -> Any,
    contentType: (T?) -> String,
    itemContent: @Composable LazyGridItemScope.(item: T?) -> Unit,
) {
    items(
        count = data?.size ?: placeholderCount,
        key = { index ->
            data?.getOrNull(index)?.let { key(it) }
                ?: PagingPlaceholderKey(index)
        },
        contentType = { contentType(data?.getOrNull(it)) },
    ) {
        itemContent(data?.getOrNull(it))
    }
}

fun <T : Any> LazyListScope.itemsIndexed(
    data: List<T>?,
    placeholderCount: Int,
    key: (index: Int, T) -> Any,
    contentType: (index: Int, T?) -> String,
    itemContent: @Composable LazyItemScope.(index: Int, item: T?) -> Unit,
) {
    items(
        count = data?.size ?: placeholderCount,
        key = { index ->
            data?.getOrNull(index)?.let { key(index, it) }
                ?: PagingPlaceholderKey(index)
        },
        contentType = { contentType(it, data?.getOrNull(it)) },
    ) {
        itemContent(it, data?.getOrNull(it))
    }
}

@Composable
fun <T : Any> rememberPagerState(data: LazyPagingItems<T>, placeholderCount: Int): PagerState {
    return rememberPagerState(pageCount = {
        val mockingPlaceholder =
            data.loadState.refresh is LoadState.Loading && data.itemCount == 0
        if (mockingPlaceholder) placeholderCount else data.itemCount
    })
}

@Composable
fun <T : Any> rememberPagerState(data: List<T>?, placeholderCount: Int): PagerState {
    return rememberPagerState(pageCount = { data?.size ?: placeholderCount })
}

fun <T : Any> LazyPagingItems<T>.getOrNull(index: Int) =
    if (index >= itemCount) null else get(index)
