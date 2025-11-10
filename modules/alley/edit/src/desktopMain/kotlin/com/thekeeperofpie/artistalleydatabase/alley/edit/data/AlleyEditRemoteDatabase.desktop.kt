package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase {
    actual suspend fun loadFunction() = "desktop"
}
