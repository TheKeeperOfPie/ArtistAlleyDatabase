package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.NotesQueries
import com.thekeeperofpie.artistalleydatabase.alley.utils.PersistentStorageRequester
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

class UserNotesDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> NotesQueries = { database().notesQueries },
) {
    companion object {
        const val MAX_CHARACTER_COUNT = 1000
    }

    suspend fun getArtistNotes(artistId: String, year: DataYear) =
        dao().getArtistNotes(artistId, year)
            .awaitAsOneOrNull()

    suspend fun getStampRallyNotes(stampRallyId: String) =
        dao().getStampRallyNotes(stampRallyId).awaitAsOneOrNull()

    suspend fun updateArtistNotes(artistId: String, year: DataYear, notes: String) {
        PersistentStorageRequester.requestPersistent()
        dao().updateArtistNotes(artistId, year, notes.take(MAX_CHARACTER_COUNT))
    }

    suspend fun updateStampRallyNotes(stampRallyId: String, notes: String) {
        PersistentStorageRequester.requestPersistent()
        dao().updateStampRallyNotes(stampRallyId, notes.take(MAX_CHARACTER_COUNT))
    }
}
