package com.thekeeperofpie.artistalleydatabase.cds.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.thekeeperofpie.artistalleydatabase.android_utils.RoomUtils
import kotlinx.coroutines.flow.Flow

@Dao
interface CdEntryBrowseDao : CdEntryDao {

    @Query(
        """
            SELECT DISTINCT (performers)
            FROM cd_entries
            LIMIT :limit OFFSET :offset
        """
    )
    fun getPerformers(limit: Int = Int.MAX_VALUE, offset: Int = 0): Flow<List<String>>

    fun getPerformer(query: String) = getPerformerInternal(RoomUtils.wrapLikeQuery(query))

    fun getPerformerFlow(query: String, limit: Int = 1) =
        getPerformerFlowInternal(query = RoomUtils.wrapLikeQuery(query), limit = limit)

    @Query(
        """
            SELECT *
            FROM cd_entries
            WHERE performers LIKE :query
        """
    )
    fun getPerformerInternal(query: String): PagingSource<Int, CdEntry>

    @Query(
        """
            SELECT *
            FROM cd_entries
            WHERE performers LIKE :query
            LIMIT :limit
        """
    )
    fun getPerformerFlowInternal(query: String, limit: Int): Flow<List<CdEntry>>
}