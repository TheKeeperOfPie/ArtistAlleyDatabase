package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.UserEntryQueries
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class UserEntryDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> UserEntryQueries = { database().userEntryQueries },
) {
    fun getBoothsWithFavorites() = flowFromSuspend { dao() }
        .flatMapLatest { it.getBoothsWithFavorites().asFlow() }
        .mapToList(PlatformDispatchers.IO)

    suspend fun insertArtistUserEntry(entry: ArtistUserEntry) {
        dao().transaction {
            val existing = dao().getArtistUserEntry(entry.artistId).awaitAsOneOrNull() ?: entry
            val newEntry = existing.copy(
                favorite = entry.favorite,
                ignored = entry.ignored,
                notes = entry.notes,
            )
            dao().insertArtistUserEntry(newEntry)
        }
    }

    suspend fun insertStampRallyUserEntry(entry: StampRallyUserEntry) {
        dao().transaction {
            val existing = dao().getStampRallyUserEntry(entry.stampRallyId).awaitAsOneOrNull()
                ?: entry
            val newEntry = existing.copy(
                favorite = entry.favorite,
                ignored = entry.ignored,
                notes = entry.notes,
            )
            dao().insertStampRallyUserEntry(newEntry)
        }
    }
}
