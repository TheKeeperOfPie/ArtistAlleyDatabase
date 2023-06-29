package com.thekeeperofpie.artistalleydatabase.anime.ignore

import android.os.SystemClock
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.MediaByIdsQuery
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry

class AnimeMediaIgnorePagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
) : PagingSource<Int, MediaByIdsQuery.Data.Page.Medium>() {

    companion object {
        private const val PAGE_SIZE = 10
    }

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, MediaByIdsQuery.Data.Page.Medium>) =
        state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, MediaByIdsQuery.Data.Page.Medium> = try {
        val page = params.key ?: 0
        val toIndex = ((page + 1) * PAGE_SIZE).coerceAtMost(refreshParams.ignoredIds.size)
        val ids = refreshParams.ignoredIds.subList(page * PAGE_SIZE, toIndex)
        val results = aniListApi.mediaByIds(ids)
        val itemsAfter = (refreshParams.ignoredIds.size - ((page + 1) * PAGE_SIZE)).coerceAtLeast(0)
        LoadResult.Page(
            data = results,
            prevKey = (page - 1).takeIf { page > 1 },
            nextKey = (page + 1).takeIf { itemsAfter > 0 },
            itemsBefore = page * PAGE_SIZE,
            itemsAfter = itemsAfter,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    data class RefreshParams(
        val requestMillis: Long = SystemClock.uptimeMillis(),
        val ignoredIds: List<Int>,
    ) {
        constructor(
            requestMillis: Long,
            ignoredIds: Set<Int>,
            sortOptions: List<SortEntry<MediaIgnoreSortOption>>,
            sortAscending: Boolean,
        ) : this(
            requestMillis,
            if (sortOptions.find { it.state == FilterIncludeExcludeState.INCLUDE }?.value
                == MediaIgnoreSortOption.ID
            ) {
                if (sortAscending) ignoredIds.sorted() else ignoredIds.sortedDescending()
            } else ignoredIds.toMutableList()
        )
    }
}
