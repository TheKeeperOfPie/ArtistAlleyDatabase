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
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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

        val expectedServerGeneration = 1L
        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 0L,
            expectedPayloadArtists = listOf(
                artistData[0].toPayload(ORIGIN, expectedServerGeneration),
                artistData[1].toPayload(ORIGIN + 3.seconds, expectedServerGeneration),
                artistData[2].toPayload(ORIGIN + 8.seconds, expectedServerGeneration),
            ),
            expectedHttpStatus = 200,
            expectedServerGeneration = 1L,
            expectedServerPayloadArtists = listOf(
                artistData[0].toPayload(ORIGIN, expectedServerGeneration),
                artistData[1].toPayload(ORIGIN + 3.seconds, expectedServerGeneration),
                artistData[2].toPayload(ORIGIN + 8.seconds, expectedServerGeneration),
            ),
        )

        serverDatabase.testQueries.getAllUserArtists(USER_ID).awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artistData[0].toServerModel(USER_ID, ORIGIN, expectedServerGeneration),
                artistData[1].toServerModel(USER_ID, ORIGIN + 3.seconds, expectedServerGeneration),
                artistData[2].toServerModel(USER_ID, ORIGIN + 8.seconds, expectedServerGeneration),
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
        serverDatabase.generationQueries.incrementUserGeneration(USER_ID).awaitAsOne()

        val expectedGeneration = 1L

        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 0L,
            expectedPayloadArtists = emptyList(),
            expectedHttpStatus = 409,
            expectedServerGeneration = expectedGeneration,
            expectedServerPayloadArtists = artistData.map {
                it.toPayload(ORIGIN + 3.seconds, expectedGeneration)
            },
        )

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
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

        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 0L,
            expectedPayloadArtists = artistDataClient.map {
                it.toPayload(ORIGIN + 3.seconds, 1L)
            },
            expectedHttpStatus = 409,
            expectedServerGeneration = 1L,
            expectedServerPayloadArtists = artistDataServer.map {
                it.toPayload(ORIGIN - 3.seconds, 1L)
            },
        )

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artistDataClient.map {
                    it.source.userEntry.copy(
                        generation = 2L,
                        updatedAt = ORIGIN + 3.seconds,
                    )
                } + artistDataServer.map {
                    it.source.userEntry.copy(
                        generation = 1L,
                        updatedAt = ORIGIN - 3.seconds,
                    )
                }
            )

        // TODO: When re-schedule sync is added, assert that a new client sync is sent to server

        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 1L,
            expectedPayloadArtists = artistDataClient.map {
                it.toPayload(updatedAt = ORIGIN + 3.seconds, generation = 2L)
            },
            expectedHttpStatus = 200,
            expectedServerGeneration = 2L,
            expectedServerPayloadArtists = artistDataClient.map {
                it.toPayload(ORIGIN + 3.seconds, 2L)
            },
        )

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artistDataClient.map {
                    it.source.userEntry.copy(
                        generation = 2L,
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

    @Test
    fun singleDevice_uploadConflict_lastWins() = runTest {
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

        val artist = artistData.first().toEdited(favorite = false, ignored = false)

        // Insert a local non-favorited artist
        userEntryDao.insertArtistUserEntry(artist.source.userEntry)

        // Insert a higher generation on the server which favorites the artist
        serverDatabase.userArtistQueries.insertUserArtist(
            artist.toEdited(favorite = true, ignored = false)
                .toServerModel(USER_ID, ORIGIN + 3.seconds, 2L)
        )
        serverDatabase.generationQueries.incrementUserGeneration(USER_ID).awaitAsOne()
        serverDatabase.generationQueries.incrementUserGeneration(USER_ID).awaitAsOne()
            .shouldBe(2L)

        timeSource += 3.seconds

        // Upload a diff that the server has a higher generation for (2L)
        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 0L,
            expectedPayloadArtists = listOf(artist.toPayload(ORIGIN, 1L)),
            // Fails, rejecting the diff, requiring the client to rebase
            expectedHttpStatus = 409,
            expectedServerGeneration = 2L,
            expectedServerPayloadArtists = listOf(
                artist.toEdited(favorite = true, ignored = false)
                    .toPayload(ORIGIN + 3.seconds, 2L)
            ),
        )

        // Assert the client now has the artist favorited
        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artist.toEdited(favorite = true, ignored = false)
                    .source.userEntry.copy(updatedAt = ORIGIN + 3.seconds, generation = 2L)
            )

        timeSource += 3.seconds

        // Have the client mark the artist ignored and push a newer update to the server
        // Insert multiple times to ensure generation only shifts once to 3
        arrayOf(
            artist.toEdited(favorite = true, ignored = true),
            artist.toEdited(favorite = false, ignored = false),
            artist.toEdited(favorite = true, ignored = true),
        ).forEach {
            userEntryDao.insertArtistUserEntry(it.source.userEntry)
        }

        timeSource += 3.seconds

        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 2L,
            expectedPayloadArtists = listOf(
                artist.toEdited(favorite = true, ignored = true)
                    .toPayload(ORIGIN + 6.seconds, 3L)
            ),
            expectedHttpStatus = 200,
            expectedServerGeneration = 3L,
            expectedServerPayloadArtists = listOf(
                artist.toEdited(favorite = true, ignored = true)
                    .toPayload(ORIGIN + 6.seconds, 3L)
            ),
        )

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artist.toEdited(favorite = true, ignored = true)
                    .source.userEntry.copy(updatedAt = ORIGIN + 6.seconds, generation = 3L)
            )
    }

    @Test
    fun singleDevice_uploadConflict_keepsHigherLocalGeneration() = runTest {
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

        val artist1 = artistData[0].toEdited(favorite = false, ignored = true)
        val artist2 = artistData[1].toEdited(favorite = false, ignored = true)

        arrayOf(artist1, artist2).forEach {
            userEntryDao.insertArtistUserEntry(it.source.userEntry)
        }

        // Insert a higher generation on the server which favorites artists
        arrayOf(artist1, artist2).forEach {
            serverDatabase.userArtistQueries.insertUserArtist(
                it.toEdited(favorite = true, ignored = true)
                    .toServerModel(USER_ID, ORIGIN + 3.seconds, 2L)
            )
        }

        serverDatabase.generationQueries.incrementUserGeneration(USER_ID).awaitAsOne()
        serverDatabase.generationQueries.incrementUserGeneration(USER_ID).awaitAsOne()
            .shouldBe(2L)

        timeSource += 3.seconds

        // This doesn't call sendClientPayloadToServerAndSync in order to intercept before the
        // server response gets applied to the client
        val clientPayload = syncer.getPayload()
        clientPayload.artists.shouldContainExactlyInAnyOrder(
            artist1.toPayload(ORIGIN, 1L),
            artist2.toPayload(ORIGIN, 1L),
        )

        // Upload a diff and return a payload updating both locally
        val [httpStatus1, reconcilePayload1] =
            UserDataHandler.syncFromClient(clock, USER_ID, clientPayload, serverDatabase)
        httpStatus1.shouldBe(409)
        reconcilePayload1.generation.shouldBe(2L)
        reconcilePayload1.artists.shouldContainExactlyInAnyOrder(
            artist1.toEdited(favorite = true, ignored = true)
                .toPayload(ORIGIN + 3.seconds, 2L),
            artist2.toEdited(favorite = true, ignored = true)
                .toPayload(ORIGIN + 3.seconds, 2L),
        )

        // Pretending that the user edited something before the server returned,
        // update artist2 locally to disagree with the server
        timeSource += 3.seconds
        userEntryDao.insertArtistUserEntry(
            artist2.toEdited(favorite = true, ignored = false).source.userEntry
        )

        // Assert the client ignored the artist2 update from the server
        syncer.syncFromServer(httpStatus1, reconcilePayload1)
        settings.lastSyncGeneration.shouldBe(2L)
        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artist1.toEdited(favorite = true, ignored = true)
                    .source.userEntry.copy(updatedAt = ORIGIN + 3.seconds, generation = 2L),
                artist2.toEdited(favorite = true, ignored = false)
                    .source.userEntry.copy(updatedAt = ORIGIN + 6.seconds, generation = 3L)
            )

        // TODO: When re-schedule sync is added, assert that a new client sync is sent to server

        // Try to re-upload artist2 to the server
        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 2L,
            expectedPayloadArtists = listOf(
                artist2.toEdited(favorite = true, ignored = false)
                    .toPayload(updatedAt = ORIGIN + 6.seconds, generation = 3L)
            ),
            expectedHttpStatus = 200,
            expectedServerGeneration = 3L,
            expectedServerPayloadArtists = listOf(
                artist2.toEdited(favorite = true, ignored = false)
                    .toPayload(ORIGIN + 6.seconds, 3L),
            )
        )

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artist1.toEdited(favorite = true, ignored = true)
                    .source.userEntry.copy(updatedAt = ORIGIN + 3.seconds, generation = 2L),
                artist2.toEdited(favorite = true, ignored = false)
                    .source.userEntry.copy(updatedAt = ORIGIN + 6.seconds, generation = 3L)
            )

        serverDatabase.testQueries.getAllUserArtists(USER_ID).awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artist1.toEdited(favorite = true, ignored = true)
                    .toServerModel(USER_ID, updatedAt = ORIGIN + 3.seconds, generation = 2L),
                artist2.toEdited(favorite = true, ignored = false)
                    .toServerModel(USER_ID, updatedAt = ORIGIN + 6.seconds, generation = 3L)
            )
    }

    @Test
    fun singleDevice_syncButServerWasReset() = runTest {
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

        timeSource += 3.seconds

        // Client outpaces server
        settings.lastSyncGeneration = 10L
        artistDataClient.forEach { userEntryDao.insertArtistUserEntry(it.source.userEntry) }

        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 10L,
            expectedPayloadArtists = artistDataClient.map { it.toPayload(ORIGIN + 3.seconds, 11L) },
            expectedHttpStatus = 409,
            expectedServerGeneration = 1L,
            expectedServerPayloadArtists = artistDataServer.map {
                it.toPayload(ORIGIN - 3.seconds, 1L)
            },
        )

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artistDataClient.map {
                    it.source.userEntry.copy(
                        generation = 2L,
                        updatedAt = ORIGIN + 3.seconds,
                    )
                } + artistDataServer.map {
                    it.source.userEntry.copy(
                        generation = 1L,
                        updatedAt = ORIGIN - 3.seconds,
                    )
                }
            )

        // TODO: When re-schedule sync is added, assert that a new client sync is sent to server

        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 1L,
            expectedPayloadArtists = artistDataClient.map { it.toPayload(ORIGIN + 3.seconds, 2L) },
            expectedHttpStatus = 200,
            expectedServerGeneration = 2L,
            expectedServerPayloadArtists = artistDataClient.map {
                it.toPayload(ORIGIN + 3.seconds, 2L)
            },
        )

        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artistDataClient.map {
                    it.source.userEntry.copy(
                        generation = 2L,
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

    @Test
    fun singleDevice_uploadOutdated_preservesServerValue() = runTest {
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

        val artist = artistData.first()

        // In the future relative to client, make an edit on the server
        serverDatabase.userArtistQueries.insertUserArtist(
            artist.toEdited(favorite = false, ignored = true)
                .toServerModel(USER_ID, ORIGIN + 3.seconds, 1L)
        )
        serverDatabase.generationQueries.incrementUserGeneration(USER_ID).awaitAsOne()
            .shouldBe(1L)

        // Create a client conflict
        settings.lastSyncGeneration = 1L
        userEntryDao.insertArtistUserEntry(
            artist.toEdited(favorite = true, ignored = true).source.userEntry
        )

        syncer.sendClientPayloadToServerAndSync(
            serverDatabase = serverDatabase,
            expectedClientGeneration = 1L,
            expectedPayloadArtists = listOf(
                artist.toEdited(favorite = true, ignored = true)
                    .toPayload(ORIGIN, 2L)
            ),
            expectedHttpStatus = 200,
            expectedServerGeneration = 2L,
            expectedServerPayloadArtists = listOf(
                artist.toEdited(favorite = false, ignored = true)
                    .toPayload(ORIGIN + 3.seconds, 2L)
            ),
        )

        // Client should be up to date with server
        clientDatabase.testQueries.getArtistUserEntries().awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artist.toEdited(favorite = false, ignored = true)
                    .source.userEntry.copy(updatedAt = ORIGIN + 3.seconds, generation = 2L)
            )

        // Server should have generation bumped but preserve state
        serverDatabase.testQueries.getAllUserArtists(USER_ID).awaitAsList()
            .shouldContainExactlyInAnyOrder(
                artist.toEdited(favorite = false, ignored = true)
                    .toServerModel(USER_ID, ORIGIN + 3.seconds, 2L)
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

    private suspend fun AlleyUserDataSyncer.sendClientPayloadToServerAndSync(
        serverDatabase: AlleyUserDatabase,
        expectedClientGeneration: Long,
        expectedPayloadArtists: List<UserDataSyncPayload.Artist>,
        expectedHttpStatus: Short,
        expectedServerGeneration: Long,
        expectedServerPayloadArtists: List<UserDataSyncPayload.Artist>,
    ) {
        val clientPayload = getPayload()
        clientPayload.generation.shouldBe(expectedClientGeneration)
        clientPayload.artists.shouldContainExactlyInAnyOrder(expectedPayloadArtists)

        val [httpStatus, serverPayload] =
            UserDataHandler.syncFromClient(clock, USER_ID, clientPayload, serverDatabase)
        httpStatus.shouldBe(expectedHttpStatus)
        serverPayload.generation.shouldBe(expectedServerGeneration)
        serverPayload.artists.shouldContainExactlyInAnyOrder(expectedServerPayloadArtists)

        syncFromServer(httpStatus, serverPayload)
    }

    private data class TestData(
        val clientDriver: SqlDriver,
        val clientDatabase: AlleySqlDatabase,
        val serverDatabase: AlleyUserDatabase,
        val artistData: List<ArtistData>,
    )

    private data class ArtistData(val source: ArtistWithUserData) {

        fun toEdited(favorite: Boolean, ignored: Boolean) = copy(
            source = source.copy(
                userEntry = source.userEntry.copy(favorite = favorite, ignored = ignored)
            )
        )

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
