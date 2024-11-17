package com.thekeeperofpie.artistalleydatabase.anilist.paging

import androidx.collection.LruCache
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.anilist.data.fragment.PaginationInfo
import kotlinx.coroutines.flow.Flow

object AniListPager2 {

    operator fun <T : Any> invoke(
        perPage: Int = 10,
        prefetchDistance: Int = perPage,
        skipCache: Boolean = false,
        apiCall: suspend (page: AniListPagingSource2.RefreshKey) -> Pair<PaginationInfo?, List<T?>?>,
    ): Flow<PagingData<T>> {
        val cache = LruCache<Int, PagingSource.LoadResult.Page<AniListPagingSource2.RefreshKey, T>>(20)
        return Pager(
            config = PagingConfig(
                pageSize = perPage,
                initialLoadSize = perPage,
                prefetchDistance = prefetchDistance,
                jumpThreshold = perPage * 2,
                enablePlaceholders = true,
            ),
            initialKey = AniListPagingSource2.RefreshKey(page = 1, skipCache = skipCache),
            pagingSourceFactory = {
                AniListPagingSource2(perPage = perPage, cache = cache, apiCall = apiCall)
            }
        ).flow
    }
}
