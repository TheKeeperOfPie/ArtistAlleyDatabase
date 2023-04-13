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
import java.time.temporal.ChronoUnit

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

        val onListOptions = refreshParams.onListOptions
        val containsOnList = onListOptions.find { it.value }?.state == IncludeExcludeState.INCLUDE
        val containsNotOnList =
            onListOptions.find { !it.value }?.state == IncludeExcludeState.INCLUDE
        val onList = when {
            !containsOnList && !containsNotOnList -> null
            containsOnList && containsNotOnList -> null
            else -> containsOnList
        }

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
            onList = onList,
            season = (refreshParams.airingDate as? AnimeMediaFilterController.AiringDate.Basic)
                ?.season,
            seasonYear = (refreshParams.airingDate as? AnimeMediaFilterController.AiringDate.Basic)
                ?.seasonYear
                ?.toIntOrNull(),
            startDateGreater = (refreshParams.airingDate as? AnimeMediaFilterController.AiringDate.Advanced)
                ?.startDate
                ?.minus(1, ChronoUnit.DAYS)
                ?.toApiFuzzyDateInt(),
            startDateLesser = (refreshParams.airingDate as? AnimeMediaFilterController.AiringDate.Advanced)
                ?.endDate
                ?.plus(1, ChronoUnit.DAYS)
                ?.toApiFuzzyDateInt(),
            averageScoreGreater = refreshParams.averageScoreRange.apiStart,
            averageScoreLesser = refreshParams.averageScoreRange.apiEnd,
            // Episode greater is not inclusive, offset by -1 to ensure correct results
            episodesGreater = refreshParams.episodesRange.apiStart?.let { it.coerceAtLeast(1) - 1 },
            episodesLesser = refreshParams.episodesRange.apiEnd,
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
        val sortOptions: List<AnimeMediaFilterController.SortEntry<MediaSortOption>>,
        val sortAscending: Boolean,
        val genres: List<MediaFilterEntry<String>>,
        val tagsByCategory: Map<String, AnimeMediaFilterController.TagSection>,
        val statuses: List<AnimeMediaFilterController.StatusEntry>,
        val formats: List<AnimeMediaFilterController.FormatEntry>,
        val showAdult: Boolean,
        val onListOptions: List<AnimeMediaFilterController.OnListOption>,
        val averageScoreRange: AnimeMediaFilterController.RangeData,
        val episodesRange: AnimeMediaFilterController.RangeData,
        val airingDate: AnimeMediaFilterController.AiringDate,
    ) {
        fun sortApiValue() = sortOptions.filter { it.state == IncludeExcludeState.INCLUDE }
            .map { it.value.toApiValue(sortAscending) }
            .ifEmpty { listOf(MediaSort.SEARCH_MATCH) }
    }
}