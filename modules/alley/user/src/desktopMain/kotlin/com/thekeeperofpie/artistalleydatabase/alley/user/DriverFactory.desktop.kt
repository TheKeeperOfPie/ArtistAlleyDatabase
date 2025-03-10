package com.thekeeperofpie.artistalleydatabase.alley.user

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import artistalleydatabase.modules.alley.data.generated.resources.Res
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.nio.file.Files

@Inject
actual class DriverFactory {
    @OptIn(ExperimentalResourceApi::class)
    actual suspend fun createDriver(): SqlDriver {
        val file = Files.createTempFile(null, ".sqlite").toFile()
        file.deleteOnExit()
        file.writeBytes(Res.readBytes("files/database.sqlite"))
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            AlleySqlDatabase.Schema.create(it).await()
            it.execute(null, "ATTACH DATABASE '${file.absolutePath}' AS readOnly;", 0).await()
        }
    }
}
