package com.thekeeperofpie.artistalleydatabase.alley.tags

import app.cash.paging.PagingSource
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import kotlinx.coroutines.flow.Flow

@Suppress("RedundantModalityModifier")
expect interface TagEntryDao {
    fun getSeries(): PagingSource<Int, SeriesEntry>
    open fun searchSeries(query: String): PagingSource<Int, SeriesEntry>
    fun getSeriesSize(): Flow<Int>
    fun getMerch(): PagingSource<Int, MerchEntry>
    open fun searchMerch(query: String): PagingSource<Int, MerchEntry>
    fun getMerchSize(): Flow<Int>
    suspend fun insertSeries(entries: List<SeriesEntry>)
    suspend fun insertMerch(entries: List<MerchEntry>)
    suspend fun clearSeries()
    suspend fun clearMerch()
    open suspend fun getBooths(tagMapQuery: TagMapQuery): Set<String>
}
