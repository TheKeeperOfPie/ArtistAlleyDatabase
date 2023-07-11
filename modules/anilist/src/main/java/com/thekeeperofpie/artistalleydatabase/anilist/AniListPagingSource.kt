package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.fragment.PaginationInfo

class AniListPagingSource<T : Any>(
    private val apiCall: suspend (page: Int) -> Pair<PaginationInfo?, List<T>>
) : PagingSource<Int, T>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, T>) =
        state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> =
        try {
            // AniList pages start at 1
            val page = params.key ?: 1

            val (pageInfo, result) = apiCall(page)
            val itemsAfter = if (pageInfo?.hasNextPage != true) {
                0
            } else {
                pageInfo.total?.let { (it - (page * 10)) }?.takeIf { it > 0 }
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
