package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import me.tatarka.inject.annotations.Inject

@Inject
actual class DriverFactory(private val application: Application) {
    actual fun createDriver(): SqlDriver {
        val path = application.getDatabasePath("alleyDatabase")
        // TODO: Skip if the same
        path.outputStream().use { output ->
            application.assets.open("composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/database.sqlite")
                .copyTo(output)
        }
        return AndroidSqliteDriver(Schema.synchronous(), application, path.name)
    }

    object Schema : SqlSchema<QueryResult.AsyncValue<Unit>> {
        override val version: Long
            get() = 1

        override fun create(driver: SqlDriver): QueryResult.AsyncValue<Unit> = QueryResult.AsyncValue {
            driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS `artistUserEntry` (
          |    `artistId` TEXT NOT NULL,
          |    `favorite` INTEGER NOT NULL DEFAULT 0,
          |    `ignored` INTEGER NOT NULL DEFAULT 0,
          |    `notes` TEXT,
          |    PRIMARY KEY (`artistId`)
          |)
          """.trimMargin(), 0).await()
            driver.execute(null, """
          |CREATE TABLE IF NOT EXISTS `stampRallyUserEntry` (
          |    `stampRallyId` TEXT NOT NULL,
          |    `favorite` INTEGER NOT NULL DEFAULT 0,
          |    `ignored` INTEGER NOT NULL DEFAULT 0,
          |    `notes` TEXT,
          |    PRIMARY KEY (`stampRallyId`)
          |)
          """.trimMargin(), 0).await()
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
