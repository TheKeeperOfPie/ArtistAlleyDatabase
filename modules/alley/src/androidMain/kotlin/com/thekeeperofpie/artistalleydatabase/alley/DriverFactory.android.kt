package com.thekeeperofpie.artistalleydatabase.alley

import android.app.Application
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import me.tatarka.inject.annotations.Inject

@Inject
actual class DriverFactory(private val application: Application) {
    actual fun createDriver(): SqlDriver {
        val path = application.getDatabasePath("alleyDatabase")
        // TODO: Skip if the same
        path.outputStream().use { output ->
            application.assets.open("composeResources/artistalleydatabase.modules.alley.generated.resources/files/database.sqlite")
                .copyTo(output)
        }
        return AndroidSqliteDriver(AlleySqlDatabase.Schema.synchronous(), application, path.name)
    }

    actual suspend fun applySchema(driver: SqlDriver) = Unit
}
