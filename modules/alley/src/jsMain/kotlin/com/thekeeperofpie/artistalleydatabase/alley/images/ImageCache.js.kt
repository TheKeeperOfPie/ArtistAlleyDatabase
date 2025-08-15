package com.thekeeperofpie.artistalleydatabase.alley.images

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import me.tatarka.inject.annotations.Inject
import org.w3c.fetch.Request

// Must keep in sync with ServiceWorker.kt
private const val MEDIA_IMAGE_CACHE = "media-image-v1"

@SingletonScope
@Inject
actual class ImageCache(
    private val imageEntryDao: ImageEntryDao,
) {
    private val queue = Channel<Collection<String>>(1)

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(PlatformDispatchers.IO) {
            prune()

            while (isActive) {
                val urls = queue.receive()
                val cache = cache()
                urls.forEach { cache.add(it).await() }
            }
        }
    }

    private suspend fun prune() {
        val now = Clock.System.now()
        val cache = cache()
        val keys = cache.keys().await()
        var index = 0
        while (index < keys.size) {
            val chunk = mutableListOf<Request>()
            while (index < keys.size && chunk.size < 25) {
                chunk += keys[index++]
            }
            if (chunk.isNotEmpty()) {
                val urls = chunk.map { it.url }
                val cachedUrls = imageEntryDao.queryUrls(urls)
                chunk.filter { cachedUrls[it.url] == null }
                    .forEach(cache::delete)
            }
        }
    }

    actual suspend fun cache(urls: Collection<String>) = queue.send(urls)

    private suspend fun cache() = window.caches.open(MEDIA_IMAGE_CACHE).await()
}
