package com.thekeeperofpie.artistalleydatabase.alley.sync

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.functions.BackendDatabases
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user.UserDataHandler
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user.UserId
import com.thekeeperofpie.artistalleydatabase.alley.functions.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.UserArtist
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
import kotlin.time.TestTimeSource

private val ORIGIN =
    LocalDate(year = 2026, month = Month.JULY, day = 4).atStartOfDayIn(TimeZone.UTC)

class UserDataSyncTest {

    @Test
    fun singleDevice_multipleWrites() = runTest {
        val (clientDriver, clientDatabase, serverDatabase, artistData) = createDatabases()
        val settings = Settings()
        val syncer = AlleyUserDataSyncer(UserDataSyncDao(database = { clientDatabase }), settings)

        val timeSource = TestTimeSource()
        val clock = timeSource.asClock(ORIGIN)
        val userEntryDao =
            UserEntryDao(driver = { clientDriver }, database = { clientDatabase }, clock = clock)
        userEntryDao.insertArtistUserEntry(
            artistData[0].toArtistUserEntry(favorite = true, ignored = false)
        )
        timeSource += 3.seconds
        userEntryDao.insertArtistUserEntry(
            artistData[1].toArtistUserEntry(favorite = true, ignored = true)
        )
        timeSource += 5.seconds
        userEntryDao.insertArtistUserEntry(
            artistData[2].toArtistUserEntry(favorite = false, ignored = true)
        )

        val payload = syncer.getPayload()
        payload.artists
            .shouldNotBeNull()
            .shouldContainAllInAnyOrder(
                UserDataSyncPayload.Artist(
                    generation = 0,
                    id = artistData[0].artist.id,
                    dataYear = artistData[0].artist.year,
                    favorite = true,
                    ignored = false,
                    updatedAt = ORIGIN,
                ),
                UserDataSyncPayload.Artist(
                    generation = 0,
                    id = artistData[1].artist.id,
                    dataYear = artistData[1].artist.year,
                    favorite = true,
                    ignored = true,
                    updatedAt = ORIGIN + 3.seconds,
                ),
                UserDataSyncPayload.Artist(
                    generation = 0,
                    id = artistData[2].artist.id,
                    dataYear = artistData[2].artist.year,
                    favorite = false,
                    ignored = true,
                    updatedAt = ORIGIN + 3.seconds + 5.seconds,
                ),
            )

        val userId = UserId("testUserId")
        UserDataHandler.syncFromClient(clock, userId, payload, serverDatabase)
            .shouldBe(1)

        serverDatabase.testQueries.getAllArtists(userId).awaitAsList()
            .shouldContainAllInAnyOrder(
                UserArtist(
                    userId = userId,
                    dataYear = artistData[0].artist.year,
                    artistId = artistData[0].artist.id,
                    favorite = true,
                    ignored = false,
                    updatedAt = ORIGIN + 8.seconds,
                    generation = 1,
                ),
                UserArtist(
                    userId = userId,
                    dataYear = artistData[1].artist.year,
                    artistId = artistData[1].artist.id,
                    favorite = true,
                    ignored = true,
                    updatedAt = ORIGIN + 8.seconds,
                    generation = 1,
                ),
                UserArtist(
                    userId = userId,
                    dataYear = artistData[2].artist.year,
                    artistId = artistData[2].artist.id,
                    favorite = false,
                    ignored = true,
                    updatedAt = ORIGIN + 8.seconds,
                    generation = 1,
                ),
            )
    }

    private suspend fun createDatabases(): TestData {
        val clientDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val artistsWithUserData = ArtistWithUserDataProvider.values.take(3).toList()
        return TestData(
            clientDriver = clientDriver,
            clientDatabase = createClientDatabase(clientDriver, artistsWithUserData),
            serverDatabase = createServerDatabase(),
            artistData = artistsWithUserData,
        )
    }

    private suspend fun createClientDatabase(
        driver: SqlDriver,
        artistsWithUserData: List<ArtistWithUserData>,
    ): AlleySqlDatabase {
        AlleySqlDatabase.Schema.create(driver).await()
        val database = DaoUtils.createAlleySqlDatabase(driver)
        artistsWithUserData.forEach {
            database.testQueries.insertArtist(it.artist.databaseEntry.toArtistEntry(it.artist.year))
        }
        return database
    }

    private suspend fun createServerDatabase(): AlleyUserDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AlleyUserDatabase.Schema.create(driver).await()
        return BackendDatabases.userDatabase(driver)
    }

    private fun ArtistWithUserData.toArtistUserEntry(favorite: Boolean, ignored: Boolean) =
        ArtistUserEntry(
            artistId = artist.id,
            dataYear = artist.year,
            favorite = favorite,
            ignored = ignored,
            // These are updated by UserEntryDao
            generation = 0L,
            updatedAt = null,
        )

    private data class TestData(
        val clientDriver: SqlDriver,
        val clientDatabase: AlleySqlDatabase,
        val serverDatabase: AlleyUserDatabase,
        val artistData: List<ArtistWithUserData>,
    )

    private class Settings : AlleyUserSettings {
        override var pendingUserAuthCode: String? by mutableStateOf(null)
        override var lastSyncGeneration: Long? by mutableStateOf(null)
    }
}
