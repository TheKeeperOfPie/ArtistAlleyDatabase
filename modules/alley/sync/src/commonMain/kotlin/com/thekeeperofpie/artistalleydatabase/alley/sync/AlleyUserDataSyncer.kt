package com.thekeeperofpie.artistalleydatabase.alley.sync

import com.thekeeperofpie.artistalleydatabase.alley.database.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.EncryptedArtistId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.EncryptedStampRallyId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload

class AlleyUserDataSyncer(
    private val userDataSyncDao: UserDataSyncDao,
    private val userEntryDao: UserEntryDao,
    private val userNotesDao: UserNotesDao,
    private val database: () -> AlleySqlDatabase,
    private val settings: AlleyUserSettings,
) {
    suspend fun getPayload(): UserDataSyncPayload {
        val lastSyncGeneration = settings.lastSyncGeneration ?: 0L
        val payload = userDataSyncDao.getPayload(lastSyncGeneration)
        return UserDataSyncPayload(
            generation = lastSyncGeneration,
            artists = payload.artistChangesSince.map {
                UserDataSyncPayload.Artist(
                    generation = it.generation,
                    dataYear = it.dataYear,
                    id = it.artistId,
                    favorite = it.favorite,
                    ignored = it.ignored,
                    updatedAt = it.updatedAt,
                )
            },
            artistNotes = payload.artistNotesChangesSince.map {
                UserDataSyncPayload.ArtistNotes(
                    generation = it.generation,
                    dataYear = it.dataYear,
                    artistId = EncryptedArtistId(it.artistId),
                    notes = it.notes,
                    updatedAt = it.updatedAt,
                )
            },
            rallies = payload.rallyChangesSince.map {
                UserDataSyncPayload.StampRally(
                    generation = it.generation,
                    id = it.stampRallyId,
                    favorite = it.favorite,
                    ignored = it.ignored,
                    updatedAt = it.updatedAt,
                )
            },
            rallyNotes = payload.rallyNotesChangesSince.map {
                UserDataSyncPayload.StampRallyNotes(
                    generation = it.generation,
                    stampRallyId = EncryptedStampRallyId(it.stampRallyId),
                    notes = it.notes,
                    updatedAt = it.updatedAt,
                )
            },
            series = payload.seriesChangesSince.map {
                UserDataSyncPayload.Series(
                    generation = it.generation,
                    id = it.seriesId,
                    favorite = it.favorite,
                    updatedAt = it.updatedAt,
                )
            },
            merch = payload.merchChangesSince.map {
                UserDataSyncPayload.Merch(
                    generation = it.generation,
                    id = it.merchId,
                    favorite = it.favorite,
                    updatedAt = it.updatedAt,
                )
            },
        )
    }

    suspend fun syncFromServer(status: Short, syncPayload: UserDataSyncPayload) {
        // On a 409 Conflict, the server rejected client changes, preserve them by
        // rebasing them, incrementing their generation above the server one. Further down,
        // any values inside the payload which are higher generation than the local copy
        // or have a later updatedAt will overwrite anything rebased here
        if (status == 409.toShort()) {
            database().generationQueries.rebaseGeneration(
                serverGeneration = syncPayload.generation ?: 0,
                lastSyncGeneration = settings.lastSyncGeneration ?: 0
            ).await()
        }
        database().transactionWithResult {
            userEntryDao.insertSyncPayload(syncPayload)
            userNotesDao.insertSyncPayload(syncPayload)

            afterCommit {
                // TODO: Also schedule a sync if there's anything remaining unsynced
                settings.lastSyncGeneration = syncPayload.generation
            }
        }
    }
}
