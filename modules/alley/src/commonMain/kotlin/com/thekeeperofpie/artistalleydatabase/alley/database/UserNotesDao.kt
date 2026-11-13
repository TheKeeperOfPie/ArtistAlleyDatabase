package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.NotesQueries
import com.thekeeperofpie.artistalleydatabase.alley.utils.PersistentStorageRequester
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@SingleIn(AppScope::class)
class UserNotesDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val dispatchers: CustomDispatchers,
    private val notesDao: suspend () -> NotesQueries = { database().notesQueries },
    private val clock: Clock = Clock.System,
) {
    @Inject
    constructor(
        database: ArtistAlleyDatabase,
        dispatchers: CustomDispatchers,
    ) : this(database = database::database, dispatchers = dispatchers)

    companion object {
        const val MAX_CHARACTER_COUNT = 1000
    }

    suspend fun artistNotes(artistId: String, year: DataYear) =
        notesDao().getArtistNotes(artistId, year)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)

    suspend fun getArtistNotes(artistId: String, year: DataYear) =
        notesDao().getArtistNotes(artistId, year)
            .awaitAsOneOrNull()

    suspend fun stampRallyNotes(stampRallyId: String) =
        notesDao().getStampRallyNotes(stampRallyId)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)

    suspend fun getStampRallyNotes(stampRallyId: String) =
        notesDao().getStampRallyNotes(stampRallyId).awaitAsOneOrNull()

    suspend fun updateArtistNotes(artistId: String, year: DataYear, notes: String) {
        PersistentStorageRequester.requestPersistent()
        notesDao().run {
            transaction {
                this@run.updateArtistNotes(
                    artistId, year, notes.take(MAX_CHARACTER_COUNT),
                    generation = AlleyDatabaseUtils.getNextGeneration(database),
                    updatedAt = clock.now(),
                )
            }
        }
    }

    suspend fun updateStampRallyNotes(stampRallyId: String, notes: String) {
        PersistentStorageRequester.requestPersistent()
        notesDao().run {
            transaction {
                this@run.updateStampRallyNotes(
                    stampRallyId,
                    notes.take(MAX_CHARACTER_COUNT),
                    generation = AlleyDatabaseUtils.getNextGeneration(database),
                    updatedAt = clock.now(),
                )
            }
        }
    }
}
