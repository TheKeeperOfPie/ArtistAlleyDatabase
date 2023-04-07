package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.type.MediaSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagEntry

class AnimeMediaSearchPagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
) : PagingSource<Int, MediaAdvancedSearchQuery.Data.Page.Medium>() {

    override fun getRefreshKey(state: PagingState<Int, MediaAdvancedSearchQuery.Data.Page.Medium>) =
        state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, MediaAdvancedSearchQuery.Data.Page.Medium> = try {
        val flattenedTags = refreshParams.tagsByCategory
            .map { it.value }
            .flatten()

        // AniList pages start at 1
        val page = params.key ?: 1
        val result = aniListApi.searchMedia(
            query = refreshParams.query,
            page = page,
            sort = refreshParams.sortApiValue(),
            genreIn = refreshParams.genres
                .filter { it.state == MediaGenreEntry.State.INCLUDE }
                .map { it.name },
            genreNotIn = refreshParams.genres
                .filter { it.state == MediaGenreEntry.State.EXCLUDE }
                .map { it.name },
            tagIn = flattenedTags
                .filter { it.state == MediaTagEntry.State.INCLUDE }
                .mapNotNull { it.name },
            tagNotIn = flattenedTags
                .filter { it.state == MediaTagEntry.State.EXCLUDE }
                .mapNotNull { it.name },
        )

        val data = result.dataAssertNoErrors
        val total = data.page.pageInfo.total
        val itemsAfter = total?.let { (it - (page * 10)) }?.takeIf { it > 0 }
        LoadResult.Page(
            data = data.page.media.filterNotNull(),
            prevKey = (page - 1).takeIf { page > 1 },
            nextKey = page + 1,
            itemsBefore = (page - 1) * 10,
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
        val genres: List<MediaGenreEntry>,
        val tagsByCategory: Map<String?, List<MediaTagEntry>>,
    ) {
        fun sortApiValue() = if (sort == MediaSortOption.DEFAULT) {
            arrayOf(MediaSort.SEARCH_MATCH)
        } else {
            arrayOf(sort.toApiValue(sortAscending))
        }
    }
}