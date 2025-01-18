package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.generated.ComposeFile
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
import org.w3c.fetch.Response
import org.w3c.workers.FetchEvent
import org.w3c.workers.InstallEvent
import org.w3c.workers.ServiceWorkerGlobalScope
import kotlin.js.Promise

external val self: ServiceWorkerGlobalScope

private val CACHE_NAME = "alley-cache-v1"

fun main() {
    console.log("Initializing service worker")
    self.addEventListener("install", { event ->
        console.log("worker.js install")
        event as InstallEvent
        event.waitUntil(
            self.caches.open(CACHE_NAME)
                .then {
                    // TODO: Move caching to app install event and actually cache everything
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
                        .let(it::addAll)
                }
        )
    })

    self.addEventListener("fetch", { event ->
        event as FetchEvent
        self.caches.match(event.request)
            .then {
                (it as? Response)?.let {
                    console.log("Cached response", event.request.url)
                    event.respondWith(Promise.resolve(it))
                }
            }
    })
}
