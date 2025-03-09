package com.thekeeperofpie.artistalleydatabase.alley.user

import android.app.Application
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_CREATE
import androidx.sqlite.driver.bundled.SQLITE_OPEN_FULLMUTEX
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteDatabaseType
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteDriver
import me.tatarka.inject.annotations.Inject

@Inject
actual class DriverFactory(private val application: Application) {
    actual suspend fun createDriver(): SqlDriver {
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
            schema = AlleySqlDatabase.Schema.synchronous(),
        )
            .also {
                it.execute(
                    null, """
                    |ATTACH DATABASE '$readOnlyPath' AS readOnly
                    """.trimMargin(), 0
                ).await()
            }
    }
}
