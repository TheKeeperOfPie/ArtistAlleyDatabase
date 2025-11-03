package com.thekeeperofpie.artistalleydatabase.alley.images

import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.w3c.fetch.Request
import org.w3c.workers.Cache
import kotlin.time.Clock

// Must keep in sync with ServiceWorker.kt
private const val MEDIA_IMAGE_CACHE = "media-image-v1"

@SingleIn(AppScope::class)
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
        val keys = cache.keys().await<JsArray<Request>>()
        var index = 0
        while (index < keys.length) {
            val chunk = mutableListOf<Request>()
            while (index < keys.length && chunk.size < 25) {
                keys[index++]?.let {
                    chunk += it
                }
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

    private suspend fun cache() = window.caches.open(MEDIA_IMAGE_CACHE).await<Cache>()
}
