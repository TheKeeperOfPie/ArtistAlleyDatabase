package com.thekeeperofpie.artistalleydatabase.alley.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.w3c.dom.url.URL
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import org.w3c.workers.ExtendableEvent
import org.w3c.workers.ExtendableMessageEvent
import org.w3c.workers.FetchEvent
import org.w3c.workers.InstallEvent
import org.w3c.workers.ServiceWorkerGlobalScope
import kotlin.js.Promise

external val self: ServiceWorkerGlobalScope

private val APP_CACHE = CacheName("alley-app-v1")
private val IMAGE_CACHE = CacheName("alley-image-v1")
private val CACHES = setOf(APP_CACHE, IMAGE_CACHE)

value class CacheName(val name: String)

private const val catalogsDir =
    "/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/catalogs"
private const val ralliesDir =
    "/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/rallies"

private const val filesToCacheInput = "CACHE_INPUT"
private val filesToCacheAndRevisions = filesToCacheInput.lineSequence()
    .associate { it.substringBeforeLast("-") to it.substringAfterLast("-") }

fun main() {
    console.log("Initializing service worker")

    self.addEventListener("install", { event ->
        console.log("serviceWorker.js install")
        event as InstallEvent
        event.waitUntil(
            promise {
                val urlsToCache = filesToCacheAndRevisions.map {
                    console.log("Cache input ${it.key}-${it.value}")
                    "/${it.key}?__REVISION__=${it.value}"
                }.toTypedArray()

                self.caches.open(APP_CACHE.name).await()
                    .addAll(urlsToCache)
                    .await()
            }
        )
    })

    self.addEventListener("activate", { event ->
        event as ExtendableEvent
        console.log("Activating", event)
        event.waitUntil(
            promise {
                cleanUpCaches()
            }
        )
    })

    self.addEventListener("message", { event ->
        console.log("Message received", event)
        if (event is ExtendableMessageEvent && event.data == "SKIP_WAITING") {
            event.waitUntil(self.skipWaiting())
        }
    })

    self.addEventListener("fetch", { event ->
        event as FetchEvent
        val request = event.request
        val url = URL(event.request.url)
        val urlPath = url.pathname
        val urlPathWithoutSlash = urlPath.removePrefix("/")
        if (url.host.contains("anilist.co")) {
            event.respondWith(self.fetch(request))
        } else {
            event.respondWith(
                promise {
                    if (urlPath == "/") {
                        val cachedRevision = filesToCacheAndRevisions["index.html"]
                        fetchCacheFirst(
                            "/index.html?__REVISION__=$cachedRevision",
                            request,
                            urlPath
                        )
                    } else {
                        val cachedRevision = filesToCacheAndRevisions[urlPathWithoutSlash]
                        if (cachedRevision != null) {
                            url.searchParams.set("__REVISION__", cachedRevision)
                            fetchCacheFirst(url.href, request, urlPath)
                        } else {
                            fetchCacheFirst(request, request, urlPath)
                        }
                    }
                }
            )
        }
    })
}

private suspend fun fetchCacheFirst(
    cacheRequest: dynamic,
    remoteRequest: Request,
    path: String,
): Response {
    val cached = self.caches.match(cacheRequest).await()
    return if (cached is Response) {
        console.log("Cache hit", path)
        if (cached.redirected) {
            Response(cached.body, object : ResponseInit {
                override var headers = cached.headers
                override var status: Short? = cached.status
                override var statusText: String? = cached.statusText
            })
        } else {
            cached
        }
    } else {
        self.fetch(remoteRequest).await()
    }
}

private suspend fun CoroutineScope.cleanUpCaches() {
    self.caches.keys()
        .await()
        .filter { key -> CACHES.none { it.name == key } }
        .map {
            async {
                console.log("Deleting old cache", it)
                self.caches.delete(it).await()
            }
        }
        .awaitAll()

    val appCache = self.caches.open(APP_CACHE.name).await()
    val appCacheKeys = appCache.keys().await()
    appCacheKeys.filter {
        val url = it.url
        filesToCacheAndRevisions.none {
            url.contains(it.key) && url.contains(it.value)
        }
    }.forEach {
        console.log("Deleting old cache request", it.url)
        appCache.delete(it)
    }
}

private fun <T> promise(block: suspend CoroutineScope.() -> T): Promise<T> {
    return Promise { resolve, reject ->
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                resolve(block())
            } catch (t: Throwable) {
                reject(t)
            }
        }
    }
}
