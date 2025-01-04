package com.thekeeperofpie.artistalleydatabase.alley.rallies

import app.cash.paging.PagingSource
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchQuery

@Suppress("RedundantModalityModifier")
expect interface StampRallyEntryDao {
    suspend fun getEntry(id: String): StampRallyEntry?
    suspend fun getEntryWithArtists(id: String): StampRallyWithArtistsEntry?
    open fun search(
        query: String,
        searchQuery: StampRallySearchQuery,
    ): PagingSource<Int, StampRallyEntry>
    suspend fun insertEntries(vararg entries: StampRallyEntry)
    suspend fun insertEntries(entries: List<StampRallyEntry>)
    suspend fun clearEntries()
    suspend fun clearConnections()
    suspend fun insertConnections(entries: List<StampRallyArtistConnection>)
    open suspend fun insertUpdatedEntries(entries: Collection<Pair<StampRallyEntry, List<StampRallyArtistConnection>>>)
    suspend fun retainIds(ids: List<String>)
}
