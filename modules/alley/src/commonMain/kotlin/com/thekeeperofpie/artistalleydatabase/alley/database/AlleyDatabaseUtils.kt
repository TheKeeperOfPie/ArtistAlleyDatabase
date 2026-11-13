package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase

internal object AlleyDatabaseUtils {
    suspend fun getNextGeneration(database: suspend () -> AlleySqlDatabase) =
        (database().generationQueries.getLastGeneration().awaitAsOneOrNull()?.lastGeneration ?: 0L) + 1L
}
