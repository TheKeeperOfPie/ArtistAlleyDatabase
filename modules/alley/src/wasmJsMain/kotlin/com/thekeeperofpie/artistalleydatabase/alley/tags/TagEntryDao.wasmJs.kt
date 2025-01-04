package com.thekeeperofpie.artistalleydatabase.alley.tags

import app.cash.paging.PagingSource
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import kotlinx.coroutines.flow.Flow

actual interface TagEntryDao {
    actual fun getSeries(): PagingSource<Int, SeriesEntry>
    actual fun searchSeries(query: String): PagingSource<Int, SeriesEntry> {
        throw UnsupportedOperationException()
    }

    actual fun getSeriesSize(): Flow<Int>
    actual fun getMerch(): PagingSource<Int, MerchEntry>
    actual fun searchMerch(query: String): PagingSource<Int, MerchEntry> {
        throw UnsupportedOperationException()
    }

    actual fun getMerchSize(): Flow<Int>
    actual suspend fun insertSeries(entries: List<SeriesEntry>)
    actual suspend fun insertMerch(entries: List<MerchEntry>)
    actual suspend fun clearSeries()
    actual suspend fun clearMerch()
    actual suspend fun getBooths(tagMapQuery: TagMapQuery): Set<String> = emptySet()
}
