package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.UserSearchQuery.Data.Page.User
import com.anilist.type.UserSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.user.UserSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.user.UserSortOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry

class AnimeSearchUserPagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
) : PagingSource<Int, User>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, User>) = state.anchorPosition?.let {
        state.closestPageToPosition(it)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> = try {
        // AniList pages start at 1
        val page = params.key ?: 1

        val result = aniListApi.searchUsers(
            query = refreshParams.query,
            page = page,
            sort = refreshParams.sortApiValue(),
            isModerator = refreshParams.filterParams.isModerator,
        )
        val pageInfo = result.page.pageInfo
        val itemsAfter = if (pageInfo.hasNextPage != true) {
            0
        } else {
            pageInfo.total?.let { (it - (page * 10)) }?.takeIf { it > 0 }
        }
        LoadResult.Page(
            data = result.page.users?.filterNotNull().orEmpty(),
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
        val filterParams: UserSortFilterController.FilterParams,
    ) {
        fun sortApiValue() = filterParams.sort.filter { it.state == FilterIncludeExcludeState.INCLUDE }
            .flatMap { it.value.toApiValue(filterParams.sortAscending) }
            .ifEmpty { listOf(UserSort.SEARCH_MATCH) }
    }
}
