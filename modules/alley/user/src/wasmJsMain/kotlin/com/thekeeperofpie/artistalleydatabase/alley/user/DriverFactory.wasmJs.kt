package com.thekeeperofpie.artistalleydatabase.alley.user

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual fun createWebWorkerDriver(worker: Worker): SqlDriver = WebWorkerDriver(worker)
