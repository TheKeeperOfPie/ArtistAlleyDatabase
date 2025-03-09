package com.thekeeperofpie.artistalleydatabase.alley.user

import me.tatarka.inject.annotations.Inject

@Inject
class AlleyUserDatabase(private val driverFactory: DriverFactory) {
    suspend fun createDriver() = driverFactory.createDriver()
}
