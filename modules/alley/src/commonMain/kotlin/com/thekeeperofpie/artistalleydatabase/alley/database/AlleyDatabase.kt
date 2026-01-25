package com.thekeeperofpie.artistalleydatabase.alley.database

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn

@SingleIn(AppScope::class)
@Inject
class ArtistAlleyDatabase(
    applicationScope: ApplicationScope,
    userDatabase: AlleyUserDatabase,
) {
    private val database = flowFromSuspend {
        val driver = userDatabase.createDriver()
        val database = DaoUtils.createAlleySqlDatabase(driver)
        // TODO: Retain only valid IDs
        driver to database
    }.flowOn(PlatformDispatchers.IO)
        .shareIn(applicationScope, SharingStarted.Eagerly, replay = 1)

    internal suspend fun database() = (database.replayCache.firstOrNull() ?: database.first()).second
    internal suspend fun driver() = (database.replayCache.firstOrNull() ?: database.first()).first
}
