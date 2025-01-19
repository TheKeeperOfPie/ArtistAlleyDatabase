package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.generated.ComposeFile
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.fetch.Response
import org.w3c.workers.Cache
import org.w3c.workers.FetchEvent
import org.w3c.workers.InstallEvent
import org.w3c.workers.ServiceWorkerGlobalScope
import kotlin.js.Promise
import kotlin.js.RegExp

external val self: ServiceWorkerGlobalScope

private val CACHE_NAME = "alley-cache-v1"

private val appFiles = listOf(
    "/",
    "/index.html",
    "/composeApp.js",
    "/init.js",
    "/manifest.webmanifest",
    "/styles.css",
    "/worker.js",
    "/apple-touch-icon.png",
    "/favicon.ico",
    "/favicon.svg",
    "/favicon-96x96.png",
    "/web-app-manifest-192x192.png",
    "/web-app-manifest-512x512.png",
    "/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/database.sqlite",
    "/composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/databaseHash.txt",
    "/composeResources/artistalleydatabase.modules.alley.generated.resources/values/strings.commonMain.cvr",
    "/composeResources/artistalleydatabase.modules.entry.generated.resources/values/strings.commonMain.cvr",
    "/composeResources/artistalleydatabase.modules.image.generated.resources/values/strings.commonMain.cvr",
    "/composeResources/artistalleydatabase.modules.utils_compose.generated.resources/values/strings.commonMain.cvr",
    "/vendors-node_modules_thekeeperofpie_alley-sqldelight-worker_sqldelight-worker_js.js",
    "/vendors-node_modules_sqlite_org_sqlite-wasm_sqlite-wasm_jswasm_sqlite3-bundler-friendly_mjs.js",
    "/vendors-node_modules_sqlite_org_sqlite-wasm_sqlite-wasm_jswasm_sqlite3-opfs-async-proxy_js.js",
)

private var jsFiles: Set<String> = mutableSetOf()
private var wasmFiles: Set<String> = mutableSetOf()

fun main() {
    console.log("Initializing service worker")
    self.addEventListener("install", { event ->
        console.log("worker.js install")
        event as InstallEvent
        event.waitUntil(
            promise {
                // TODO: Move caching to app install event and actually cache everything
                val cache = self.caches.open(CACHE_NAME).await()
                tryCaching(cache, appFiles)

                // TODO: Find a way to inject the hashes
                jsFiles += findJsFiles("/composeApp.js")
                tryCaching(cache, jsFiles)
                console.log("Cached .js files", jsFiles)

                wasmFiles += findWasmInJsFile("/composeApp.js")
                wasmFiles += jsFiles.flatMap { findWasmInJsFile(it) }
                wasmFiles += findWasmInJsFile("/vendors-node_modules_sqlite_org_sqlite-wasm_sqlite-wasm_jswasm_sqlite3-bundler-friendly_mjs.js")
                tryCaching(cache, wasmFiles)
                console.log("Cached .wasm files", wasmFiles)

                ComposeFiles.catalogs.files
                    .asSequence()
                    .filterIsInstance<ComposeFile.Folder>()
                    .flatMap { folder ->
                        folder.files.filterIsInstance<ComposeFile.Image>()
                            .map { image ->
                                "composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/catalogs/${folder.name}/${image.name}"
                            }
                    }
                    .take(100)
                    .toList()
                    .toTypedArray()
                    .also { console.log("Caching", it) }
                    .let(cache::addAll)
                    .await()
            }
        )
    })

    self.addEventListener("fetch", { event ->
        event as FetchEvent
        console.log("Fetching", event.request.url)
        event.respondWith(
            promise {
                (self.caches.match(event.request).await() as? Response)
                    ?.also { console.log("Cached response", event.request.url) }
                    ?: self.fetch(event.request).await()
            }
        )
    })
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
    console.log("require text", text)
    console.log("requireMatch", requireMatch)
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

private fun <T> promise(block: suspend () -> T): Promise<T> {
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
