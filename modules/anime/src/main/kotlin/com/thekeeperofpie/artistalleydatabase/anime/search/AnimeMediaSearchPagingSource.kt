package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.type.MediaSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaFilterEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import java.time.LocalDate

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
        val flattenedTags = refreshParams.tagsByCategory.values.flatMap {
            when (it) {
                is AnimeMediaFilterController.TagSection.Category -> it.flatten()
                is AnimeMediaFilterController.TagSection.Tag -> listOf(it)
            }
        }

        // AniList pages start at 1
        val page = params.key ?: 1
        val result = aniListApi.searchMedia(
            query = refreshParams.query,
            page = page,
            perPage = 10,
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
                .map { it.value },
            statusNotIn = refreshParams.statuses
                .filter { it.state == IncludeExcludeState.EXCLUDE }
                .map { it.value },
            formatIn = refreshParams.formats
                .filter { it.state == IncludeExcludeState.INCLUDE }
                .map { it.value },
            formatNotIn = refreshParams.formats
                .filter { it.state == IncludeExcludeState.EXCLUDE }
                .map { it.value },
            showAdult = refreshParams.showAdult,
            onList = refreshParams.onList.value,
            season = (refreshParams.airingDate as? AnimeMediaFilterController.AiringDate.Basic)
                ?.season,
            seasonYear = (refreshParams.airingDate as? AnimeMediaFilterController.AiringDate.Basic)
                ?.seasonYear
                ?.toIntOrNull(),
            startDateGreater = (refreshParams.airingDate as? AnimeMediaFilterController.AiringDate.Advanced)
                ?.startDate
                ?.toApiFuzzyDateInt(),
            startDateLesser = (refreshParams.airingDate as? AnimeMediaFilterController.AiringDate.Advanced)
                ?.endDate
                ?.toApiFuzzyDateInt(),
        )

        val data = result.dataAssertNoErrors
        val pageInfo = data.page.pageInfo
        val itemsAfter = if (pageInfo.hasNextPage != true) {
            0
        } else {
            pageInfo.total?.let { (it - (page * 10)) }?.takeIf { it > 0 }
        }
        LoadResult.Page(
            data = data.page.media.filterNotNull(),
            prevKey = (page - 1).takeIf { page > 1 },
            nextKey = (page + 1).takeIf { pageInfo.hasNextPage == true },
            itemsBefore = (page - 1) * 10,
            itemsAfter = itemsAfter ?: LoadResult.Page.COUNT_UNDEFINED,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    private fun LocalDate.toApiFuzzyDateInt(): Int? {
        val monthString = monthValue.toString().padStart(2, '0')
        val dayOfMonthString = dayOfMonth.toString().padStart(2, '0')
        return "$year$monthString$dayOfMonthString".toIntOrNull()
    }

    data class RefreshParams(
        val query: String,
        val requestMillis: Long,
        val sort: MediaSortOption,
        val sortAscending: Boolean,
        val genres: List<MediaFilterEntry<String>>,
        val tagsByCategory: Map<String, AnimeMediaFilterController.TagSection>,
        val statuses: List<AnimeMediaFilterController.StatusEntry>,
        val formats: List<AnimeMediaFilterController.FormatEntry>,
        val showAdult: Boolean,
        val onList: AnimeMediaFilterController.OnListOption,
        val airingDate: AnimeMediaFilterController.AiringDate,
    ) {
        fun sortApiValue() = if (sort == MediaSortOption.DEFAULT) {
            arrayOf(MediaSort.SEARCH_MATCH)
        } else {
            arrayOf(sort.toApiValue(sortAscending))
        }
    }
}