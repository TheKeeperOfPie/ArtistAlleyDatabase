package com.thekeeperofpie.artistalleydatabase.alley.images

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
actual class ImageCache {
    actual suspend fun cache(urls: Collection<String>) {
        // Do nothing, JVM targets don't support this
    }
}
