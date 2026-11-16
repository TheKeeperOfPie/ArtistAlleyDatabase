package com.thekeeperofpie.artistalleydatabase.alley.sync

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.toArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.DaoUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import io.kotest.matchers.collections.shouldContainAllInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
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
        val (driver, database, artistData) = createTestDatabase()
        val settings = Settings()
        val syncer = AlleyUserDataSyncer(UserDataSyncDao(database = { database }), settings)

        val timeSource = TestTimeSource()
        val clock = timeSource.asClock(ORIGIN)
        val userEntryDao = UserEntryDao(driver = { driver }, database = { database }, clock = clock)
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
                    favorite = true,
                    ignored = false,
                    updatedAt = ORIGIN,
                ),
                UserDataSyncPayload.Artist(
                    generation = 0,
                    id = artistData[1].artist.id,
                    favorite = true,
                    ignored = true,
                    updatedAt = ORIGIN + 3.seconds,
                ),
                UserDataSyncPayload.Artist(
                    generation = 0,
                    id = artistData[2].artist.id,
                    favorite = false,
                    ignored = true,
                    updatedAt = ORIGIN + 3.seconds + 5.seconds,
                ),
            )
    }

    private suspend fun createTestDatabase(): TestData {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AlleySqlDatabase.Schema.create(driver).await()
        val database = DaoUtils.createAlleySqlDatabase(driver)
        val artistsWithUserData = ArtistWithUserDataProvider.values.take(3).toList()
        artistsWithUserData.forEach {
            database.testQueries.insertArtist(it.artist.databaseEntry.toArtistEntry(it.artist.year))
        }

        return TestData(driver, database, artistsWithUserData)
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
        val driver: SqlDriver,
        val database: AlleySqlDatabase,
        val artistData: List<ArtistWithUserData>,
    )

    private class Settings : AlleyUserSettings {
        override var pendingUserAuthCode: String? by mutableStateOf(null)
        override var lastSyncGeneration: Long? by mutableStateOf(null)
    }
}
