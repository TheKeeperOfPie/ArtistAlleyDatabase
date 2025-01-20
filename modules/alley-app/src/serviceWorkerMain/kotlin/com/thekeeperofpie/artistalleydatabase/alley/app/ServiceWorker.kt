package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.generated.ComposeFile
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
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
import org.w3c.workers.Cache
import org.w3c.workers.ExtendableEvent
import org.w3c.workers.FetchEvent
import org.w3c.workers.InstallEvent
import org.w3c.workers.ServiceWorkerGlobalScope
import kotlin.js.Promise
import kotlin.js.RegExp

external val self: ServiceWorkerGlobalScope

private val APP_CACHE = CacheName("alley-app-v1")
private val IMAGE_CACHE = CacheName("alley-image-v1")
private val CACHES = setOf(APP_CACHE, IMAGE_CACHE)

value class CacheName(val name: String)

private val appFilesNetworkFirst = listOf(
    "/",
    "/index.html",
    "/composeApp.js",
    "/init.js",
    "/manifest.webmanifest",
    "/styles.css",
    "/composeResources/artistalleydatabase.modules.alley.generated.resources/values/strings.commonMain.cvr",
    "/composeResources/artistalleydatabase.modules.entry.generated.resources/values/strings.commonMain.cvr",
    "/composeResources/artistalleydatabase.modules.image.generated.resources/values/strings.commonMain.cvr",
    "/composeResources/artistalleydatabase.modules.utils_compose.generated.resources/values/strings.commonMain.cvr",
    "/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/database.sqlite",
    "/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/databaseHash.txt",
    "/vendors-node_modules_thekeeperofpie_alley-sqldelight-worker_sqldelight-worker_js.js",
    "/vendors-node_modules_sqlite_org_sqlite-wasm_sqlite-wasm_jswasm_sqlite3-bundler-friendly_mjs.js",
    "/vendors-node_modules_sqlite_org_sqlite-wasm_sqlite-wasm_jswasm_sqlite3-opfs-async-proxy_js.js",
)

private val appFilesCacheFirst = listOf(
    "/apple-touch-icon.png",
    "/favicon.ico",
    "/favicon.svg",
    "/favicon-96x96.png",
    "/web-app-manifest-192x192.png",
    "/web-app-manifest-512x512.png",
)

private const val catalogsDir =
    "/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/catalogs"
private const val ralliesDir =
    "/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/rallies"

private var jsFiles: Set<String> = mutableSetOf()
private var wasmFiles: Set<String> = mutableSetOf()

private val imagesToCache by lazy {
    ComposeFiles.catalogs.files
        .asSequence()
        .filterIsInstance<ComposeFile.Folder>()
        .flatMap { folder ->
            folder.files.filterIsInstance<ComposeFile.Image>()
                .map { image ->
                    "$catalogsDir/${folder.name}/${image.name}"
                }
        }
        .toList()
        .toTypedArray()
}

fun main() {
    console.log("Initializing service worker")
    self.addEventListener("install", { event ->
        console.log("serviceWorker.js install")
        event as InstallEvent
        event.waitUntil(
            promise {
                // TODO: Move caching to app install event and actually cache everything
                val appCache = self.caches.open(APP_CACHE.name).await()
                tryCaching(appCache, appFilesNetworkFirst)
                tryCaching(appCache, appFilesCacheFirst)

                // TODO: Find a way to inject the hashes
                jsFiles += findJsFiles("/composeApp.js")
                tryCaching(appCache, jsFiles)
                console.log("Cached .js files", jsFiles)

                wasmFiles += findWasmInJsFile("/composeApp.js")
                wasmFiles += jsFiles.flatMap { findWasmInJsFile(it) }
                wasmFiles += findWasmInJsFile("/vendors-node_modules_sqlite_org_sqlite-wasm_sqlite-wasm_jswasm_sqlite3-bundler-friendly_mjs.js")
                tryCaching(appCache, wasmFiles)
                console.log("Cached .wasm files", wasmFiles)

                val imageCache = self.caches.open(IMAGE_CACHE.name).await()
                imagesToCache
                    .also { console.log("Caching", it) }
                    .let(imageCache::addAll)
                    .await()
            }
        )
    })

    self.addEventListener("activate", { event ->
        event as ExtendableEvent
        console.log("Activating", event)
        event.waitUntil(
            promise {
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

                val imageCache = self.caches.open(IMAGE_CACHE.name).await()
                val imageKeys = imageCache.keys().await()
                imageKeys.filter { URL(it.url).pathname !in imagesToCache }
                    .forEach {
                        console.log("Deleting old image key", it.url)
                        imageCache.delete(it)
                    }
            }
        )
    })

    self.addEventListener("fetch", { event ->
        event as FetchEvent
        val request = event.request
        val url = URL(event.request.url)
        val urlPath = url.pathname
        val urlPathWithoutSlash = urlPath.removePrefix("/")
        console.log("Fetching", url)
        event.respondWith(
            promise {
                when {
                    urlPath.contains(catalogsDir) || urlPath.contains(ralliesDir) ->
                        fetchCacheFirst(request, urlPath)
                    urlPath in appFilesNetworkFirst ->
                        fetchNetworkFirstAndSave(request, urlPath, APP_CACHE)
                    urlPath in appFilesCacheFirst
                            || urlPathWithoutSlash in jsFiles
                            || urlPathWithoutSlash in wasmFiles ->
                        fetchCacheFirstAndSave(request, urlPath, APP_CACHE)
                    else -> {
                        console.error("Fetch request not handled", urlPath)
                        val result = runCatching { self.fetch(request).await() }
                        result.getOrNull()
                            ?: self.caches.match(request).await() as? Response
                            ?: result.getOrThrow()
                    }
                }
            }
        )
    })
}

private suspend fun fetchCacheFirst(request: Request, path: String): Response {
    val cached = self.caches.match(request).await()
    return if (cached is Response) {
        console.log("Cache hit", path)
        cached
    } else {
        console.log("Cache missed", path)
        self.fetch(request).await()
    }
}

private suspend fun fetchCacheFirstAndSave(request: Request, path: String, cacheName: CacheName): Response {
    val cached = self.caches.match(request).await()
    return if (cached is Response) {
        console.log("Cache hit", path)
        cached
    } else {
        console.log("Cache missed and saving", path)
        val response = self.fetch(request).await()
        self.caches.open(cacheName.name).await().put(request, response.clone()).await()
        response
    }
}

private suspend fun fetchNetworkFirstAndSave(
    request: Request,
    path: String,
    cacheName: CacheName,
): Response {
    val result = if (self.navigator.onLine) {
        runCatching { self.fetch(request).await() }
    } else {
        null
    }
    return if (result?.isSuccess != true) {
        val response = self.caches.match(request).await()
        if (response == null) {
            console.log("Network failed, cache failed", path)
        } else {
            console.log("Network failed, cache hit", path)
        }
        response as Response
    } else {
        val response = result.getOrThrow()
        self.caches.open(cacheName.name).await().put(request, response.clone()).await()
        console.log("Network success, cache saved", path)
        response
    }
}

private suspend fun openFileAsText(file: String) =
    runCatching { self.fetch(file).await().text().await() }.getOrNull()

private suspend fun findWasmInJsFile(file: String): Set<String> {
    val text = openFileAsText(file) ?: return emptySet()
    val regExp = RegExp("\\+ {0,1}\"(.{20}?\\.wasm)", "g")
    val results = mutableSetOf<String>()
    var match = regExp.exec(text)?.get(1)
    while (match != null) {
        console.log("Found .wasm $match")
        results += match
        match = regExp.exec(text)?.get(1)
    }
    return results
}

private suspend fun findJsFiles(file: String): List<String> {
    console.log("Searching", file)
    val text = openFileAsText(file) ?: return emptyList()
    val results = mutableSetOf<String>()
    val urlRegExp = RegExp("new URL\\(.{5,10}\\((.{0,5})\\)", "g")
    var urlMatch = urlRegExp.exec(text)?.get(1)
    while (urlMatch != null) {
        console.log("Matched", urlMatch)
        results += "$urlMatch.js"
        urlMatch = urlRegExp.exec(text)?.get(1)
    }

    // Minified version of __webpack_require__\.e\((.*?)\)
    val requireRegExp = RegExp("\\(\\)=>.\\..\\((.*?)\\)", "g")
    var requireMatch = requireRegExp.exec(text)?.get(1)
    while (requireMatch != null) {
        console.log("Matched", requireMatch)
        results += "$requireMatch.js"
        requireMatch = requireRegExp.exec(text)?.get(1)
    }
    return results.flatMap { findJsFiles(it) + it }
}

private suspend fun tryCaching(cache: Cache, files: Collection<String>) {
    files.forEach {
        console.log("Caching", it)
        try {
            cache.add(it).await()
        } catch (t: Throwable) {
            console.log("Failed to cache", it)
        }
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
