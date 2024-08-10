package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.withContext

class MediaIgnorePagingSource(
    private val ignoreDao: AnimeIgnoreDao,
    private val mediaType: MediaType,
) : PagingSource<Int, AnimeMediaIgnoreEntry>() {

    override val jumpingSupported = true

    override fun getRefreshKey(state: PagingState<Int, AnimeMediaIgnoreEntry>) =
        state.anchorPosition?.let { (it / 10) + 1 }

    override suspend fun load(params: LoadParams<Int>) = withContext(CustomDispatchers.IO) {
        val entryCount = ignoreDao.getEntryCount()
        val page = params.key ?: return@withContext LoadResult.Page(
            data = emptyList<AnimeMediaIgnoreEntry>(),
            prevKey = null,
            nextKey = 0,
            itemsBefore = 0,
            itemsAfter = entryCount,
        )

        val itemsAfter = (entryCount - page * 10).coerceAtLeast(0)
        val entries = ignoreDao.getEntries(limit = 10, offset = page * 10, type = mediaType)
        LoadResult.Page(
            data = entries,
            prevKey = (page - 1).takeIf { page > 0 },
            nextKey = (page + 1).takeIf { itemsAfter > 0 },
            itemsBefore = page * 10,
            itemsAfter = itemsAfter,
        )
    }
}
