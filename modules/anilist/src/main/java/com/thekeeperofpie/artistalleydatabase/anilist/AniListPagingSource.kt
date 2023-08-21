package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.fragment.PaginationInfo
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.withContext

class AniListPagingSource<T : Any>(
    private val perPage: Int = 10,
    private val apiCall: suspend (page: Int) -> Pair<PaginationInfo?, List<T>>,
) : PagingSource<Int, T>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, T>) =
        state.anchorPosition?.let { (it / perPage) + 1 }

    override suspend fun load(params: LoadParams<Int>) = withContext(CustomDispatchers.IO) {
        try {
            // AniList pages start at 1
            val page = params.key ?: 1

            val (pageInfo, result) = apiCall(page)

            // Sometimes the API returns 500 even if there aren't that many results
            var pageTotal = pageInfo?.total?.takeIf { it != 500 }
            if (pageInfo?.hasNextPage != true && page == 1 && pageTotal != result.size) {
                pageTotal = result.size
            }
            val itemsAfter = if (pageInfo?.hasNextPage != true) {
                0
            } else {
                // TODO: Pass perPage in so it can be customized
                pageTotal?.let { (it - (page * perPage)) }?.takeIf { it > 0 }
            }
            LoadResult.Page(
                data = result,
                prevKey = (page - 1).takeIf { page > 1 },
                nextKey = (page + 1).takeIf { pageInfo?.hasNextPage == true },
                itemsBefore = (page - 1) * 10,
                itemsAfter = itemsAfter ?: LoadResult.Page.COUNT_UNDEFINED,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
