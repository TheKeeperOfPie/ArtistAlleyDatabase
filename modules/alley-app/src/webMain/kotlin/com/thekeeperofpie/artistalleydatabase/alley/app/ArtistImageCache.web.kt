package com.thekeeperofpie.artistalleydatabase.alley.app

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.GetArtistFavorites
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.await
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import org.w3c.fetch.Request
import org.w3c.workers.Cache
import org.w3c.workers.CacheQueryOptions
import kotlin.collections.plus
import kotlin.collections.plusAssign
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.get
import kotlin.js.length
import kotlin.text.get

@OptIn(ExperimentalWasmJsInterop::class)
@SingleIn(AppScope::class)
@Inject
class ArtistImageCache(private val artistEntryDao: ArtistEntryDao, private val userEntryDao: UserEntryDao) {

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(CustomDispatchers.IO) {
            val initialPair = null as List<GetArtistFavorites>? to emptyList<GetArtistFavorites>()
            handleEvents(listOf(Event.Verify))
            userEntryDao.getArtistFavorites()
                .scan(initialPair) { (previousPrevious, previous), next ->
                    if (previousPrevious == null) {
                        next to next
                    } else {
                        previous to next
                    }
                }
                .filter { it.first != it.second }
                .map { (previous, next) ->
                    if (previous == null) return@map emptyList()
                    val additions = next
                        .filter { artist -> previous.none { it.artistId == artist.artistId } }
                        .map { Event.Save(it.artistId) }
                    val removals = previous
                        .filter { artist -> next.none { it.artistId == artist.artistId } }
                        .map { Event.Delete(it.artistId) }
                    additions + removals
                }
                .collect { handleEvents(it) }
        }
    }

    private suspend fun handleEvents(events: List<Event>) {
        val cache = openCache()
        val existingKeys = cache.keys().await()
        events.forEach { event ->
            when (event) {
                is Event.Delete -> {
                    val entriesForArtist = DataYear.entries
                        .mapNotNull { artistEntryDao.getEntry(it, event.artistId) }
                        .ifEmpty { null }
                        ?: return

                    val keysForBooth = mutableListOf<Request>()
                    entriesForArtist.forEach {
                        val identifier = "${it.artist.year.folderName}/catalogs/${it.artist.booth}"
                        for (index in 0 until existingKeys.length) {
                            val request = existingKeys[index]
                            if (request?.url?.contains(identifier) == true) {
                                keysForBooth += request
                            }
                        }
                    }
                    keysForBooth.forEach {
                        cache.delete(it, CacheQueryOptions())
                    }
                }
                is Event.Save -> {
                    val entriesForArtist = DataYear.entries
                        .mapNotNull { artistEntryDao.getEntry(it, event.artistId) }
                        .ifEmpty { null }
                        ?: return

                    val keysForBooth = mutableListOf<Request>()
                    entriesForArtist.forEach {
                        val identifier = "${it.artist.year.folderName}/catalogs/${it.artist.booth}"
                        for (index in 0 until existingKeys.length) {
                            val request = existingKeys[index]
                            if (request?.url?.contains(identifier) == true) {
                                keysForBooth += request
                            }
                        }
                    }
                    val images = entriesForArtist.flatMap {
                        AlleyImageUtils.getArtistImages(
                            year = it.artist.year,
                            images = it.artist.images,
                        )
                    }
                    if (images.isEmpty()) return
                    val newToCache = mutableListOf<String>()
                    images.forEach { image ->
                        val path = image.uri.path ?: return@forEach
                        val existingRequest = keysForBooth.find { it.url.contains(path) }
                        if (existingRequest == null) {
                            newToCache += path
                        } else {
                            keysForBooth.remove(existingRequest)
                        }
                    }
                    keysForBooth.forEach {
                        cache.delete(it, CacheQueryOptions())
                    }
                    newToCache.forEach { cache.add(it).await() }
                }
                Event.Verify -> {
                    val stale = mutableListOf<Request>()
                    for (index in 0 until existingKeys.length) {
                        val request = existingKeys[index] ?: continue
                        val path = request.url
                            .let(Uri::parseOrNull)
                            ?.path
                        if (path == null || !AlleyImageUtils.artistImageExists(artistEntryDao, path)) {
                            ConsoleLogger.log("Remove stale image = $path")
                            stale += request
                        }
                    }
                    stale.forEach {
                        cache.delete(it, CacheQueryOptions())
                    }
                    val favorites = userEntryDao.getArtistFavorites().firstOrNull()
                    favorites?.forEach { artist ->
                        DataYear.entries.forEach {
                            val artist = artistEntryDao.getEntry(it, artist.artistId)
                                ?: return@forEach
                            val images = AlleyImageUtils.getArtistImages(
                                year = it,
                                images = artist.artist.images,
                            )
                            images.forEach { image ->
                                var matchingKey: Request? = null
                                for (index in 0 until existingKeys.length) {
                                    val request = existingKeys[index]
                                    val path = image.uri.path
                                    if (path != null && request?.url?.contains(path) == true) {
                                        matchingKey = request
                                        break
                                    }
                                }

                                if (matchingKey == null) {
                                    cache.add(image.uri.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun openCache(): Cache = window.caches.open("alley-image-v1").await()

    sealed interface Event {
        data object Verify : Event
        data class Save(val artistId: String) : Event
        data class Delete(val artistId: String) : Event
    }
}
