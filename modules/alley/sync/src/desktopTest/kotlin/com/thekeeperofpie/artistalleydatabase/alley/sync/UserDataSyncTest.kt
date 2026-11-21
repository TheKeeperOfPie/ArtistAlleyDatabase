package com.thekeeperofpie.artistalleydatabase.alley.sync

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.functions.BackendDatabases
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.RefreshToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user.UserDataHandler
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user.UserId
import com.thekeeperofpie.artistalleydatabase.alley.functions.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.alley.user.User
import com.thekeeperofpie.artistalleydatabase.alley.user.UserArtist
import com.thekeeperofpie.artistalleydatabase.alley.user.UserGeneration
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import io.kotest.matchers.collections.shouldContainAllInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.asClock
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TestTimeSource

private val ORIGIN =
    LocalDate(year = 2026, month = Month.JULY, day = 4).atStartOfDayIn(TimeZone.UTC)
private val USER_ID = UserId("testUserId")

class UserDataSyncTest {

    private val timeSource = TestTimeSource()
    private val clock = timeSource.asClock(ORIGIN)

    private val artistData = ArtistWithUserDataProvider.values
        .mapIndexed { index, data ->
            val [favorite, ignored] = when (index % 3) {
                0 -> true to false
                1 -> true to true
                2 -> false to true
                else -> throw IllegalStateException()
            }
            ArtistData(
                data.copy(
                    userEntry = data.userEntry.copy(
                        favorite = favorite,
                        ignored = ignored
                    )
                )
            )
        }

    @Test
    fun singleDevice_uploadOnce() = runTest {
        val (clientDriver, clientDatabase, serverDatabase, artistData) = createDatabases()
        val settings = Settings()
        val userEntryDao = UserEntryDao(
            driver = { clientDriver },
            database = { clientDatabase },
            settings = settings,
            clock = clock,
        )
        val syncer = AlleyUserDataSyncer(
            userEntryDao = userEntryDao,
            userDataSyncDao = UserDataSyncDao(database = { clientDatabase }),
            settings = settings,
        )

        userEntryDao.insertArtistUserEntry(artistData[0].source.userEntry)
        timeSource += 3.seconds
        userEntryDao.insertArtistUserEntry(artistData[1].source.userEntry)
        timeSource += 5.seconds
        userEntryDao.insertArtistUserEntry(artistData[2].source.userEntry)

        val expectedGeneration = 1L
        val payload = syncer.getPayload()
        payload.artists
            .shouldNotBeNull()
            .shouldContainAllInAnyOrder(
                artistData[0].toPayload(ORIGIN, generation = expectedGeneration),
                artistData[1].toPayload(ORIGIN + 3.seconds, generation = expectedGeneration),
                artistData[2].toPayload(ORIGIN + 3.seconds + 5.seconds, generation = expectedGeneration),
            )

        val reconcilePayload =
            UserDataHandler.syncFromClient(clock, USER_ID, payload, serverDatabase)
        reconcilePayload.lastSyncGeneration.shouldBe(expectedGeneration)
        reconcilePayload.artists.shouldContainAllInAnyOrder(artistData.map {
            it.toPayload(ORIGIN + 8.seconds, expectedGeneration)
        })

        serverDatabase.testQueries.getAllUserArtists(USER_ID).awaitAsList()
            .shouldContainAllInAnyOrder(
                artistData.map {
                    it.toServerModel(USER_ID, ORIGIN + 8.seconds, expectedGeneration)
                }
            )
    }

    @Test
    fun singleDevice_downloadOnce() = runTest {
        val (clientDriver, clientDatabase, serverDatabase, artistData) = createDatabases()
        val settings = Settings()
        val userEntryDao = UserEntryDao(
            driver = { clientDriver },
            database = { clientDatabase },
            clock = clock,
            settings = settings,
        )
        val syncer = AlleyUserDataSyncer(
            userEntryDao = userEntryDao,
            userDataSyncDao = UserDataSyncDao(database = { clientDatabase }),
            settings = settings,
        )

        artistData.map { it.toServerModel(USER_ID, ORIGIN + 3.seconds, 1) }
            .forEach { serverDatabase.userArtistQueries.insertUserArtist(it) }

        val expectedGeneration = 1L
        val payload =
            UserDataHandler.syncFromClient(clock, USER_ID, UserDataSyncPayload(), serverDatabase)
        payload.lastSyncGeneration.shouldBe(expectedGeneration)
        payload.artists
            .shouldContainAllInAnyOrder(
                artistData.map {
                    it.toPayload(ORIGIN + 3.seconds, expectedGeneration)
                }
            )

        syncer.syncFromServer(payload)

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainAllInAnyOrder(
                artistData.map {
                    it.source.userEntry.copy(
                        generation = 1L,
                        updatedAt = ORIGIN + 3.seconds,
                    )
                }
            )
    }

    @Test
    fun singleDevice_uploadExclusiveAndDownloadReconciled() = runTest {
        val (clientDriver, clientDatabase, serverDatabase, artistData) =
            createDatabases(artistData = artistData.take(6).toList())

        val settings = Settings()
        val userEntryDao = UserEntryDao(
            driver = { clientDriver },
            database = { clientDatabase },
            clock = clock,
            settings = settings,
        )
        val syncer = AlleyUserDataSyncer(
            userEntryDao = userEntryDao,
            userDataSyncDao = UserDataSyncDao(database = { clientDatabase }),
            settings = settings,
        )

        val artistDataClient = artistData.take(3)
        val artistDataServer = artistData.drop(3)

        timeSource += 3.seconds
        artistDataClient.forEach { userEntryDao.insertArtistUserEntry(it.source.userEntry) }
        artistDataServer.forEach {
            serverDatabase.userArtistQueries.insertUserArtist(
                it.toServerModel(
                    USER_ID,
                    ORIGIN - 3.seconds,
                    1L
                )
            )
        }
        serverDatabase.generationQueries.incrementUserGeneration(USER_ID).awaitAsOne()
            .shouldBe(1L)

        val clientPayload = syncer.getPayload()
        clientPayload.lastSyncGeneration.shouldBe(0L)
        clientPayload.artists.shouldContainAllInAnyOrder(
            artistDataClient.map { it.toPayload(ORIGIN + 3.seconds, 1L) }
        )

        val expectedGeneration = 2L
        val payload =
            UserDataHandler.syncFromClient(clock, USER_ID, syncer.getPayload(), serverDatabase)
        payload.lastSyncGeneration.shouldBe(expectedGeneration)
        payload.artists
            .shouldContainAllInAnyOrder(
                artistDataClient.map {
                    it.toPayload(ORIGIN + 3.seconds, expectedGeneration)
                } + artistDataServer.map {
                    it.toPayload(ORIGIN - 3.seconds, 1L)
                }
            )

        syncer.syncFromServer(payload)

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainAllInAnyOrder(
                artistDataClient.map {
                    it.source.userEntry.copy(
                        generation = expectedGeneration,
                        updatedAt = ORIGIN + 3.seconds,
                    )
                } + artistDataServer.map {
                    it.source.userEntry.copy(
                        generation = 1L,
                        updatedAt = ORIGIN - 3.seconds,
                    )
                }
            )
    }

    private suspend fun createDatabases(
        artistData: List<ArtistData> = this.artistData.take(3).toList(),
    ): TestData {
        val clientDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        return TestData(
            clientDriver = clientDriver,
            clientDatabase = createClientDatabase(clientDriver, artistData),
            serverDatabase = createServerDatabase(),
            artistData = artistData,
        )
    }

    private suspend fun createClientDatabase(
        driver: SqlDriver,
        artistData: List<ArtistData>,
    ): AlleySqlDatabase {
        AlleySqlDatabase.Schema.create(driver).await()
        return DaoUtils.createAlleySqlDatabase(driver).apply {
            artistData.forEach {
                testQueries.insertArtist(it.source.artist.databaseEntry.toArtistEntry(it.source.artist.year))
            }
        }
    }

    private suspend fun createServerDatabase(): AlleyUserDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AlleyUserDatabase.Schema.create(driver).await()
        return BackendDatabases.userDatabase(driver).apply {
            userQueries.provisionUser(
                User(
                    userId = USER_ID,
                    refreshToken = RefreshToken("testRefreshToken"),
                    createdAt = ORIGIN,
                    deviceIdentifier = null,
                )
            )
            generationQueries.provisionGeneration(UserGeneration(USER_ID, 0))
        }
    }

    private data class TestData(
        val clientDriver: SqlDriver,
        val clientDatabase: AlleySqlDatabase,
        val serverDatabase: AlleyUserDatabase,
        val artistData: List<ArtistData>,
    )

    private data class ArtistData(val source: ArtistWithUserData) {

        fun toPayload(updatedAt: Instant, generation: Long) = UserDataSyncPayload.Artist(
            generation = generation,
            dataYear = source.artist.year,
            id = source.artist.id,
            favorite = source.userEntry.favorite,
            ignored = source.userEntry.ignored,
            updatedAt = updatedAt,
        )

        fun toServerModel(userId: UserId, updatedAt: Instant, generation: Long) = UserArtist(
            userId = userId,
            dataYear = source.artist.year,
            artistId = source.artist.id,
            favorite = source.userEntry.favorite,
            ignored = source.userEntry.ignored,
            updatedAt = updatedAt,
            generation = generation,
        )
    }

    private class Settings : AlleyUserSettings {
        override var pendingUserAuthCode: String? by mutableStateOf(null)
        override var lastSyncGeneration: Long? by mutableStateOf(null)
    }
}
