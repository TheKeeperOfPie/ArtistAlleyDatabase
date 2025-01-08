package com.thekeeperofpie.artistalleydatabase.alley

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import artistalleydatabase.modules.alley.generated.resources.Res
import kotlinx.coroutines.runBlocking
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.nio.file.Files

@Inject
actual class DriverFactory {
    @OptIn(ExperimentalResourceApi::class)
    actual fun createDriver(): SqlDriver {
        val file = Files.createTempFile(null, ".sqlite").toFile()
        file.deleteOnExit()
        runBlocking {
            file.writeBytes(Res.readBytes("files/database.sqlite"))
        }
        return JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}")
    }
    actual suspend fun applySchema(driver: SqlDriver) = AlleySqlDatabase.Schema.awaitCreate(driver)
}
