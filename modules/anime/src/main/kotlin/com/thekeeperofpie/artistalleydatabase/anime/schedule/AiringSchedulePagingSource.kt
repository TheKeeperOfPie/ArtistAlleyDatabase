package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.AiringScheduleQuery
import com.anilist.type.AiringSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import java.time.LocalDate
import java.time.ZoneOffset

class AiringSchedulePagingSource(
    private val aniListApi: AuthedAniListApi,
    private val refreshParams: RefreshParams,
) : PagingSource<Int, AiringScheduleQuery.Data.Page.AiringSchedule>() {

    companion object {
        private const val PER_PAGE = 25
    }

    override val jumpingSupported = true

    override fun getRefreshKey(
        state: PagingState<Int, AiringScheduleQuery.Data.Page.AiringSchedule>,
    ) = state.anchorPosition?.let {
        state.closestPageToPosition(it)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
    }

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, AiringScheduleQuery.Data.Page.AiringSchedule> = try {
        // AniList pages start at 1
        val page = params.key ?: 1

        val date = refreshParams.day
        val startTime = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond() - 1
        val endTime = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        val result = aniListApi.airingSchedule(
            startTime = startTime,
            endTime = endTime,
            sort = when (refreshParams.sort) {
                AiringScheduleSort.POPULARITY,
                AiringScheduleSort.TIME -> AiringSort.TIME_DESC
                AiringScheduleSort.ID -> AiringSort.ID_DESC
            },
            perPage = PER_PAGE,
            page = page,
        )
        val pageInfo = result.page?.pageInfo
        val itemsAfter = if (pageInfo?.hasNextPage != true) {
            0
        } else {
            pageInfo.total?.let { (it - (page * PER_PAGE)) }?.takeIf { it > 0 }
        }

        val data = result.page?.airingSchedules?.filterNotNull().orEmpty()
            .let {
                if (refreshParams.sort == AiringScheduleSort.POPULARITY) {
                    it.sortedWith(
                        compareByDescending<AiringScheduleQuery.Data.Page.AiringSchedule, Int?>(
                            nullsLast()
                        ) { it.media?.popularity }
                            .thenComparing(compareBy(nullsLast()) { it.airingAt })
                    )
                } else it
            }

        LoadResult.Page(
            data = data,
            prevKey = (page - 1).takeIf { page > 1 },
            nextKey = (page + 1).takeIf { pageInfo?.hasNextPage == true },
            itemsBefore = (page - 1) * PER_PAGE,
            itemsAfter = itemsAfter ?: LoadResult.Page.COUNT_UNDEFINED,
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    data class RefreshParams(
        val sort: AiringScheduleSort,
        val day: LocalDate,
        val requestMillis: Long,
    )
}
