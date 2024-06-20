package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TagEntryDao {

    @Query("""SELECT * FROM series_entries ORDER BY name COLLATE NOCASE""")
    fun getSeries(): PagingSource<Int, SeriesEntry>

    @Query("""SELECT * FROM merch_entries ORDER BY name COLLATE NOCASE""")
    fun getMerch(): PagingSource<Int, MerchEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(entries: List<SeriesEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerch(entries: List<MerchEntry>)
}
