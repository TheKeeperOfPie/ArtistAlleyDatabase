package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.MediaTagsQuery
import com.anilist.type.MediaFormat
import com.anilist.type.MediaSort
import com.anilist.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaFilterEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState

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
                .filter { it.state == IncludeExcludeState.INCLUDE }
                .map { it.value },
            genreNotIn = refreshParams.genres
                .filter { it.state == IncludeExcludeState.EXCLUDE }
                .map { it.value },
            tagIn = flattenedTags
                .filter { it.state == IncludeExcludeState.INCLUDE }
                .map { it.value.name },
            tagNotIn = flattenedTags
                .filter { it.state == IncludeExcludeState.EXCLUDE }
                .map { it.value.name },
            statusIn = refreshParams.statuses
                .filter { it.state == IncludeExcludeState.INCLUDE }
                .map { it.value.first },
            statusNotIn = refreshParams.statuses
                .filter { it.state == IncludeExcludeState.EXCLUDE }
                .map { it.value.first },
            formatIn = refreshParams.formats
                .filter { it.state == IncludeExcludeState.INCLUDE }
                .map { it.value.first },
            formatNotIn = refreshParams.formats
                .filter { it.state == IncludeExcludeState.EXCLUDE }
                .map { it.value.first },
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
        val genres: List<MediaFilterEntry<String>>,
        val tagsByCategory: Map<String?,
                List<MediaFilterEntry<MediaTagsQuery.Data.MediaTagCollection>>>,
        val statuses: List<MediaFilterEntry<Pair<MediaStatus, Int>>>,
        val formats: List<MediaFilterEntry<Pair<MediaFormat, Int>>>,
    ) {
        fun sortApiValue() = if (sort == MediaSortOption.DEFAULT) {
            arrayOf(MediaSort.SEARCH_MATCH)
        } else {
            arrayOf(sort.toApiValue(sortAscending))
        }
    }
}