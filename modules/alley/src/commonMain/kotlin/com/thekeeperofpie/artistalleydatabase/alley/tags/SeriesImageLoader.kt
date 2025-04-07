package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.Snapshot
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

// TODO: This entire mechanism is very suboptimal
class SeriesImageLoader(
    dispatchers: CustomDispatchers,
    scope: CoroutineScope,
    seriesImagesStore: SeriesImagesStore,
) {

    private val requests = mutableStateMapOf<String, Request>()

    private val requestChannel = Channel<SeriesEntry>(100, BufferOverflow.DROP_OLDEST)

    init {
        scope.launch(dispatchers.io) {
            requests.putAll(
                seriesImagesStore.getAllCachedImages().mapValues { Request.Done(it.value) }
            )
            while (isActive) {
                val chunk = mutableSetOf(requestChannel.receive())
                withTimeoutOrNull(5.seconds) {
                    chunk += requestChannel.receive()
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

                val images = seriesImagesStore.getImages(chunk.toList())
                val succeeded =
                    chunk.mapNotNull { series -> images[series.id]?.let { series to it } }
                        .associate { it.first.id to Request.Done(it.second) }
                val failed = chunk.filter { !images.contains(it.id) }
                    .associate { it.id to Request.Failed }
                requests.putAll(succeeded + failed)
            }
        }
    }

    private sealed interface Request {
        data class Pending(val series: SeriesEntry) : Request
        data class Done(val url: String) : Request
        data object Failed : Request
    }

    fun getSeriesImage(series: SeriesEntry): String? {
        val cached = requests[series.id].takeUnless { it is Request.Failed }
        if (cached == null) {
            requestChannel.trySend(series)
        }
        return (cached as? Request.Done)?.url
    }
}
