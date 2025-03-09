package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.NotesQueries
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear

class NotesDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val dao: suspend () -> NotesQueries = { database().notesQueries },
) {

    suspend fun getArtistNotes(artistId: String, year: DataYear) =
        dao().getArtistNotes(artistId, year)
            .awaitAsOneOrNull()

    suspend fun getStampRallyNotes(stampRallyId: String) =
        dao().getStampRallyNotes(stampRallyId).awaitAsOneOrNull()

    suspend fun updateArtistNotes(artistId: String, year: DataYear, notes: String) =
        dao().updateArtistNotes(artistId, year, notes)

    suspend fun updateStampRallyNotes(stampRallyId: String, notes: String) =
        dao().updateStampRallyNotes(stampRallyId, notes)
}
