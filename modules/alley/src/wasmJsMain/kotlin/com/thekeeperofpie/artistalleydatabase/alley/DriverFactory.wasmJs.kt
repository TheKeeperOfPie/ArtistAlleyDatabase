package com.thekeeperofpie.artistalleydatabase.alley

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import me.tatarka.inject.annotations.Inject
import org.w3c.dom.Worker

private fun createCustomWorker(): Worker =
    js("""new Worker(new URL("@thekeeperofpie/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")

@Inject
actual class DriverFactory {
    actual fun createDriver(): SqlDriver = WebWorkerDriver(createCustomWorker())
    actual suspend fun applySchema(driver: SqlDriver) =
        AlleySqlDatabase.Schema.awaitCreate(driver)
}
