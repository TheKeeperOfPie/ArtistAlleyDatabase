package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.SuspendingTransactionWithReturn
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyNotes
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@SingleIn(AppScope::class)
class UserNotesDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val settings: AlleyUserSettings,
    private val notesDao: suspend () -> NotesQueries = { database().notesQueries },
    private val clock: Clock = Clock.System,
) {
    @Inject
    constructor(
        database: ArtistAlleyDatabase,
        settings: AlleyUserSettings,
    ) : this(database = database::database, settings = settings)

    companion object {
        const val MAX_CHARACTER_COUNT = 1000
    }

    suspend fun artistNotes(artistId: String, year: DataYear) =
        notesDao().getArtistNotes(artistId, year)
            .asFlow()
            .mapToOneOrNull(PlatformDispatchers.IO)

    suspend fun getArtistNotes(artistId: String, year: DataYear) =
        notesDao().getArtistNotes(artistId, year)
            .awaitAsOneOrNull()

    suspend fun stampRallyNotes(stampRallyId: String) =
        notesDao().getStampRallyNotes(stampRallyId)
            .asFlow()
            .mapToOneOrNull(PlatformDispatchers.IO)

    suspend fun getStampRallyNotes(stampRallyId: String) =
        notesDao().getStampRallyNotes(stampRallyId).awaitAsOneOrNull()

    suspend fun updateArtistNotes(artistId: String, year: DataYear, notes: String?) {
        PersistentStorageRequester.requestPersistent()
        notesDao().run {
            transaction {
                this@run.insertArtistNotes(
                    ArtistNotes(
                        artistId = artistId,
                        dataYear = year,
                        notes = notes?.take(MAX_CHARACTER_COUNT),
                        generation = (settings.lastSyncGeneration ?: 0) + 1,
                        updatedAt = clock.now(),
                    )
                )
            }
        }
    }

    suspend fun updateStampRallyNotes(stampRallyId: String, notes: String?) {
        PersistentStorageRequester.requestPersistent()
        notesDao().run {
            transaction {
                this@run.insertStampRallyNotes(
                    StampRallyNotes(
                        stampRallyId = stampRallyId,
                        notes = notes?.take(MAX_CHARACTER_COUNT),
                        generation = (settings.lastSyncGeneration ?: 0) + 1,
                        updatedAt = clock.now(),
                    )
                )
            }
        }
    }

    context(_: SuspendingTransactionWithReturn<Unit>)
    suspend fun insertSyncPayload(payload: UserDataSyncPayload) {
        val notesDao = notesDao()
        payload.artistNotes?.forEach {
            val userEntry = notesDao.getArtistNotes(it.artistId.id, it.dataYear).awaitAsOneOrNull()
            val currentUpdatedAt = userEntry?.updatedAt
            val incomingUpdatedAt = it.updatedAt
            if (userEntry == null ||
                userEntry.generation < it.generation ||
                (currentUpdatedAt != null && incomingUpdatedAt != null && currentUpdatedAt < incomingUpdatedAt)
            ) {
                notesDao.insertArtistNotes(
                    ArtistNotes(
                        artistId = it.artistId.id,
                        dataYear = it.dataYear,
                        notes = it.notes ?: userEntry?.notes,
                        generation = it.generation,
                        updatedAt = incomingUpdatedAt,
                    )
                )
            }
        }
    }
}
