package com.thekeeperofpie.artistalleydatabase.alley

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import me.tatarka.inject.annotations.Inject
import org.w3c.dom.Worker

private fun createCustomWorker(): Worker =
    js("""new Worker(new URL("@thekeeperofpie/alley-sqldelight-worker/sqldelight-worker.js", import.meta.url))""")

@Inject
actual class DriverFactory {
    actual fun createDriver(): SqlDriver = WebWorkerDriver(createCustomWorker())
}
