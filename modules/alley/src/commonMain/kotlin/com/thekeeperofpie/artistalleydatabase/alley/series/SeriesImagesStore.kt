package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.AlleyAniListApi
import com.thekeeperofpie.artistalleydatabase.alley.AlleyTmdbApi
import com.thekeeperofpie.artistalleydatabase.alley.AlleyWikipediaApi
import com.thekeeperofpie.artistalleydatabase.alley.GetImageEntries
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageCache
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageType
import com.thekeeperofpie.artistalleydatabase.alley.user.ImageEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TmdbType
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@SingleIn(AppScope::class)
@Inject
class SeriesImagesStore(
    private val aniListApi: AlleyAniListApi,
    private val imageEntryDao: ImageEntryDao,
    private val imageCache: ImageCache,
    private val seriesEntryDao: SeriesEntryDao,
    private val tmdbApi: AlleyTmdbApi,
    private val wikipediaApi: AlleyWikipediaApi,
) {
    suspend fun getAllCachedImages(): Map<String, String> {
        val images = imageEntryDao.getAllImages()
            .groupBy { it.type }
            .mapValues { it.value.associate { it.imageId to it.url } }
        return seriesEntryDao.getSeriesIds()
            .mapNotNull {
                val imageUrl =
                    it.aniListId?.toString()?.let { images[ImageType.ANILIST.name]?.get(it) }
                        ?: it.wikipediaId?.toString()
                            ?.let { images[ImageType.WIKIPEDIA.name]?.get(it) }
                        ?: return@mapNotNull null
                it.id to imageUrl
            }
            .associate { it }
    }

    suspend fun getCachedImages(series: List<SeriesImageInfo>): CacheResult {
        val mediaImageIds = series.toImageIds()

        val cachedAniListImages = imageEntryDao
            .getImages(mediaImageIds.aniListIds.values, ImageType.ANILIST)
            .associateBy { it.imageId }
        val cachedWikipediaImages = imageEntryDao
            .getImages(mediaImageIds.wikipediaIds.values, ImageType.WIKIPEDIA)
            .associateBy { it.imageId }
        val cachedTmdbImages = imageEntryDao
            .getImages(mediaImageIds.tmdbIds.values.map { it.first }, ImageType.WIKIPEDIA)
            .associateBy { it.imageId }

        val seriesIdsToImages = series.mapNotNull {
            val imageUrl = when {
                it.aniListId != null -> cachedAniListImages[it.aniListId.toString()]?.url
                it.wikipediaId != null -> cachedWikipediaImages[it.wikipediaId.toString()]?.url
                it.tmdbId != null -> cachedTmdbImages[it.tmdbId]?.url
                else -> null
            } ?: return@mapNotNull null
            it.id to imageUrl
        }.associate { it }

        return CacheResult(
            aniListImages = cachedAniListImages,
            wikipediaImages = cachedWikipediaImages,
            tmdbImages = cachedTmdbImages,
            seriesIdsToImages = seriesIdsToImages,
        )
    }

    suspend fun getAllImages(
        series: List<SeriesImageInfo>,
        cacheResult: CacheResult,
    ): Map<String, String> {
        val mediaImageIds = series.toImageIds()
        val (cachedAniListImages, cachedWikipediaImages, cachedTmdbImages, _) = cacheResult
        val now = Clock.System.now()
        val missingAniListIds = mediaImageIds.aniListIds.values.filter {
            cachedAniListImages[it]?.takeUnless {
                (now - Instant.fromEpochSeconds(it.createdAtSecondsUtc)) > 7.days
            } == null
        }

        val missingWikipediaIds = mediaImageIds.wikipediaIds.values.filter {
            cachedWikipediaImages[it]?.takeUnless {
                (now - Instant.fromEpochSeconds(it.createdAtSecondsUtc)) > 7.days
            } == null
        }

        val missingTmdbIds = mediaImageIds.tmdbIds.values.filter {
            cachedTmdbImages[it.first]?.takeUnless {
                (now - Instant.fromEpochSeconds(it.createdAtSecondsUtc)) > 7.days
            } == null
        }

        val allAniListImages = cachedAniListImages.mapValues { it.value.url }
            .plus(loadAniListImages(now, missingAniListIds))

        val allWikipediaImages = cachedWikipediaImages.mapValues { it.value.url }
            .plus(loadWikipediaImages(now, missingWikipediaIds))

        val allTmdbImages = cachedTmdbImages.mapValues { it.value.url }
            .plus(loadTmdbImages(now, missingTmdbIds))

        return series.mapNotNull {
            val imageUrl = when {
                it.aniListId != null -> allAniListImages[it.aniListId.toString()]
                it.wikipediaId != null -> allWikipediaImages[it.wikipediaId.toString()]
                it.tmdbId != null -> allTmdbImages[it.tmdbId]
                else -> null
            } ?: return@mapNotNull null
            it.id to imageUrl
        }.associate { it }
    }

    private suspend fun loadAniListImages(now: Instant, ids: List<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val newAniListImages =
            aniListApi.getMediaImages(ids.mapNotNull { it.toIntOrNull() })
        if (newAniListImages.isEmpty()) return emptyMap()
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
        return newAniListImages.mapKeys { it.key.toString() }
    }

    private suspend fun loadWikipediaImages(now: Instant, ids: List<String>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val newWikipediaImages = wikipediaApi.getMediaImages(ids)
        if (newWikipediaImages.isEmpty()) return emptyMap()
        imageEntryDao.insertImageEntries(newWikipediaImages.map {
            ImageEntry(
                imageId = it.key,
                type = ImageType.WIKIPEDIA.name,
                url = it.value,
                createdAtSecondsUtc = now.epochSeconds,
            )
        })
        try {
            imageCache.cache(newWikipediaImages.values)
        } catch (_: Throwable) {
        }
        return newWikipediaImages.mapKeys { it.key }
    }

    private suspend fun loadTmdbImages(
        now: Instant,
        ids: List<Pair<String, TmdbType>>,
    ): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val newTmdbImages = tmdbApi.getMediaImages(ids)
        if (newTmdbImages.isEmpty()) return emptyMap()
        imageEntryDao.insertImageEntries(newTmdbImages.map {
            ImageEntry(
                imageId = it.key,
                type = ImageType.TMDB.name,
                url = it.value,
                createdAtSecondsUtc = now.epochSeconds,
            )
        })
        try {
            imageCache.cache(newTmdbImages.values)
        } catch (_: Throwable) {
        }
        return newTmdbImages.mapKeys { it.key }
    }

    private fun List<SeriesImageInfo>.toImageIds() =
        MediaImageIds(
            aniListIds = filter { it.aniListId != null }
                .associate { it.id to it.aniListId!!.toString() },
            wikipediaIds = filter { it.wikipediaId != null }
                .associate { it.id to it.wikipediaId!!.toString() },
            tmdbIds = filter { it.tmdbId != null && it.tmdbType != null && it.tmdbType != TmdbType.NONE }
                .associate { it.id to (it.tmdbId!! to it.tmdbType!!) },
        )


    data class CacheResult(
        val aniListImages: Map<String, GetImageEntries>,
        val wikipediaImages: Map<String, GetImageEntries>,
        val tmdbImages: Map<String, GetImageEntries>,
        val seriesIdsToImages: Map<String, String>,
    )

    data class MediaImageIds(
        val aniListIds: Map<String, String>,
        val wikipediaIds: Map<String, String>,
        val tmdbIds: Map<String, Pair<String, TmdbType>>,
    )
}
