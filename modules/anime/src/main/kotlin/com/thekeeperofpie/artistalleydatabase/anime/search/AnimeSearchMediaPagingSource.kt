package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSort
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.toAniListFuzzyDateInt
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import java.time.temporal.ChronoUnit

class AnimeSearchMediaPagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
    private val mediaType: MediaType,
) : PagingSource<Int, MediaAdvancedSearchQuery.Data.Page.Medium>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, MediaAdvancedSearchQuery.Data.Page.Medium>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val targetIndex = (anchorPosition - (state.config.initialLoadSize / 2)).coerceAtLeast(0)
        return (targetIndex / state.config.pageSize) + 1
    }

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, MediaAdvancedSearchQuery.Data.Page.Medium> = try {
        val filterParams = refreshParams.filterParams
        val flattenedTags = filterParams.tagsByCategory.values.flatMap {
            when (it) {
                is TagSection.Category -> it.flatten()
                is TagSection.Tag -> listOf(it)
            }
        }

        // AniList pages start at 1
        val page = params.key ?: 1

        val onList = filterParams.onList

        val season = refreshParams.seasonYearOverride?.first
            ?: (filterParams.airingDate as? AiringDate.Basic)?.season

        val seasonYear = refreshParams.seasonYearOverride?.second
            ?: (filterParams.airingDate as? AiringDate.Basic)
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
            startDateGreater = (filterParams.airingDate as? AiringDate.Advanced)
                ?.startDate
                ?.minus(1, ChronoUnit.DAYS)
                ?.toAniListFuzzyDateInt(),
            startDateLesser = (filterParams.airingDate as? AiringDate.Advanced)
                ?.endDate
                ?.plus(1, ChronoUnit.DAYS)
                ?.toAniListFuzzyDateInt(),
            averageScoreGreater = filterParams.averageScoreRange.apiStart,
            averageScoreLesser = filterParams.averageScoreRange.apiEnd,
            // Greater is not inclusive, offset by -1 to ensure correct results
            episodesGreater = filterParams.episodesRange?.apiStart
                ?.let { it.coerceAtLeast(1) - 1 },
            episodesLesser = filterParams.episodesRange?.apiEnd,
            volumesGreater = filterParams.volumesRange?.apiStart
                ?.let { it.coerceAtLeast(1) - 1 },
            volumesLesser = filterParams.volumesRange?.apiEnd,
            chaptersGreater = filterParams.chaptersRange?.apiStart
                ?.let { it.coerceAtLeast(1) - 1 },
            chaptersLesser = filterParams.chaptersRange?.apiEnd,
            sourcesIn = filterParams.sources
                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .map { it.value },
            licensedByIdIn = filterParams.licensedBy
                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .mapNotNull { it.value.siteId },
            minimumTagRank = filterParams.tagRank,
            includeDescription = refreshParams.includeDescription,
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

    data class RefreshParams(
        val query: String,
        val includeDescription: Boolean,
        val requestMillis: Long,
        val filterParams: MediaSortFilterController.FilterParams<MediaSortOption>,
        val seasonYearOverride: Pair<MediaSeason, Int>? = null,
    ) {
        fun sortApiValue() =
            filterParams.sort.filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .flatMap { it.value.toApiValue(filterParams.sortAscending) }
                .ifEmpty { listOf(MediaSort.SEARCH_MATCH) }
    }
}
