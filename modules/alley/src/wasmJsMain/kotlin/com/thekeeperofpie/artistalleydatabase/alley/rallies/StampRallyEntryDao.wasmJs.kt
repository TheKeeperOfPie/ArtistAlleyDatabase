package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.paging.PagingSource
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery

actual interface StampRallyEntryDao {
    actual suspend fun getEntry(id: String): StampRallyEntry?
    actual suspend fun getEntryWithArtists(id: String): StampRallyWithArtistsEntry?
    actual fun search(
        query: String,
        searchQuery: StampRallySearchQuery,
    ): PagingSource<Int, StampRallyEntry> { throw UnsupportedOperationException() }
    actual suspend fun insertEntries(vararg entries: StampRallyEntry)
    actual suspend fun insertEntries(entries: List<StampRallyEntry>)
    actual suspend fun clearEntries()
    actual suspend fun clearConnections()
    actual suspend fun insertConnections(entries: List<StampRallyArtistConnection>)
    actual suspend fun insertUpdatedEntries(entries: Collection<Pair<StampRallyEntry, List<StampRallyArtistConnection>>>) {}
    actual suspend fun retainIds(ids: List<String>)
}
