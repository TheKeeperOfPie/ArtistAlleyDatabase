package com.thekeeperofpie.artistalleydatabase.alley.user

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import dev.zacsweers.metro.Inject
import org.w3c.dom.Worker
import kotlin.js.js

expect fun createWebWorkerDriver(worker: Worker): SqlDriver

private fun createCustomWorker(): Worker =
    js("""new Worker(new URL("@thekeeperofpie/alley-sqldelight-worker/sqldelight-worker.js", import.meta.url))""")

private const val pragmaUserVersion = "user_version"

@Inject
actual class DriverFactory {

    private val currentVersion = AlleySqlDatabase.Schema.version

    actual suspend fun createDriver(): SqlDriver = createWebWorkerDriver(createCustomWorker())
        .also { migrate(it) }

    private suspend fun migrate(driver: SqlDriver) {
        var oldVersion = driver.executeQuery(
            identifier = null,
            sql = "PRAGMA $pragmaUserVersion",
            mapper = {
                it.next()
                QueryResult.Value(it.getLong(0))
            },
            parameters = 0,
        ).await()

        if (oldVersion == currentVersion) return

        // First public build didn't write a version, so use table presence as a proxy
        if (oldVersion == null || oldVersion == 0L) {
            val tableCount = driver.executeQuery(
                identifier = null,
                sql = "SELECT COUNT(*) FROM sqlite_schema WHERE type='table' AND name='artistUserEntry'",
                mapper = {
                    it.next()
                    QueryResult.Value(it.getLong(0))
                },
                parameters = 0,
            ).await()
            if (tableCount != null && tableCount > 0) {
                oldVersion = 1
            }
        }

        ConsoleLogger.log("Migrating from $oldVersion to $currentVersion")

        if (oldVersion == null || oldVersion == 0L) {
            AlleySqlDatabase.Schema.create(driver).await()
            driver.execute(null, "PRAGMA $pragmaUserVersion=$currentVersion", 0).await()
        } else if (oldVersion < currentVersion) {
            AlleySqlDatabase.Schema.migrate(
                driver = driver,
                oldVersion = oldVersion,
                newVersion = currentVersion,
            ).await()
            driver.execute(null, "PRAGMA $pragmaUserVersion=$currentVersion", 0).await()
        }
    }
}
