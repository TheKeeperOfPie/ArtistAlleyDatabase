package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSort
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AnimeSearchMediaPagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
    private val mediaType: MediaType,
) : PagingSource<Int, MediaAdvancedSearchQuery.Data.Page.Medium>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, MediaAdvancedSearchQuery.Data.Page.Medium>) =
        state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, MediaAdvancedSearchQuery.Data.Page.Medium> = try {
        val filterParams = refreshParams.filterParams
        val flattenedTags = filterParams.tagsByCategory.values.flatMap {
            when (it) {
                is AnimeMediaFilterController.TagSection.Category -> it.flatten()
                is AnimeMediaFilterController.TagSection.Tag -> listOf(it)
            }
        }

        // AniList pages start at 1
        val page = params.key ?: 1

        val onListOptions = filterParams.onListOptions
        val containsOnList =
            onListOptions.find { it.value }?.state == FilterIncludeExcludeState.INCLUDE
        val containsNotOnList =
            onListOptions.find { !it.value }?.state == FilterIncludeExcludeState.INCLUDE
        val onList = when {
            !containsOnList && !containsNotOnList -> null
            containsOnList && containsNotOnList -> null
            else -> containsOnList
        }

        val season = refreshParams.seasonYearOverride?.first
            ?: (filterParams.airingDate as? AnimeMediaFilterController.AiringDate.Basic)?.season

        val seasonYear = refreshParams.seasonYearOverride?.second
            ?: (filterParams.airingDate as? AnimeMediaFilterController.AiringDate.Basic)
                ?.seasonYear
                ?.toIntOrNull()

        val result = aniListApi.searchMedia(
            query = refreshParams.query,
            mediaType = mediaType,
            page = page,
            perPage = 10,
            sort = refreshParams.sortApiValue(),
            genreIn = filterParams.genres
                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .map { it.value },
            genreNotIn = filterParams.genres
                .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                .map { it.value },
            tagIn = flattenedTags
                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .map { it.value.name },
            tagNotIn = flattenedTags
                .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                .map { it.value.name },
            statusIn = filterParams.statuses
                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .map { it.value },
            statusNotIn = filterParams.statuses
                .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                .map { it.value },
            formatIn = filterParams.formats
                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .map { it.value },
            formatNotIn = filterParams.formats
                .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                .map { it.value },
            showAdult = filterParams.showAdult,
            onList = onList,
            season = season,
            seasonYear = seasonYear,
            startDateGreater = (filterParams.airingDate as? AnimeMediaFilterController.AiringDate.Advanced)
                ?.startDate
                ?.minus(1, ChronoUnit.DAYS)
                ?.toApiFuzzyDateInt(),
            startDateLesser = (filterParams.airingDate as? AnimeMediaFilterController.AiringDate.Advanced)
                ?.endDate
                ?.plus(1, ChronoUnit.DAYS)
                ?.toApiFuzzyDateInt(),
            averageScoreGreater = filterParams.averageScoreRange.apiStart,
            averageScoreLesser = filterParams.averageScoreRange.apiEnd,
            // Episode greater is not inclusive, offset by -1 to ensure correct results
            episodesGreater = filterParams.episodesRange.apiStart?.let { it.coerceAtLeast(1) - 1 },
            episodesLesser = filterParams.episodesRange.apiEnd,
            sourcesIn = filterParams.sources
                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .map { it.value },
            minimumTagRank = filterParams.tagRank,
        )
        val pageInfo = result.page.pageInfo
        val itemsAfter = if (pageInfo.hasNextPage != true) {
            0
        } else {
            pageInfo.total?.let { (it - (page * 10)) }?.takeIf { it > 0 }
        }
        LoadResult.Page(
            data = result.page.media.filterNotNull(),
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
        val sortOptions: List<SortEntry<MediaSortOption>>,
        val sortAscending: Boolean,
        val filterParams: AnimeMediaFilterController.FilterParams,
        val seasonYearOverride: Pair<MediaSeason, Int>? = null,
    ) {
        fun sortApiValue() = sortOptions.filter { it.state == FilterIncludeExcludeState.INCLUDE }
            .map { it.value.toApiValue(sortAscending) }
            .ifEmpty { listOf(MediaSort.SEARCH_MATCH) }
    }
}
