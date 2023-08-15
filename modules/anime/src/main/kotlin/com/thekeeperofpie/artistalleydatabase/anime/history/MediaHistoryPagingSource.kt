package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.withContext

class MediaHistoryPagingSource(
    private val historyDao: AnimeHistoryDao,
) : PagingSource<Int, AnimeMediaHistoryEntry>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, AnimeMediaHistoryEntry>) =
        state.anchorPosition?.let { (it / 10) + 1 }

    override suspend fun load(params: LoadParams<Int>) = withContext(CustomDispatchers.IO) {
        val entryCount = historyDao.getEntryCount()
        val page = params.key ?: return@withContext LoadResult.Page(
            data = emptyList<AnimeMediaHistoryEntry>(),
            prevKey = null,
            nextKey = 0,
            itemsBefore = 0,
            itemsAfter = entryCount,
        )

        val itemsAfter = (entryCount - page * 10).coerceAtLeast(0)
        val entries = historyDao.getEntries(limit = 10, offset = page * 10)
        LoadResult.Page(
            data = entries,
            prevKey = (page - 1).takeIf { page > 0 },
            nextKey = (page + 1).takeIf { itemsAfter > 0 },
            itemsBefore = page * 10,
            itemsAfter = itemsAfter,
        )
    }
}
