package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.AlleyAniListApi
import com.thekeeperofpie.artistalleydatabase.alley.AlleyWikipediaApi
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
    private val wikipediaApi: AlleyWikipediaApi,
) {
    suspend fun getAllCachedImages(): Map<String, String> {
        val images = imageEntryDao.getAllImages()
            .groupBy { it.type }
            .mapValues { it.value.associate { it.imageId to it.url } }
        return seriesEntryDao.getSeriesIds()
            .mapNotNull {
                val imageUrl = it.aniListId?.toString()?.let {images[ImageType.ANILIST.name]?.get(it) }
                    ?: it.wikipediaId?.toString()?.let {images[ImageType.WIKIPEDIA.name]?.get(it) }
                    ?: return@mapNotNull null
                it.id to imageUrl
            }
            .associate { it }
    }

    suspend fun getImages(series: List<SeriesEntry>): Map<String, String> {
        val seriesIdToAniListIds = series.filter { it.aniListId != null }
            .associate { it.id to it.aniListId!!.toString() }
        val seriesIdToWikipediaIds = series.filter { it.wikipediaId != null }
            .associate { it.id to it.wikipediaId!!.toString() }
            .filterKeys { it !in seriesIdToAniListIds }

        val cachedAniListImages = imageEntryDao
            .getImages(seriesIdToAniListIds.values, ImageType.ANILIST)
            .associateBy { it.imageId }
        val cachedWikipediaImages = imageEntryDao
            .getImages(seriesIdToWikipediaIds.values, ImageType.WIKIPEDIA)
            .associateBy { it.imageId }

        val now = Clock.System.now()
        val missingAniListIds = seriesIdToAniListIds.values.filter {
            cachedAniListImages[it]?.takeUnless {
                (now - Instant.fromEpochSeconds(it.createdAtSecondsUtc)) > 7.days
            } == null
        }

        val missingWikipediaIds = seriesIdToWikipediaIds.values.filter {
            cachedWikipediaImages[it]?.takeUnless {
                (now - Instant.fromEpochSeconds(it.createdAtSecondsUtc)) > 7.days
            } == null
        }

        val allAniListImages = cachedAniListImages.mapValues { it.value.url }.toMutableMap()
        if (missingAniListIds.isNotEmpty()) {
            val newAniListImages =
                aniListApi.getMediaImages(missingAniListIds.mapNotNull { it.toIntOrNull() })
            if (newAniListImages.isNotEmpty()) {
                imageEntryDao.insertImageEntries(newAniListImages.map {
                    ImageEntry(
                        imageId = it.key.toString(),
                        type = ImageType.ANILIST.name,
                        url = it.value,
                        createdAtSecondsUtc = now.epochSeconds,
                    )
                })
                try {
                    imageCache.cache(newAniListImages.values)
                } catch (_: Throwable) {
                }
                allAniListImages += newAniListImages.mapKeys { it.key.toString() }
            }
        }

        val allWikipediaImages = cachedWikipediaImages.mapValues { it.value.url }.toMutableMap()
        if (missingWikipediaIds.isNotEmpty()) {
            val newWikipediaImages = wikipediaApi.getMediaImages(missingWikipediaIds)
            if (newWikipediaImages.isNotEmpty()) {
                imageEntryDao.insertImageEntries(newWikipediaImages.map {
                    ImageEntry(
                        imageId = it.key.toString(),
                        type = ImageType.WIKIPEDIA.name,
                        url = it.value,
                        createdAtSecondsUtc = now.epochSeconds,
                    )
                })
                try {
                    imageCache.cache(newWikipediaImages.values)
                } catch (_: Throwable) {
                }
                allWikipediaImages += newWikipediaImages.mapKeys { it.key.toString() }
            }
        }
        return series.mapNotNull {
            val imageUrl = when {
                it.aniListId != null -> allAniListImages[it.aniListId.toString()]
                it.wikipediaId != null -> allWikipediaImages[it.wikipediaId.toString()]
                else -> null
            } ?: return@mapNotNull null
            it.id to imageUrl
        }.associate { it }
    }
}
