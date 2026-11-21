package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

private fun GetBoothsWithFavorites.toBoothWithFavorite() =
    BoothWithFavorite(
        year = dataYear,
        id = id.toString(),
        booth = booth,
        name = name,
        images = images,
        fallbackImageYear = fallbackImageYear,
        tempImages = tempImages.orEmpty(),
        embeds = embeds.orEmpty(),
        favorite = DaoUtils.coerceBooleanForJs(favorite),
        hasNotes = !notes.isNullOrBlank(),
    )

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
class UserEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val settings: AlleyUserSettings,
    private val artistUserEntryDao: suspend () -> ArtistUserEntryQueries = { database().artistUserEntryQueries },
    private val rallyUserEntryDao: suspend () -> StampRallyUserEntryQueries = { database().stampRallyUserEntryQueries },
    private val seriesUserEntryDao: suspend () -> SeriesUserEntryQueries = { database().seriesUserEntryQueries },
    private val merchUserEntryDao: suspend () -> MerchUserEntryQueries = { database().merchUserEntryQueries },
    private val clock: Clock = Clock.System,
) {
    companion object {
        private val NOTIFY_DELAY = 350.milliseconds
    }

    @Inject
    constructor(database: ArtistAlleyDatabase, settings: AlleyUserSettings) : this(
        driver = database::driver,
        database = database::database,
        settings = settings,
    )

    fun getArtistFavorites(dataYear: DataYear) = flowFromSuspend(artistUserEntryDao)
        .flatMapLatest { it.getArtistFavorites(dataYear).asFlow() }
        .mapToList(PlatformDispatchers.IO)

    fun getAllArtistFavorites() = flowFromSuspend(artistUserEntryDao)
        .flatMapLatest { it.getAllArtistFavorites().asFlow() }
        .mapToList(PlatformDispatchers.IO)

    fun getBoothsWithFavorites(year: DataYear) = flowFromSuspend(artistUserEntryDao)
        .flatMapLatest { it.getBoothsWithFavorites(year).asFlow() }
        .mapToList(PlatformDispatchers.IO)
        .map { it.map { it.toBoothWithFavorite() } }

    suspend fun insertArtistUserEntry(entry: ArtistUserEntry) {
        PersistentStorageRequester.requestPersistent()
        artistUserEntryDao().run {
            transaction {
                val existing =
                    getArtistUserEntry(entry.artistId, entry.dataYear).awaitAsOneOrNull() ?: entry
                val newEntry = existing.copy(
                    favorite = entry.favorite,
                    ignored = entry.ignored,
                    generation = (settings.lastSyncGeneration ?: 0) + 1,
                    updatedAt = clock.now(),
                )
                insertArtistUserEntry(newEntry)
            }
            delay(NOTIFY_DELAY)
            driver().notifyListeners("artistEntry", "artistUserEntry")
        }
    }

    suspend fun insertStampRallyUserEntry(entry: StampRallyUserEntry) {
        PersistentStorageRequester.requestPersistent()
        rallyUserEntryDao().run {
            transaction {
                val existing = getStampRallyUserEntry(entry.stampRallyId).awaitAsOneOrNull()
                    ?: entry
                val newEntry = existing.copy(
                    favorite = entry.favorite,
                    ignored = entry.ignored,
                    generation = (settings.lastSyncGeneration ?: 0) + 1,
                    updatedAt = clock.now(),
                )
                insertStampRallyUserEntry(newEntry)
            }
            delay(NOTIFY_DELAY)
            driver().notifyListeners("stampRallyEntry", "stampRallyUserEntry")
        }
    }

    suspend fun insertSeriesUserEntry(entry: SeriesUserEntry) {
        PersistentStorageRequester.requestPersistent()
        seriesUserEntryDao().run {
            transaction {
                val existing = getSeriesUserEntry(entry.seriesId).awaitAsOneOrNull()
                    ?: entry
                val newEntry = existing.copy(
                    favorite = entry.favorite,
                    generation = (settings.lastSyncGeneration ?: 0) + 1,
                    updatedAt = clock.now(),
                )
                insertSeriesUserEntry(newEntry)
            }
            delay(NOTIFY_DELAY)
            driver().notifyListeners("seriesEntry", "seriesUserEntry")
        }
    }

    suspend fun insertMerchUserEntry(entry: MerchUserEntry) {
        PersistentStorageRequester.requestPersistent()
        merchUserEntryDao().run {
            transaction {
                val existing = getMerchUserEntry(entry.merchId).awaitAsOneOrNull()
                    ?: entry
                val newEntry = existing.copy(
                    favorite = entry.favorite,
                    generation = (settings.lastSyncGeneration ?: 0) + 1,
                    updatedAt = clock.now(),
                )
                insertMerchUserEntry(newEntry)
            }
            delay(NOTIFY_DELAY)
            driver().notifyListeners("merchEntry", "merchUserEntry")
        }
    }

    suspend fun insertSyncPayload(payload: UserDataSyncPayload) {
        database().transactionWithResult {
            val artistUserEntryDao = artistUserEntryDao()
            payload.artists?.forEach {
                val userEntry =
                    artistUserEntryDao.getArtistUserEntry(it.id, it.dataYear).awaitAsOneOrNull()
                if (userEntry == null || userEntry.generation <= it.generation) {
                    artistUserEntryDao.insertArtistUserEntry(
                        ArtistUserEntry(
                            artistId = it.id,
                            dataYear = it.dataYear,
                            favorite = it.favorite ?: userEntry?.favorite ?: false,
                            ignored = it.ignored ?: userEntry?.ignored ?: false,
                            generation = it.generation,
                            updatedAt = it.updatedAt,
                        )
                    )
                }
            }
        }
    }

    fun getTagFavorites() = combine(
        getSeriesFavorites(),
        getMerchFavorites(),
        ::TagFavorites,
    )

    fun getSeriesFavorites() = flowFromSuspend(seriesUserEntryDao)
        .flatMapLatest {
            it.getSeriesFavorites().asFlow()
                .mapLatest { it.awaitAsList().mapNotNull { it.id }.toSet() }
        }

    fun getMerchFavorites() = flowFromSuspend(merchUserEntryDao)
        .flatMapLatest {
            it.getMerchFavorites().asFlow()
                .mapLatest { it.awaitAsList().mapNotNull { it.name }.toSet() }
        }

    fun getRallyFavorites() = flowFromSuspend(rallyUserEntryDao)
        .flatMapLatest {
            it.getStampRallyFavorites().asFlow()
                .mapLatest { it.awaitAsList().toSet() }
        }

    data class TagFavorites(
        val seriesIds: Set<String>,
        val merchIds: Set<String>,
    )
}
