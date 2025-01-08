package com.thekeeperofpie.artistalleydatabase.alley

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
    suspend fun applySchema(driver: SqlDriver)
}
