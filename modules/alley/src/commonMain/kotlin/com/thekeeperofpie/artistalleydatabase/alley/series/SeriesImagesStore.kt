package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.AlleyAniListApi
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageCache
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageType
import com.thekeeperofpie.artistalleydatabase.alley.user.ImageEntry
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.days

@SingletonScope
@Inject
class SeriesImagesStore(
    private val aniListApi: AlleyAniListApi,
    private val imageEntryDao: ImageEntryDao,
    private val imageCache: ImageCache,
    private val seriesEntryDao: SeriesEntryDao,
) {
    suspend fun getAllCachedImages(): Map<String, String> {
        val images = imageEntryDao.getAllImages()
            .groupBy { it.type }
            .mapValues { it.value.associate { it.imageId to it.url } }
        return seriesEntryDao.getSeriesIds()
            .mapNotNull {
                val aniListId = it.aniListId ?: return@mapNotNull null
                val imageUrl = images[ImageType.ANILIST.name]?.get(it.aniListId.toString()) ?:
                    return@mapNotNull null
                it.id to imageUrl
            }
            .associate { it }
    }

    suspend fun getImages(series: List<SeriesEntry>): Map<String, String> {
        val aniListIdMap = series.filter { it.aniListId != null }
            .associate { it.aniListId!!.toString() to it.id }

        val imageEntries = imageEntryDao.getImages(aniListIdMap.keys, ImageType.ANILIST)

        val seriesIdToImageEntry = imageEntries.associateBy { aniListIdMap[it.imageId] }
            .filterKeys { it != null }
            .mapKeys { it.key!! }

        val now = Clock.System.now()
        val fetchMediaIdToSeriesId = series
            .filter {
                val imageEntry = seriesIdToImageEntry[it.id]
                imageEntry == null ||
                        (now - Instant.fromEpochSeconds(imageEntry.createdAtSecondsUtc)) > 7.days
            }
            .filter { it.aniListId != null }
            .associate { it.aniListId!!.toInt() to it.id }

        val mediaImages = if (fetchMediaIdToSeriesId.isEmpty()) {
            emptyMap()
        } else {
            aniListApi.getMediaImages(fetchMediaIdToSeriesId.keys)
        }

        val fetchSeriesIdToUrl = mediaImages
            .mapNotNull { (key, value) -> fetchMediaIdToSeriesId[key]?.let { it to value } }

        if (mediaImages.isNotEmpty()) {
            imageEntryDao.insertImageEntries(mediaImages.map {
                ImageEntry(
                    imageId = it.key.toString(),
                    type = ImageType.ANILIST.name,
                    url = it.value,
                    createdAtSecondsUtc = now.epochSeconds,
                )
            })
        }

        try {
            imageCache.cache(mediaImages.map { it.value })
        } catch (ignored: Throwable) {
        }

        return seriesIdToImageEntry.mapValues { it.value.url } + fetchSeriesIdToUrl
    }
}
