package com.thekeeperofpie.artistalleydatabase.alley.sync

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.toStampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.TestQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.functions.BackendDatabases
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.RefreshToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user.UserDataHandler
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user.UserId
import com.thekeeperofpie.artistalleydatabase.alley.functions.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryProvider
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.User
import com.thekeeperofpie.artistalleydatabase.alley.user.UserArtist
import com.thekeeperofpie.artistalleydatabase.alley.user.UserArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.UserGeneration
import com.thekeeperofpie.artistalleydatabase.alley.user.UserMerch
import com.thekeeperofpie.artistalleydatabase.alley.user.UserSeries
import com.thekeeperofpie.artistalleydatabase.alley.user.UserStampRally
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.EncryptedArtistId
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

    private val artistNotesData = artistData
        .mapIndexed { index, data ->
            ArtistNotesData(
                Triple(
                    data.source.artist,
                    data.source.userEntry,
                    ArtistNotes(
                        artistId = data.source.artist.id,
                        dataYear = data.source.artist.year,
                        notes = "notes-$index",
                        generation = 0,
                        updatedAt = null,
                    ),
                )
            )
        }

    private val stampRallyData = StampRallyWithUserDataProvider.values
        .mapIndexed { index, data ->
            val [favorite, ignored] = when (index % 3) {
                0 -> true to false
                1 -> true to true
                2 -> false to true
                else -> throw IllegalStateException()
            }
            StampRallyData(
                data.copy(
                    userEntry = data.userEntry.copy(
                        favorite = favorite,
                        ignored = ignored
                    )
                )
            )
        }

    private val seriesData = SeriesWithUserDataProvider.values
        .mapIndexed { index, data ->
            val favorite = index % 2 == 0
            SeriesData(data.copy(userEntry = data.userEntry.copy(favorite = favorite)))
        }

    private val merchData = MerchEntryProvider.values
        .mapIndexed { index, data ->
            val favorite = index % 2 == 0
            MerchData(
                MerchWithUserData(
                    merch = data,
                    userEntry = MerchUserEntry(
                        merchId = data.uuid,
                        favorite = favorite,
                        generation = 0L,
                        updatedAt = null,
                    )
                )
            )
        }

    // TODO: Replace with Burst when it supports Kotlin 2.5.0
    @Test
    fun singleDevice_uploadOnce_artists() = runTest {
        singleDevice_uploadOnce(ParametersArtists)
    }

    @Test
    fun singleDevice_uploadOnce_artistNotes() = runTest {
        singleDevice_uploadOnce(ParametersArtistNotes)
    }

    @Test
    fun singleDevice_uploadOnce_rallies() = runTest {
        singleDevice_uploadOnce(ParametersStampRallies)
    }

    @Test
    fun singleDevice_uploadOnce_series() = runTest {
        singleDevice_uploadOnce(ParametersSeries)
    }

    @Test
    fun singleDevice_uploadOnce_merch() = runTest {
        singleDevice_uploadOnce(ParametersMerch)
    }

    private fun <Source, UserEntry, Payload, ServerModel> singleDevice_uploadOnce(parameters: Parameters<Source, UserEntry, Payload, ServerModel>) =
        runTest {
            val (clientDatabase, serverDatabase, userEntryDao, userNotesDao, syncer, settings, data) =
                createDatabases(parameters)

            parameters.insertUserEntry(userEntryDao, userNotesDao, data[0])
            timeSource += 3.seconds
            parameters.insertUserEntry(userEntryDao, userNotesDao, data[1])
            timeSource += 5.seconds
            parameters.insertUserEntry(userEntryDao, userNotesDao, data[2])

            val expectedServerGeneration = 1L
            syncer.sendClientPayloadToServerAndSync2(
                parameters = parameters,
                serverDatabase = serverDatabase,
                expectedClientGeneration = 0L,
                expectedPayload = with(parameters) {
                    listOf(
                        data[0].toPayload(ORIGIN, expectedServerGeneration),
                        data[1].toPayload(ORIGIN + 3.seconds, expectedServerGeneration),
                        data[2].toPayload(ORIGIN + 8.seconds, expectedServerGeneration),
                    )
                },
                expectedHttpStatus = 200,
                expectedServerGeneration = 1L,
                expectedServerPayload = with(parameters) {
                    listOf(
                        data[0].toPayload(ORIGIN, expectedServerGeneration),
                        data[1].toPayload(ORIGIN + 3.seconds, expectedServerGeneration),
                        data[2].toPayload(ORIGIN + 8.seconds, expectedServerGeneration),
                    )
                },
            )

            with(parameters) {
                serverDatabase.getServerEntries()
                    .shouldContainExactlyInAnyOrder(
                        data[0].toServerModel(ORIGIN, expectedServerGeneration),
                        data[1].toServerModel(ORIGIN + 3.seconds, expectedServerGeneration),
                        data[2].toServerModel(ORIGIN + 8.seconds, expectedServerGeneration),
                    )

                clientDatabase.getClientEntries()
                    .shouldContainExactlyInAnyOrder(
                        data[0].toUserEntry(ORIGIN, expectedServerGeneration),
                        data[1].toUserEntry(ORIGIN + 3.seconds, expectedServerGeneration),
                        data[2].toUserEntry(ORIGIN + 8.seconds, expectedServerGeneration),
                    )
            }
        }

    @Test
    fun singleDevice_downloadOnce_artists() = runTest {
        singleDevice_downloadOnce(ParametersArtists)
    }

    @Test
    fun singleDevice_downloadOnce_rallies() = runTest {
        singleDevice_downloadOnce(ParametersStampRallies)
    }

    @Test
    fun singleDevice_downloadOnce_series() = runTest {
        singleDevice_downloadOnce(ParametersSeries)
    }

    @Test
    fun singleDevice_downloadOnce_merch() = runTest {
        singleDevice_downloadOnce(ParametersMerch)
    }

    private fun <Source, UserEntry, Payload, ServerModel> singleDevice_downloadOnce(parameters: Parameters<Source, UserEntry, Payload, ServerModel>) =
        runTest {
            val (clientDatabase, serverDatabase, userEntryDao, userNotesDao, syncer, settings, data) =
                createDatabases(parameters)

            with(parameters) {
                data.map { it.toServerModel(ORIGIN + 3.seconds, 1) }
                    .forEach { serverDatabase.insertServerEntry(it) }
            }
            serverDatabase.generationQueries.incrementUserGeneration(USER_ID).awaitAsOne()

            val expectedGeneration = 1L

            syncer.sendClientPayloadToServerAndSync2(
                parameters = parameters,
                serverDatabase = serverDatabase,
                expectedClientGeneration = 0L,
                expectedPayload = emptyList(),
                expectedHttpStatus = 409,
                expectedServerGeneration = expectedGeneration,
                expectedServerPayload = with(parameters) {
                    data.map { it.toPayload(ORIGIN + 3.seconds, expectedGeneration) }
                },
            )

            with(parameters) {
                clientDatabase.getClientEntries()
                    .shouldContainExactlyInAnyOrder(
                        data.map {
                            it.toUserEntry(ORIGIN + 3.seconds, 1L)
                        }
                    )
            }
        }

    @Test
    fun singleDevice_uploadExclusiveAndDownloadReconciled() = runTest {
        val (clientDatabase, serverDatabase, userEntryDao, userNotesDao, syncer, settings, data) =
            createDatabases(artistData = artistData.take(6).toList())

        val artistDataClient = data.take(3)
        val artistDataServer = data.drop(3)

        timeSource += 3.seconds
        artistDataClient.forEach { userEntryDao.insertArtistUserEntry(it.source.userEntry) }
        artistDataServer.forEach {
            serverDatabase.userArtistQueries.insertUserArtist(
                it.toServerModel(ORIGIN - 3.seconds, 1L)
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
        val (clientDatabase, serverDatabase, userEntryDao, userNotesDao, syncer, settings, data) =
            createDatabases()

        val artist = data.first().toEdited(favorite = false, ignored = false)

        // Insert a local non-favorited artist
        userEntryDao.insertArtistUserEntry(artist.source.userEntry)

        // Insert a higher generation on the server which favorites the artist
        serverDatabase.userArtistQueries.insertUserArtist(
            artist.toEdited(favorite = true, ignored = false)
                .toServerModel(ORIGIN + 3.seconds, 2L)
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
        val (clientDatabase, serverDatabase, userEntryDao, userNotesDao, syncer, settings, data) =
            createDatabases()

        val artist1 = data[0].toEdited(favorite = false, ignored = true)
        val artist2 = data[1].toEdited(favorite = false, ignored = true)

        arrayOf(artist1, artist2).forEach {
            userEntryDao.insertArtistUserEntry(it.source.userEntry)
        }

        // Insert a higher generation on the server which favorites artists
        arrayOf(artist1, artist2).forEach {
            serverDatabase.userArtistQueries.insertUserArtist(
                it.toEdited(favorite = true, ignored = true)
                    .toServerModel(ORIGIN + 3.seconds, 2L)
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
                    .toServerModel(updatedAt = ORIGIN + 3.seconds, generation = 2L),
                artist2.toEdited(favorite = true, ignored = false)
                    .toServerModel(updatedAt = ORIGIN + 6.seconds, generation = 3L)
            )
    }

    @Test
    fun singleDevice_syncButServerWasReset() = runTest {
        val (clientDatabase, serverDatabase, userEntryDao, userNotesDao, syncer, settings, data) =
            createDatabases(artistData = artistData.take(6).toList())

        val artistDataClient = data.take(3)
        val artistDataServer = data.drop(3)

        artistDataServer.forEach {
            serverDatabase.userArtistQueries.insertUserArtist(
                it.toServerModel(ORIGIN - 3.seconds, 1L)
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
        val (clientDatabase, serverDatabase, userEntryDao, userNotesDao, syncer, settings, data) =
            createDatabases()

        val artist = data.first()

        // In the future relative to client, make an edit on the server
        serverDatabase.userArtistQueries.insertUserArtist(
            artist.toEdited(favorite = false, ignored = true)
                .toServerModel(ORIGIN + 3.seconds, 1L)
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
                    .toServerModel(ORIGIN + 3.seconds, 2L)
            )
    }

    private suspend fun createDatabases(
        artistData: List<ArtistData> = this.artistData.take(3).toList(),
    ): TestData<ArtistData> {
        val clientDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val clientDatabase = createClientDatabase(clientDriver, artistData)
        val settings = Settings()
        val userEntryDao = UserEntryDao(
            driver = { clientDriver },
            database = { clientDatabase },
            settings = settings,
            clock = clock,
        )
        val userNotesDao = UserNotesDao(
            database = { clientDatabase },
            settings = settings,
            clock = clock,
        )
        val syncer = AlleyUserDataSyncer(
            userDataSyncDao = UserDataSyncDao(database = { clientDatabase }),
            userEntryDao = userEntryDao,
            userNotesDao = userNotesDao,
            database = { clientDatabase },
            settings = settings,
        )
        return TestData(
            clientDatabase = clientDatabase,
            serverDatabase = createServerDatabase(),
            userEntryDao = userEntryDao,
            userNotesDao = userNotesDao,
            syncer = syncer,
            settings = settings,
            data = artistData,
        )
    }

    private suspend fun <Source> createDatabases(
        parameters: Parameters<Source, *, *, *>,
    ): TestData<Source> {
        val clientDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val clientDatabase = createClientDatabase(clientDriver, parameters)
        val settings = Settings()
        val userEntryDao = UserEntryDao(
            driver = { clientDriver },
            database = { clientDatabase },
            settings = settings,
            clock = clock,
        )
        val userNotesDao = UserNotesDao(
            database = { clientDatabase },
            settings = settings,
            clock = clock,
        )
        val syncer = AlleyUserDataSyncer(
            userDataSyncDao = UserDataSyncDao(database = { clientDatabase }),
            userEntryDao = userEntryDao,
            userNotesDao = userNotesDao,
            database = { clientDatabase },
            settings = settings,
        )
        return TestData(
            clientDatabase = clientDatabase,
            serverDatabase = createServerDatabase(),
            userEntryDao = userEntryDao,
            userNotesDao = userNotesDao,
            syncer = syncer,
            settings = settings,
            data = parameters.data(),
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

    private suspend fun <Source> createClientDatabase(
        driver: SqlDriver,
        parameters: Parameters<Source, *, *, *>,
    ): AlleySqlDatabase {
        AlleySqlDatabase.Schema.create(driver).await()
        return DaoUtils.createAlleySqlDatabase(driver).apply {
            with(parameters) {
                data().forEach {
                    testQueries.insertBaseEntry(it)
                }
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

    private suspend fun <Payload> AlleyUserDataSyncer.sendClientPayloadToServerAndSync2(
        parameters: Parameters<*, *, Payload, *>,
        serverDatabase: AlleyUserDatabase,
        expectedClientGeneration: Long,
        expectedPayload: List<Payload>,
        expectedHttpStatus: Short,
        expectedServerGeneration: Long,
        expectedServerPayload: List<Payload>,
    ) {
        val clientPayload = getPayload()
        clientPayload.generation.shouldBe(expectedClientGeneration)
        parameters.field(clientPayload).shouldContainExactlyInAnyOrder(expectedPayload)

        val [httpStatus, serverPayload] =
            UserDataHandler.syncFromClient(clock, USER_ID, clientPayload, serverDatabase)
        httpStatus.shouldBe(expectedHttpStatus)
        serverPayload.generation.shouldBe(expectedServerGeneration)
        parameters.field(serverPayload).shouldContainExactlyInAnyOrder(expectedServerPayload)

        syncFromServer(httpStatus, serverPayload)
    }

    private val ParametersArtists = Parameters(
        data = { this.artistData.take(3).toList() },
        field = { it.artists },
        insertBaseEntry = {
            insertArtist(it.source.artist.databaseEntry.toArtistEntry(it.source.artist.year))
        },
        insertUserEntry = { entryDao, _, data -> entryDao.insertArtistUserEntry(data.source.userEntry) },
        insertServerEntry = { userArtistQueries.insertUserArtist(it) },
        getClientEntries = { testQueries.getArtistUserEntries().awaitAsList() },
        getServerEntries = { testQueries.getAllUserArtists(USER_ID).awaitAsList() },
        toUserEntry = ArtistData::toUserEntry,
        toPayload = ArtistData::toPayload,
        toServerModel = ArtistData::toServerModel,
    )

    private val ParametersArtistNotes = Parameters(
        data = { this.artistNotesData.take(3).toList() },
        field = { it.artistNotes },
        insertBaseEntry = {
            insertArtist(it.source.first.databaseEntry.toArtistEntry(it.source.first.year))
        },
        insertUserEntry = { _, notesDao, data ->
            notesDao.updateArtistNotes(
                artistId = data.source.first.id,
                year = data.source.second.dataYear,
                notes = data.source.third.notes,
            )
        },
        insertServerEntry = { userArtistNotesQueries.insertUserArtistNotes(it) },
        getClientEntries = { testQueries.getArtistNotes().awaitAsList() },
        getServerEntries = { testQueries.getAllUserArtistNotes(USER_ID).awaitAsList() },
        toUserEntry = ArtistNotesData::toUserEntry,
        toPayload = ArtistNotesData::toPayload,
        toServerModel = ArtistNotesData::toServerModel,
    )

    private val ParametersStampRallies = Parameters(
        data = { this.stampRallyData.take(3).toList() },
        field = { it.rallies },
        insertBaseEntry = {
            insertStampRally(it.source.stampRally.toStampRallyEntry(it.source.stampRally.year))
        },
        insertUserEntry = { entryDao, _, data -> entryDao.insertStampRallyUserEntry(data.source.userEntry) },
        insertServerEntry = { userStampRallyQueries.insertUserStampRally(it) },
        getClientEntries = { testQueries.getStampRallyUserEntries().awaitAsList() },
        getServerEntries = { testQueries.getAllUserStampRallies(USER_ID).awaitAsList() },
        toUserEntry = StampRallyData::toUserEntry,
        toPayload = StampRallyData::toPayload,
        toServerModel = StampRallyData::toServerModel,
    )

    private val ParametersSeries = Parameters(
        data = { this.seriesData.take(3).toList() },
        field = { it.series },
        insertBaseEntry = { insertSeries(it.source.series.toSeriesEntry()) },
        insertUserEntry = { entryDao, _, data -> entryDao.insertSeriesUserEntry(data.source.userEntry) },
        insertServerEntry = { userSeriesQueries.insertUserSeries(it) },
        getClientEntries = { testQueries.getSeriesUserEntries().awaitAsList() },
        getServerEntries = { testQueries.getAllUserSeries(USER_ID).awaitAsList() },
        toUserEntry = SeriesData::toUserEntry,
        toPayload = SeriesData::toPayload,
        toServerModel = SeriesData::toServerModel,
    )

    private val ParametersMerch = Parameters(
        data = { this.merchData.take(3).toList() },
        field = { it.merch },
        insertBaseEntry = { insertMerch(it.source.merch) },
        insertUserEntry = { entryDao, _, data -> entryDao.insertMerchUserEntry(data.source.userEntry) },
        insertServerEntry = { userMerchQueries.insertUserMerch(it) },
        getClientEntries = { testQueries.getMerchUserEntries().awaitAsList() },
        getServerEntries = { testQueries.getAllUserMerch(USER_ID).awaitAsList() },
        toUserEntry = MerchData::toUserEntry,
        toPayload = MerchData::toPayload,
        toServerModel = MerchData::toServerModel,
    )

    data class Parameters<Source, UserEntry, Payload, ServerModel>(
        val data: () -> List<Source>,
        val field: (UserDataSyncPayload) -> List<Payload>?,
        val insertBaseEntry: suspend TestQueries.(Source) -> Unit,
        val insertUserEntry: suspend (UserEntryDao, UserNotesDao, Source) -> Unit,
        val insertServerEntry: suspend AlleyUserDatabase.(ServerModel) -> Unit,
        val getClientEntries: suspend AlleySqlDatabase.() -> List<UserEntry>,
        val getServerEntries: suspend AlleyUserDatabase.() -> List<ServerModel>,
        val toUserEntry: Source.(updatedAt: Instant, generation: Long) -> UserEntry,
        val toPayload: Source.(updatedAt: Instant, generation: Long) -> Payload,
        val toServerModel: Source.(updatedAt: Instant, generation: Long) -> ServerModel,
    )

    private data class TestData<Source>(
        val clientDatabase: AlleySqlDatabase,
        val serverDatabase: AlleyUserDatabase,
        val userEntryDao: UserEntryDao,
        val userNotesDao: UserNotesDao,
        val syncer: AlleyUserDataSyncer,
        val settings: AlleyUserSettings,
        val data: List<Source>,
    )

    private data class ArtistData(val source: ArtistWithUserData) {

        fun toEdited(favorite: Boolean, ignored: Boolean) = copy(
            source = source.copy(
                userEntry = source.userEntry.copy(favorite = favorite, ignored = ignored)
            )
        )

        fun toUserEntry(updatedAt: Instant, generation: Long) =
            source.userEntry.copy(updatedAt = updatedAt, generation = generation)

        fun toPayload(updatedAt: Instant, generation: Long) = UserDataSyncPayload.Artist(
            generation = generation,
            dataYear = source.artist.year,
            id = source.artist.id,
            favorite = source.userEntry.favorite,
            ignored = source.userEntry.ignored,
            updatedAt = updatedAt,
        )

        fun toServerModel(updatedAt: Instant, generation: Long) = UserArtist(
            userId = USER_ID,
            dataYear = source.artist.year,
            artistId = source.artist.id,
            favorite = source.userEntry.favorite,
            ignored = source.userEntry.ignored,
            updatedAt = updatedAt,
            generation = generation,
        )
    }

    private data class ArtistNotesData(val source: Triple<ArtistEntry, ArtistUserEntry, ArtistNotes>) {

        fun toEdited(notes: String) =
            copy(source = source.copy(third = source.third.copy(notes = notes)))

        fun toUserEntry(updatedAt: Instant, generation: Long) =
            source.third.copy(updatedAt = updatedAt, generation = generation)

        fun toPayload(updatedAt: Instant, generation: Long) = UserDataSyncPayload.ArtistNotes(
            generation = generation,
            dataYear = source.third.dataYear,
            artistId = EncryptedArtistId(source.third.artistId),
            notes = source.third.notes,
            updatedAt = updatedAt,
        )

        fun toServerModel(updatedAt: Instant, generation: Long) = UserArtistNotes(
            userId = USER_ID,
            dataYear = source.third.dataYear,
            artistId = EncryptedArtistId(source.third.artistId),
            notes = source.third.notes,
            updatedAt = updatedAt,
            generation = generation,
        )
    }

    private data class StampRallyData(val source: StampRallyWithUserData) {

        fun toEdited(favorite: Boolean, ignored: Boolean) = copy(
            source = source.copy(
                userEntry = source.userEntry.copy(favorite = favorite, ignored = ignored)
            )
        )

        fun toUserEntry(updatedAt: Instant, generation: Long) =
            source.userEntry.copy(updatedAt = updatedAt, generation = generation)

        fun toPayload(updatedAt: Instant, generation: Long) = UserDataSyncPayload.StampRally(
            generation = generation,
            id = source.stampRally.id,
            favorite = source.userEntry.favorite,
            ignored = source.userEntry.ignored,
            updatedAt = updatedAt,
        )

        fun toServerModel(updatedAt: Instant, generation: Long) = UserStampRally(
            userId = USER_ID,
            stampRallyId = source.stampRally.id,
            favorite = source.userEntry.favorite,
            ignored = source.userEntry.ignored,
            updatedAt = updatedAt,
            generation = generation,
        )
    }

    private data class SeriesData(val source: SeriesWithUserData) {

        fun toEdited(favorite: Boolean) = copy(
            source = source.copy(
                userEntry = source.userEntry.copy(favorite = favorite)
            )
        )

        fun toUserEntry(updatedAt: Instant, generation: Long) =
            source.userEntry.copy(updatedAt = updatedAt, generation = generation)

        fun toPayload(updatedAt: Instant, generation: Long) = UserDataSyncPayload.Series(
            generation = generation,
            id = source.series.uuid.toString(),
            favorite = source.userEntry.favorite,
            updatedAt = updatedAt,
        )

        fun toServerModel(updatedAt: Instant, generation: Long) = UserSeries(
            userId = USER_ID,
            seriesId = source.series.uuid.toString(),
            favorite = source.userEntry.favorite,
            updatedAt = updatedAt,
            generation = generation,
        )
    }

    private data class MerchData(val source: MerchWithUserData) {

        fun toEdited(favorite: Boolean) = copy(
            source = source.copy(
                userEntry = source.userEntry.copy(favorite = favorite)
            )
        )

        fun toUserEntry(updatedAt: Instant, generation: Long) =
            source.userEntry.copy(updatedAt = updatedAt, generation = generation)

        fun toPayload(updatedAt: Instant, generation: Long) = UserDataSyncPayload.Merch(
            generation = generation,
            id = source.merch.uuid,
            favorite = source.userEntry.favorite,
            updatedAt = updatedAt,
        )

        fun toServerModel(updatedAt: Instant, generation: Long) = UserMerch(
            userId = USER_ID,
            merchId = source.merch.uuid,
            favorite = source.userEntry.favorite,
            updatedAt = updatedAt,
            generation = generation,
        )
    }

    private class Settings : AlleyUserSettings {
        override var pendingUserAuthCode: String? by mutableStateOf(null)
        override var lastSyncGeneration: Long? by mutableStateOf(null)
    }
}
