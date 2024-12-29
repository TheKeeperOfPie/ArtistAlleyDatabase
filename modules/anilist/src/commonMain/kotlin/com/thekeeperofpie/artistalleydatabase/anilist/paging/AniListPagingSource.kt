package com.thekeeperofpie.artistalleydatabase.anilist.paging

import androidx.collection.LruCache
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.data.fragment.PaginationInfo
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.withContext

open class AniListPagingSource<T : Any>(
    private val perPage: Int = 10,
    private val cache: LruCache<Int, LoadResult.Page<Int, T>>? = null,
    private val apiCall: suspend (page: Int) -> Pair<PaginationInfo?, List<T?>?>,
) : PagingSource<Int, T>() {

    override val jumpingSupported = true
    override val keyReuseSupported = true

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val targetIndex = (anchorPosition - (state.config.initialLoadSize / 2)).coerceAtLeast(0)
        return (targetIndex / state.config.pageSize) + 1
    }

    override suspend fun load(params: LoadParams<Int>) = withContext(CustomDispatchers.IO) {
        try {
            // AniList pages start at 1
            val page = params.key ?: 1
            val cached = cache?.get(page)
            if (cached != null) {
                return@withContext cached
            }

            val call = apiCall(page)
            val pageInfo = call.first
            val result = call.second?.filterNotNull().orEmpty()

            // Sometimes the API returns 500 even if there aren't that many results
            var pageTotal = pageInfo?.total?.takeIf { it != 500 }
            if (pageInfo?.hasNextPage != true && page == 1 && pageTotal != result.size) {
                pageTotal = result.size
            }
            val itemsAfter = if (pageInfo?.hasNextPage != true || result.isEmpty()) {
                0
            } else {
                pageTotal?.let { (it - (page * perPage)) }?.takeIf { it > 0 }
            }
            LoadResult.Page(
                data = result,
                prevKey = (page - 1).takeIf { page > 1 },
                nextKey = (page + 1)
                    .takeIf { pageInfo?.hasNextPage == true && result.isNotEmpty() },
                itemsBefore = (page - 1) * perPage,
                itemsAfter = itemsAfter ?: LoadResult.Page.COUNT_UNDEFINED,
            ).also {
                cache?.put(page, it)
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
