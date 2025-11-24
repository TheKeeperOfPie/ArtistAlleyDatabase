package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.collection.LruCache
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import dev.zacsweers.metro.Inject

@Inject
class SeriesEntryCache(
    private val seriesEntryDao: SeriesEntryDao,
) {
    val cache = LruCache<String, SeriesInfo>(500)

    suspend fun getSeries(ids: List<String>): List<SeriesInfo> {
        val missing = mutableListOf<String>()
        val cachedSeries = mutableListOf<SeriesInfo>()
        ids.forEach {
            val cached = cache[it]
            if (cached != null) {
                cachedSeries += cached
            } else {
                missing += it
            }
        }
        val queried = seriesEntryDao.getSeriesByIds(missing)
            .onEach { cache.put(it.id, it) }
        return ids.mapNotNull { id ->
            cachedSeries.find { it.id == id } ?: queried.find { it.id == id }
        }
    }
}
