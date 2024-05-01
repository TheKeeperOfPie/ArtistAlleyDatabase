package com.thekeeperofpie.artistalleydatabase.anilist.paging

import androidx.collection.LruCache
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.fragment.PaginationInfo
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.withContext

open class AniListPagingSource2<T : Any>(
    private val perPage: Int = 10,
    private val cache: LruCache<Int, LoadResult.Page<RefreshKey, T>>? = null,
    private val apiCall: suspend (key: RefreshKey) -> Pair<PaginationInfo?, List<T?>?>,
) : PagingSource<AniListPagingSource2.RefreshKey, T>() {

    override val jumpingSupported = true
    override val keyReuseSupported = true

    override fun getRefreshKey(state: PagingState<RefreshKey, T>): RefreshKey? {
        val anchorPosition = state.anchorPosition ?: return null
        val targetIndex = (anchorPosition - (state.config.initialLoadSize / 2)).coerceAtLeast(0)
        return RefreshKey(page = (targetIndex / state.config.pageSize) + 1, skipCache = true)
    }

    override suspend fun load(params: LoadParams<RefreshKey>) = withContext(CustomDispatchers.IO) {
        try {
            // AniList pages start at 1
            val page = params.key?.page ?: 1
            val cached = cache?.get(page)
            if (cached != null) {
                return@withContext cached
            }

            val call = apiCall(params.key ?: RefreshKey.NEW)
            val pageInfo = call.first
            val result = call.second?.filterNotNull().orEmpty()

            // Sometimes the API returns 500 even if there aren't that many results
            var pageTotal = pageInfo?.total?.takeIf { it != 500 }
            if (pageInfo?.hasNextPage != true && page == 1 && pageTotal != result.size) {
                pageTotal = result.size
            }
            val itemsAfter = if (pageInfo?.hasNextPage != true) {
                0
            } else {
                pageTotal?.let { (it - (page * perPage)) }?.takeIf { it > 0 }
            }
            val nextSkipCache = params.key?.skipCache ?: true
            LoadResult.Page(
                data = result,
                prevKey = (page - 1).takeIf { page > 1 }?.let { RefreshKey(it, skipCache = nextSkipCache) },
                nextKey = (page + 1).takeIf { pageInfo?.hasNextPage == true }
                    ?.let { RefreshKey(it, skipCache = nextSkipCache) },
                itemsBefore = (page - 1) * perPage,
                itemsAfter = itemsAfter ?: LoadResult.Page.COUNT_UNDEFINED,
            ).also {
                cache?.put(page, it)
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    data class RefreshKey(
        val page: Int,
        val skipCache: Boolean,
    ) {
        companion object {
            val NEW = RefreshKey(page = 1, skipCache = false)
        }
    }
}
