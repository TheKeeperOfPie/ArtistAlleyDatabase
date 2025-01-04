package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.paging.PagingSource
import com.thekeeperofpie.artistalleydatabase.alley.ArtistBoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistWithStampRalliesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistSeriesConnection
import kotlinx.coroutines.flow.Flow

actual interface ArtistEntryDao {
    actual suspend fun getEntry(id: String): ArtistEntry?
    actual fun getEntryFlow(id: String): Flow<ArtistEntry>
    actual suspend fun getEntryWithStampRallies(id: String): ArtistWithStampRalliesEntry?
    actual suspend fun getEntriesSize(): Int
    actual fun getBoothsWithFavorite(): Flow<List<ArtistBoothWithFavorite>>
    actual fun search(
        query: String,
        searchQuery: ArtistSearchQuery,
    ): PagingSource<Int, ArtistEntry> {
        throw UnsupportedOperationException()
    }

    actual suspend fun insertEntries(vararg entries: ArtistEntry)
    actual suspend fun insertEntries(entries: List<ArtistEntry>)
    actual suspend fun insertSeriesConnections(entries: List<ArtistSeriesConnection>)
    actual suspend fun clearSeriesConnections()
    actual suspend fun clearMerchConnections()
    actual suspend fun insertMerchConnections(entries: List<ArtistMerchConnection>)
    actual suspend fun insertUpdatedEntries(entries: Collection<Triple<ArtistEntry, List<ArtistSeriesConnection>, List<ArtistMerchConnection>>>) {}
}
