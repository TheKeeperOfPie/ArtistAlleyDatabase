package com.thekeeperofpie.artistalleydatabase.alley.user

import dev.zacsweers.metro.Inject

@Inject
class AlleyUserDatabase(private val driverFactory: DriverFactory) {
    suspend fun createDriver() = driverFactory.createDriver()
}
