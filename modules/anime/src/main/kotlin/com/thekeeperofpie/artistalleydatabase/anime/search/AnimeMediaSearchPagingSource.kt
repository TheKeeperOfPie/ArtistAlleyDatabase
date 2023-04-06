package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.type.MediaSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaSortOption

class AnimeMediaSearchPagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
) :
    PagingSource<Int, MediaAdvancedSearchQuery.Data.Page.Medium>() {

    override fun getRefreshKey(state: PagingState<Int, MediaAdvancedSearchQuery.Data.Page.Medium>) =
        state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, MediaAdvancedSearchQuery.Data.Page.Medium> = try {
        val page = params.key ?: 0
        val result = aniListApi.searchMedia(
            query = refreshParams.query,
            page = page,
            sort = refreshParams.sortApiValue()
        )

        val data = result.dataAssertNoErrors
        val total = data.page.pageInfo.total
        val itemsAfter = total?.let { (it - ((page + 1)* 10)) }?.takeIf { it > 0 }
        LoadResult.Page(
            data = data.page.media.filterNotNull(),
            prevKey = (page - 1).takeIf { page > 0 },
            nextKey = page + 1,
            itemsBefore = page * 10,
            itemsAfter = itemsAfter ?: LoadResult.Page.COUNT_UNDEFINED,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    data class RefreshParams(
        val query: String,
        val requestMillis: Long,
        val sort: MediaSortOption,
        val sortAscending: Boolean,
    ) {
        fun sortApiValue() = if (sort == MediaSortOption.DEFAULT) {
            arrayOf(MediaSort.SEARCH_MATCH)
        } else {
            arrayOf(sort.toApiValue(sortAscending))
        }
    }
}