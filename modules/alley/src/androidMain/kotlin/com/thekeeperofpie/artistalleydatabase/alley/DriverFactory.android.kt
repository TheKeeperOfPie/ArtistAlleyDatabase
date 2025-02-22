package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteDatabaseType
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteDriver
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
        return AndroidxSqliteDriver(
            driver = BundledSQLiteDriver(),
            databaseType = AndroidxSqliteDatabaseType.File(userPath.path),
            schema = Schema(application).synchronous(),
        )
    }

    class Schema(private val application: Application) : SqlSchema<QueryResult.AsyncValue<Unit>> {
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

                val readOnlyPath = application.getDatabasePath("alley")
                driver.execute(
                    null, """
                    |ATTACH DATABASE '$readOnlyPath' AS readOnly
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
