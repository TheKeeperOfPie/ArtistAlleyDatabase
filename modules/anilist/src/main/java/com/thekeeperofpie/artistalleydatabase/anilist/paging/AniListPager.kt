package com.thekeeperofpie.artistalleydatabase.anilist.paging

import androidx.collection.LruCache
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.anilist.fragment.PaginationInfo
import kotlinx.coroutines.flow.Flow

object AniListPager {

    operator fun <T : Any> invoke(
        perPage: Int = 10,
        prefetchDistance: Int = perPage,
        apiCall: suspend (page: Int) -> Pair<PaginationInfo?, List<T?>?>,
    ): Flow<PagingData<T>> {
        val cache = LruCache<Int, PagingSource.LoadResult.Page<Int, T>>(20)
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                initialLoadSize = perPage,
                prefetchDistance = prefetchDistance,
                jumpThreshold = perPage * 2,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                AniListPagingSource(perPage = perPage, cache = cache, apiCall = apiCall)
            }
        ).flow
    }
}
