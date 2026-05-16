package com.thekeeperofpie.artistalleydatabase.alley.app.serviceworker

import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.w3c.dom.url.URL
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import org.w3c.workers.ExtendableEvent
import org.w3c.workers.ExtendableMessageEvent
import org.w3c.workers.FetchEvent
import org.w3c.workers.ServiceWorkerGlobalScope
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.js.Promise

external val self: ServiceWorkerGlobalScope

private val APP_CACHE = CacheName("alley-app-v1")
private val IMAGE_CACHE = CacheName("alley-image-v1")

// Must keep in sync with ImageCache.wasmJs.kt
private val MEDIA_IMAGE_CACHE = CacheName("media-image-v1")
private val CACHES = setOf(APP_CACHE, IMAGE_CACHE, MEDIA_IMAGE_CACHE)

value class CacheName(val name: String)

private const val filesToCacheInput = "CACHE_INPUT"
private val filesToCacheAndRevisions = filesToCacheInput.lineSequence()
    .associate { it.substringBeforeLast("-") to it.substringAfterLast("-") }

private fun supportsWasm(): Boolean = js(
    """
    const simpleWasmModule = new Uint8Array([
        0,  97, 115, 109,   1,   0,   0,  0,   1,   8,   2,  95,
        1, 120,   0,  96,   0,   0,   3,  3,   2,   1,   1,  10,
       14,   2,   6,   0,   6,  64,  25, 11,  11,   5,   0, 208,
      112,  26,  11,   0,  45,   4, 110, 97, 109, 101,   1,  15,
        2,   0,   5, 102, 117, 110,  99, 48,   1,   5, 102, 117,
      110,  99,  49,   4,   8,   1,   0,  5, 116, 121, 112, 101,
       48,  10,  11,   1,   0,   1,   0,  6, 102, 105, 101, 108,
      100,  48
        ]);

    return typeof WebAssembly !== "undefined" &&
        typeof WebAssembly.validate === "function" &&
        WebAssembly.validate(simpleWasmModule);
"""
)

fun main() {
    console.log("Initializing service worker")

    self.addEventListener("install", { event ->
        console.log("serviceWorker.js install")
        event as ExtendableEvent
        event.waitUntil(
            promise {
                val supportsWasm = supportsWasm()
                val urlsToCache = filesToCacheAndRevisions
                    .filter {
                        // Avoid loading JS compat file if not necessary
                        !supportsWasm || it.key != "originJsAlley-app.js"
                    }
                    .map {
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
        if (url.host.contains("anilist.co") || url.host.contains("wikipedia.org")) {
            event.respondWith(self.fetch(request))
        } else {
            event.respondWith(
                promise {
                    val response = if (urlPath == "/") {
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

                    // Fix a bug with Pages serving the wrong Content-Type
                    val url = request.url
                    val contentType = when {
                        url.endsWith(".wasm") -> "application/wasm"
                        else -> null
                    }
                    if (contentType != null && contentType != response.headers.get("Content-Type")) {
                        Response(
                            response.body,
                            ResponseInit(
                                headers = response.headers,
                                status = response.status,
                                statusText = response.statusText,
                            )
                        ).apply {
                            headers.set("Content-Type", contentType)
                        }
                    } else {
                        response
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

private suspend fun cleanUpCaches() = coroutineScope {
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

private fun <T> promise(block: suspend () -> T) =
    Promise { resolve, reject ->
        block.startCoroutine(completion = object : Continuation<T> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                result.fold(resolve, reject)
            }
        })
    }
