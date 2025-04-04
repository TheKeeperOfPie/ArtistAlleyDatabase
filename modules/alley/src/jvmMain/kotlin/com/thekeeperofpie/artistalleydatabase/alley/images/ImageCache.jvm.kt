package com.thekeeperofpie.artistalleydatabase.alley.images

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
actual class ImageCache {
    actual suspend fun cache(urls: List<String>) {
        // Do nothing, JVM targets don't support this
    }
}
