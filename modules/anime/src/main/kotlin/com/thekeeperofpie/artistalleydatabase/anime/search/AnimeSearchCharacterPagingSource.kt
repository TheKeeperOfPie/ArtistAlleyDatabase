package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.CharacterAdvancedSearchQuery.Data.Page.Character
import com.anilist.type.CharacterSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortFilterController
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState

class AnimeSearchCharacterPagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
) : PagingSource<Int, Character>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, Character>) = state.anchorPosition?.let {
        state.closestPageToPosition(it)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> = try {
        // AniList pages start at 1
        val page = params.key ?: 1

        val result = aniListApi.searchCharacters(
            query = refreshParams.query,
            page = page,
            sort = refreshParams.sortApiValue(),
            isBirthday = refreshParams.filterParams.isBirthday,
        )
        val pageInfo = result.page.pageInfo
        val itemsAfter = if (pageInfo.hasNextPage != true) {
            0
        } else {
            pageInfo.total?.let { (it - (page * 10)) }?.takeIf { it > 0 }
        }
        LoadResult.Page(
            data = result.page.characters?.filterNotNull().orEmpty(),
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
        val filterParams: CharacterSortFilterController.FilterParams,
    ) {
        fun sortApiValue() = filterParams.sort
            .firstOrNull { it.state == FilterIncludeExcludeState.INCLUDE }
            ?.value
            ?.toApiValueForSearch(filterParams.sortAscending)
            ?: listOf(CharacterSort.SEARCH_MATCH)
    }
}
