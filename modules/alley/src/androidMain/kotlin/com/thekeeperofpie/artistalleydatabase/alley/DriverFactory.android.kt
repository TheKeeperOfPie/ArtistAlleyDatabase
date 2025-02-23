package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_CREATE
import androidx.sqlite.driver.bundled.SQLITE_OPEN_FULLMUTEX
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteDatabaseType
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteDriver
import kotlinx.coroutines.runBlocking
import me.tatarka.inject.annotations.Inject

@Inject
actual class DriverFactory(private val application: Application) {
    actual fun createDriver(): SqlDriver {
        val readOnlyPath = application.getDatabasePath("alley")
        // TODO: Skip if the same
        readOnlyPath.outputStream().use { output ->
            application.assets.open("composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/database.sqlite")
                .copyTo(output)
        }
        val userPath = application.getDatabasePath("alleyUser")
        val driver = BundledSQLiteDriver()
        return AndroidxSqliteDriver(
            driver = object : SQLiteDriver {
                override fun open(fileName: String) =
                    driver.open(
                        fileName,
                        SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE or SQLITE_OPEN_FULLMUTEX,
                    )
            },
            databaseType = AndroidxSqliteDatabaseType.File(userPath.path),
            schema = Schema.synchronous(),
        )
            .also {
                runBlocking {
                    it.execute(
                        null, """
                    |ATTACH DATABASE '$readOnlyPath' AS readOnly
                    """.trimMargin(), 0
                    ).await()
                }
            }
    }

    object Schema : SqlSchema<QueryResult.AsyncValue<Unit>> {
        override val version: Long
            get() = 1

        override fun create(driver: SqlDriver): QueryResult.AsyncValue<Unit> =
            QueryResult.AsyncValue {
                driver.execute(
                    null, """
                    |CREATE TABLE IF NOT EXISTS `artistUserEntry` (
                    |    `artistId` TEXT NOT NULL,
                    |    `favorite` INTEGER NOT NULL DEFAULT 0,
                    |    `ignored` INTEGER NOT NULL DEFAULT 0,
                    |    `notes` TEXT,
                    |    PRIMARY KEY (`artistId`)
                    |)
                    """.trimMargin(), 0
                ).await()
                driver.execute(
                    null, """
                    |CREATE TABLE IF NOT EXISTS `stampRallyUserEntry` (
                    |    `stampRallyId` TEXT NOT NULL,
                    |    `favorite` INTEGER NOT NULL DEFAULT 0,
                    |    `ignored` INTEGER NOT NULL DEFAULT 0,
                    |    `notes` TEXT,
                    |    PRIMARY KEY (`stampRallyId`)
                    |)
                    """.trimMargin(), 0
                ).await()
            }

        override fun migrate(
            driver: SqlDriver,
            oldVersion: Long,
            newVersion: Long,
            vararg callbacks: AfterVersion,
        ): QueryResult.AsyncValue<Unit> = QueryResult.AsyncValue {
        }
    }
}
