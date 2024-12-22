package com.thekeeperofpie.artistalleydatabase.anime.search.data

import androidx.collection.LruCache
import com.anilist.data.MediaAdvancedSearchQuery
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaSort
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.toAniListFuzzyDateInt
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaTagSection
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class AnimeSearchMediaPagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
    cache: LruCache<Int, LoadResult.Page<Int, MediaAdvancedSearchQuery.Data.Page.Medium>>,
    private val mediaType: MediaType,
) : AniListPagingSource<MediaAdvancedSearchQuery.Data.Page.Medium>(
    cache = cache,
    perPage = 25,
    apiCall = { page ->
        val filterParams = refreshParams.filterParams
        val flattenedTags = filterParams.tagsByCategory.values.flatMap {
            when (it) {
                is MediaTagSection.Category -> it.flatten()
                is MediaTagSection.Tag -> listOf(it)
            }
        }

        val onList = filterParams.onList

        val season = refreshParams.seasonYearOverride?.first
            ?: (filterParams.airingDate as? AiringDate.Basic)?.season?.toAniListSeason()

        val seasonYear = refreshParams.seasonYearOverride?.second
            ?: (filterParams.airingDate as? AiringDate.Basic)
                ?.seasonYear
                ?.toIntOrNull()

        val result = aniListApi.searchMedia(
            query = refreshParams.query,
            mediaType = mediaType,
            page = page,
            perPage = 25,
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
                ?.minus(1, DateTimeUnit.DAY)
                ?.toAniListFuzzyDateInt(),
            startDateLesser = (filterParams.airingDate as? AiringDate.Advanced)
                ?.endDate
                ?.plus(1, DateTimeUnit.DAY)
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
        result.page.pageInfo to result.page.media.filterNotNull()
    }) {

    data class RefreshParams(
        val query: String,
        val includeDescription: Boolean,
        val refreshEvent: RefreshFlow.Event,
        val filterParams: MediaSearchFilterParams<MediaSortOption>,
        val seasonYearOverride: Pair<MediaSeason, Int>? = null,
    ) {
        fun sortApiValue() =
            filterParams.sort.filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .flatMap { it.value.toApiValue(filterParams.sortAscending) }
                .ifEmpty { listOf(MediaSort.SEARCH_MATCH) }
    }
}
