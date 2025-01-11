package com.thekeeperofpie.artistalleydatabase.alley

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
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            it.execute(null, "ATTACH DATABASE '${file.absolutePath}' AS readOnly;", 0)
        }
    }
}
