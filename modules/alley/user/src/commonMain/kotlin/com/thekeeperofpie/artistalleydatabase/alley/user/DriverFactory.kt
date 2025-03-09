package com.thekeeperofpie.artistalleydatabase.alley.user

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    suspend fun createDriver(): SqlDriver
}
