package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.Snapshot
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

@SingleIn(AppScope::class)
@Inject
class SeriesImageLoader(
    appScope: ApplicationScope,
    dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
) {
    private val requests = mutableStateMapOf<String, Request>()

    private val requestChannel = Channel<SeriesImageInfo>(200, BufferOverflow.DROP_OLDEST)

    init {
        appScope.launch(dispatchers.io) {
            val allCached = seriesImagesStore.getAllCachedImages()
            requests.putAll(allCached.seriesIdToUrls.mapValues { Request.Done(it.value) })
            allCached.staleIds.forEach {
                seriesImagesStore.deleteStale(it)
            }
            while (isActive) {
                val chunk = mutableSetOf(requestChannel.receive())
                withTimeoutOrNull(1.seconds) {
                    while (isActive && chunk.size < 25) {
                        chunk += requestChannel.receive()
                    }
                }
                Snapshot.withMutableSnapshot {
                    val map = requests.toMap()
                    val newEntries = chunk.associate {
                        it.id to (map[it.id]
                            ?.takeUnless { it is Request.Failed }
                            ?: Request.Pending(it))
                    }
                    requests.putAll(newEntries)
                }

                val series = chunk.toList()
                val cacheResult = seriesImagesStore.getCachedImages(series)
                val cachedImages = cacheResult.seriesIdsToImages
                val cachedSucceeded =
                    chunk.mapNotNull { series -> cachedImages[series.id]?.let { series to it } }
                        .associate { it.first.id to Request.Done(it.second) }
                requests.putAll(cachedSucceeded)

                val allImages = seriesImagesStore.getAllImages(series, cacheResult)
                val succeeded =
                    chunk.mapNotNull { series -> allImages[series.id]?.let { series to it } }
                        .associate { it.first.id to Request.Done(it.second) }
                val failed = chunk.filter { !allImages.contains(it.id) }
                    .associate { it.id to Request.Failed }
                requests.putAll(succeeded + failed)
            }
        }
    }

    private sealed interface Request {
        data class Pending(val series: SeriesImageInfo) : Request
        data class Done(val url: String) : Request
        data object Failed : Request
    }

    fun getSeriesImage(series: SeriesInfo) = getSeriesImage(series.toImageInfo())

    fun getSeriesImage(series: SeriesImageInfo): String? {
        val cached = requests[series.id]
        if (cached == null) {
            requestChannel.trySend(series)
        }
        return (cached as? Request.Done)?.url
    }

    fun getCachedSeriesImage(seriesId: String) = (requests[seriesId] as? Request.Done)?.url
}
