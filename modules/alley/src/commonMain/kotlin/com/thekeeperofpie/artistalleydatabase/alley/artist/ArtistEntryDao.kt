package com.thekeeperofpie.artistalleydatabase.alley.artist

import app.cash.paging.PagingSource
import com.thekeeperofpie.artistalleydatabase.alley.ArtistBoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistSeriesConnection
import kotlinx.coroutines.flow.Flow

@Suppress("RedundantModalityModifier")
expect interface ArtistEntryDao {
    suspend fun getEntry(id: String): ArtistEntry?
    fun getEntryFlow(id: String): Flow<ArtistEntry>
    suspend fun getEntryWithStampRallies(id: String): ArtistWithStampRalliesEntry?
    suspend fun getEntriesSize(): Int
    fun getBoothsWithFavorite(): Flow<List<ArtistBoothWithFavorite>>
    open fun search(query: String, searchQuery: ArtistSearchQuery): PagingSource<Int, ArtistEntry>
    suspend fun insertEntries(vararg entries: ArtistEntry)
    suspend fun insertEntries(entries: List<ArtistEntry>)
    suspend fun insertSeriesConnections(entries: List<ArtistSeriesConnection>)
    suspend fun clearSeriesConnections()
    suspend fun clearMerchConnections()
    suspend fun insertMerchConnections(entries: List<ArtistMerchConnection>)
    open suspend fun insertUpdatedEntries(entries: Collection<Triple<ArtistEntry, List<ArtistSeriesConnection>, List<ArtistMerchConnection>>>)
}
