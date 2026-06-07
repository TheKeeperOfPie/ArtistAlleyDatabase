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
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@SingleIn(AppScope::class)
@Inject
class SeriesImagesStore(
    private val aniListApi: AlleyAniListApi,
    appScope: ApplicationScope,
    private val imageEntryDao: ImageEntryDao,
    private val imageCache: ImageCache,
    private val seriesEntryDao: SeriesEntryDao,
    private val tmdbApi: AlleyTmdbApi,
    private val wikipediaApi: AlleyWikipediaApi,
) {
    val seriesAndImageIds = appScope.async(start = CoroutineStart.LAZY) {
        seriesEntryDao.getSeriesAndImageIds()
    }

    suspend fun getAllCachedImages(): AllCachedResult {
        val imageEntries = imageEntryDao.getAllImages()
            .groupBy { ImageType.fromSerializedName(it.type) }
            .mapValues { it.value.associateBy { it.imageId } }
        val images = seriesAndImageIds.await()
            .values
            .mapNotNull {
                val image =
                    it.aniListId?.toString()?.let { imageEntries[ImageType.ANILIST]?.get(it) }
                        ?: it.openLibraryId?.let { imageEntries[ImageType.OPEN_LIBRARY]?.get(it) }
                        ?: it.steamId?.let { imageEntries[ImageType.STEAM]?.get(it) }
                        ?: it.tmdbId?.let { imageEntries[ImageType.TMDB]?.get(it) }
                        ?: it.wikipediaId?.toString()
                            ?.let { imageEntries[ImageType.WIKIPEDIA]?.get(it) }
                        ?: return@mapNotNull null
                it.id to image.url
            }
            .associate { it }
        val staleIds = imageEntries[null]
            ?.values
            ?.map { it.imageId to it.type }
            .orEmpty()
        return AllCachedResult(seriesIdToUrls = images, staleIds = staleIds)
    }

    suspend fun deleteStale(idAndType: Pair<String, String>) =
        imageEntryDao.deleteStale(idAndType.first, idAndType.second)

    suspend fun getCachedImages(series: List<SeriesImageInfo>): CacheResult {
        val mediaImageIds = series.toImageIds()

        val cachedAniListImages = imageEntryDao
            .getImages(mediaImageIds.aniListIds.values, ImageType.ANILIST)
            .associateBy { it.imageId }
        val cachedOpenLibraryImages = imageEntryDao
            .getImages(mediaImageIds.openLibraryIds.values, ImageType.OPEN_LIBRARY)
            .associateBy { it.imageId }
        val cachedSteamImages = imageEntryDao
            .getImages(mediaImageIds.steamIds.values.map { it.appId }, ImageType.STEAM)
            .associateBy { it.imageId }
        val cachedTmdbImages = imageEntryDao
            .getImages(mediaImageIds.tmdbIds.values.map { it.first }, ImageType.TMDB)
            .associateBy { it.imageId }
        val cachedWikipediaImages = imageEntryDao
            .getImages(mediaImageIds.wikipediaIds.values, ImageType.WIKIPEDIA)
            .associateBy { it.imageId }

        val seriesIdsToImages = series.mapNotNull {
            val imageUrl = when {
                it.aniListId != null -> cachedAniListImages[it.aniListId.toString()]?.url
                it.openLibraryId != null -> cachedOpenLibraryImages[it.openLibraryId]?.url
                it.steamId != null -> cachedSteamImages[it.steamId]?.url
                it.tmdbId != null -> cachedTmdbImages[it.tmdbId]?.url
                it.wikipediaId != null -> cachedWikipediaImages[it.wikipediaId.toString()]?.url
                else -> null
            } ?: return@mapNotNull null
            it.id to imageUrl
        }.associate { it }

        return CacheResult(
            aniListImages = cachedAniListImages,
            steamImages = cachedSteamImages,
            openLibraryImages = cachedOpenLibraryImages,
            tmdbImages = cachedTmdbImages,
            wikipediaImages = cachedWikipediaImages,
            seriesIdsToImages = seriesIdsToImages,
        )
    }

    suspend fun getAllImages(
        series: List<SeriesImageInfo>,
        cacheResult: CacheResult,
    ): Map<String, String> {
        val mediaImageIds = series.toImageIds()
        val now = Clock.System.now()
        val missingAniListIds = mediaImageIds.aniListIds.values.filter {
            cacheResult.aniListImages[it]?.takeUnless {
                (now - Instant.fromEpochSeconds(it.createdAtSecondsUtc)) > 7.days
            } == null
        }

        val missingOpenLibraryIds = mediaImageIds.openLibraryIds.values.filter {
            // Cache expiry doesn't matter since these are hardcoded
            cacheResult.openLibraryImages[it] == null
        }

        val missingSteamIds = mediaImageIds.steamIds.values.filter {
            // Cache expiry doesn't matter since these are hardcoded
            cacheResult.steamImages[it.appId] == null
        }

        val missingTmdbIds = mediaImageIds.tmdbIds.values.filter {
            cacheResult.tmdbImages[it.first]?.takeUnless {
                (now - Instant.fromEpochSeconds(it.createdAtSecondsUtc)) > 7.days
            } == null
        }

        val missingWikipediaIds = mediaImageIds.wikipediaIds.values.filter {
            cacheResult.wikipediaImages[it]?.takeUnless {
                (now - Instant.fromEpochSeconds(it.createdAtSecondsUtc)) > 7.days
            } == null
        }

        val allAniListImages = cacheResult.aniListImages.mapValues { it.value.url }
            .plus(loadAniListImages(now, missingAniListIds))

        val allOpenLibraryImages = cacheResult.openLibraryImages.mapValues { it.value.url }
            .plus(loadOpenLibraryImages(now, missingOpenLibraryIds))

        val allSteamImages = cacheResult.steamImages.mapValues { it.value.url }
            .plus(loadSteamImages(now, missingSteamIds))

        val allTmdbImages = cacheResult.tmdbImages.mapValues { it.value.url }
            .plus(loadTmdbImages(now, missingTmdbIds))

        val allWikipediaImages = cacheResult.wikipediaImages.mapValues { it.value.url }
            .plus(loadWikipediaImages(now, missingWikipediaIds))

        return series.mapNotNull {
            val imageUrl = when {
                it.aniListId != null -> allAniListImages[it.aniListId.toString()]
                it.openLibraryId != null -> allOpenLibraryImages[it.openLibraryId]
                it.steamId != null -> allSteamImages[it.steamId]
                it.tmdbId != null -> allTmdbImages[it.tmdbId]
                it.wikipediaId != null -> allWikipediaImages[it.wikipediaId.toString()]
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
                type = ImageType.ANILIST.serializedName,
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
                type = ImageType.WIKIPEDIA.serializedName,
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
                type = ImageType.TMDB.serializedName,
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

    private suspend fun loadOpenLibraryImages(
        now: Instant,
        ids: List<String>,
    ): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val newOpenLibraryImages = ids.associateWith {
            "https://covers.openlibrary.org/b/OLID/$it-M.jpg"
        }
        imageEntryDao.insertImageEntries(newOpenLibraryImages.map {
            ImageEntry(
                imageId = it.key,
                type = ImageType.OPEN_LIBRARY.serializedName,
                url = it.value,
                createdAtSecondsUtc = now.epochSeconds,
            )
        })
        try {
            imageCache.cache(newOpenLibraryImages.values)
        } catch (_: Throwable) {
        }
        return newOpenLibraryImages.mapKeys { it.key }
    }

    private suspend fun loadSteamImages(now: Instant, ids: List<SteamImageId>): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val newSteamImages = ids.associate {
            val path = it.imagePath?.removePrefix("/") ?: "library_600x900.jpg"
            it.appId to "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/${it.appId}/$path"
        }
        imageEntryDao.insertImageEntries(newSteamImages.map {
            ImageEntry(
                imageId = it.key,
                type = ImageType.STEAM.serializedName,
                url = it.value,
                createdAtSecondsUtc = now.epochSeconds,
            )
        })
        try {
            imageCache.cache(newSteamImages.values)
        } catch (_: Throwable) {
        }
        return newSteamImages.mapKeys { it.key }
    }

    private fun List<SeriesImageInfo>.toImageIds() =
        MediaImageIds(
            aniListIds = filter { it.aniListId != null }
                .associate { it.id to it.aniListId!!.toString() },
            openLibraryIds = filter { it.openLibraryId != null }
                .associate { it.id to it.openLibraryId!! },
            steamIds = filter { it.steamId != null }
                .associate { it.id to SteamImageId(it.steamId!!, it.steamImagePath) },
            tmdbIds = filter { it.tmdbId != null && it.tmdbType != null && it.tmdbType != TmdbType.NONE }
                .associate { it.id to (it.tmdbId!! to it.tmdbType!!) },
            wikipediaIds = filter { it.wikipediaId != null }
                .associate { it.id to it.wikipediaId!!.toString() },
        )

    data class AllCachedResult(
        val seriesIdToUrls: Map<String, String>,
        val staleIds: List<Pair<String, String>>,
    )

    data class CacheResult(
        val aniListImages: Map<String, GetImageEntries>,
        val openLibraryImages: Map<String, GetImageEntries>,
        val steamImages: Map<String, GetImageEntries>,
        val tmdbImages: Map<String, GetImageEntries>,
        val wikipediaImages: Map<String, GetImageEntries>,
        val seriesIdsToImages: Map<String, String>,
    )

    data class MediaImageIds(
        val aniListIds: Map<String, String>,
        val openLibraryIds: Map<String, String>,
        val steamIds: Map<String, SteamImageId>,
        val tmdbIds: Map<String, Pair<String, TmdbType>>,
        val wikipediaIds: Map<String, String>,
    )

    data class SteamImageId(
        val appId: String,
        val imagePath: String?,
    )
}
