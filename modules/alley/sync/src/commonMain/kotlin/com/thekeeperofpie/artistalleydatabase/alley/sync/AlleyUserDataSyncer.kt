package com.thekeeperofpie.artistalleydatabase.alley.sync

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.snapshotFlow
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserAuth
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.EncryptedArtistId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.EncryptedStampRallyId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.ExperimentalMetroApi
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.OnGraphInit
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@ContributesTo(AppScope::class)
interface AlleyUserDataSyncerInitializer {

    @OptIn(ExperimentalMetroApi::class)
    @OnGraphInit
    fun initialize(syncer: AlleyUserDataSyncer) {
        syncer.initialize()
    }
}

@SingleIn(AppScope::class)
class AlleyUserDataSyncer(
    private val applicationScope: ApplicationScope,
    private val database: suspend () -> AlleySqlDatabase,
    private val userDataSyncDao: UserDataSyncDao,
    private val userEntryDao: UserEntryDao,
    private val userNotesDao: UserNotesDao,
    private val userAuth: AlleyUserAuth,
    private val settings: AlleyUserSettings,
) {
    @Inject
    constructor(
        applicationScope: ApplicationScope,
        artistAlleyDatabase: ArtistAlleyDatabase,
        userDataSyncDao: UserDataSyncDao,
        userEntryDao: UserEntryDao,
        userNotesDao: UserNotesDao,
        userAuth: AlleyUserAuth,
        settings: AlleyUserSettings,
    ): this(
        applicationScope = applicationScope,
        database = artistAlleyDatabase::database,
        userDataSyncDao = userDataSyncDao,
        userEntryDao = userEntryDao,
        userNotesDao = userNotesDao,
        userAuth = userAuth,
        settings = settings,
    )

    internal fun initialize() {
        applicationScope.launch {
            snapshotFlow { userAuth.isLoggedIn }
                .flatMapLatest {
                    if (!it) return@flatMapLatest emptyFlow()
                    database().generationQueries.getLastGeneration()
                        .asFlow()
                        .mapToOneOrNull(PlatformDispatchers.IO)
                        .filterNotNull()
                        .debounce(5.seconds)
                        .conflate()
                }
                .collectLatest { syncUserData() }
        }
    }

    private suspend fun syncUserData(): Unit = try {
        val clientPayload = getPayload()
        ConsoleLogger.log("Syncing ${clientPayload.size} changes to server")
        val [status, response] = userAuth.sync(clientPayload) ?: return
        val serverPayload = response.payload
        ConsoleLogger.log("Syncing ${serverPayload.size} changes from server")
        syncFromServer(status.value.toShort(), serverPayload)
    } catch (t: Throwable) {
        currentCoroutineContext().ensureActive()
        // TODO: Surface errors?
        t.printStackTrace()
    }

    @VisibleForTesting
    suspend fun getPayload(): UserDataSyncPayload {
        val lastSyncGeneration = settings.lastSyncGeneration ?: 0L
        val payload = userDataSyncDao.getPayload(lastSyncGeneration)
            ?: return UserDataSyncPayload() // If nothing changed, return empty
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

    @VisibleForTesting
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
