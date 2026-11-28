package com.thekeeperofpie.artistalleydatabase.alley.sync

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.GenerationQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.UserDataSyncQueries
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
class UserDataSyncDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val generationDao: suspend () -> GenerationQueries = { database().generationQueries },
    private val syncDao: suspend () -> UserDataSyncQueries = { database().userDataSyncQueries },
) {
    @Inject
    constructor(database: ArtistAlleyDatabase) : this(database = database::database)

    suspend fun getPayload(lastSyncGeneration: Long): SyncPayload? {
        val syncDao = syncDao()
        val generationDao = generationDao()
        return syncDao.transactionWithResult {
            val lastGeneration = generationDao.getLastGeneration().awaitAsOneOrNull()?.lastGeneration
            if (lastSyncGeneration == lastGeneration) return@transactionWithResult null
            SyncPayload(
                lastGeneration = lastGeneration,
                artistChangesSince = syncDao.getArtistChangesSince(lastSyncGeneration).awaitAsList(),
                artistNotesChangesSince = syncDao.getArtistNotesChangesSince(lastSyncGeneration).awaitAsList(),
                rallyChangesSince = syncDao.getStampRallyChangesSince(lastSyncGeneration).awaitAsList(),
                rallyNotesChangesSince = syncDao.getStampRallyNotesChangesSince(lastSyncGeneration).awaitAsList(),
                seriesChangesSince = syncDao.getSeriesChangesSince(lastSyncGeneration).awaitAsList(),
                merchChangesSince = syncDao.getMerchChangesSince(lastSyncGeneration).awaitAsList(),
            )
        }
    }

    data class SyncPayload(
        val lastGeneration: Long?,
        val artistChangesSince: List<ArtistUserEntry>,
        val artistNotesChangesSince: List<ArtistNotes>,
        val rallyChangesSince: List<StampRallyUserEntry>,
        val rallyNotesChangesSince: List<StampRallyNotes>,
        val seriesChangesSince: List<SeriesUserEntry>,
        val merchChangesSince: List<MerchUserEntry>,
    )
}
