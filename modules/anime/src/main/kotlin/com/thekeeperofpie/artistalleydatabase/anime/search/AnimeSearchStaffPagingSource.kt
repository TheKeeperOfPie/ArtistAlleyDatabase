package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.StaffSearchQuery.Data.Page.Staff
import com.anilist.type.StaffSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState

class AnimeSearchStaffPagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
) : PagingSource<Int, Staff>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, Staff>) = state.anchorPosition?.let {
        state.closestPageToPosition(it)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Staff> = try {
        // AniList pages start at 1
        val page = params.key ?: 1

        val result = aniListApi.searchStaff(
            query = refreshParams.query,
            page = page,
            sort = refreshParams.sortApiValue(),
        )
        val pageInfo = result.page.pageInfo
        val itemsAfter = if (pageInfo.hasNextPage != true) {
            0
        } else {
            pageInfo.total?.let { (it - (page * 10)) }?.takeIf { it > 0 }
        }
        LoadResult.Page(
            data = result.page.staff?.filterNotNull().orEmpty(),
            prevKey = (page - 1).takeIf { page > 1 },
            nextKey = (page + 1).takeIf { pageInfo.hasNextPage == true },
            itemsBefore = (page - 1) * 10,
            itemsAfter = itemsAfter ?: LoadResult.Page.COUNT_UNDEFINED,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    data class RefreshParams(
        val query: String,
        val requestMillis: Long,
        val sortOptions: List<AnimeMediaFilterController.SortEntry<StaffSortOption>>,
        val sortAscending: Boolean,
//        val filterParams: AnimeMediaFilterController.FilterParams,
    ) {
        fun sortApiValue() = sortOptions.filter { it.state == IncludeExcludeState.INCLUDE }
            .map { it.value.toApiValue(sortAscending) }
            .ifEmpty { listOf(StaffSort.SEARCH_MATCH) }
    }
}
