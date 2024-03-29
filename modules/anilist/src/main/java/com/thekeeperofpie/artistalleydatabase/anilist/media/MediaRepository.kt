package com.thekeeperofpie.artistalleydatabase.anilist.media

import com.anilist.fragment.AniListMedia
import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.R

class MediaRepository(
    application: ScopedApplication,
    private val mediaEntryDao: MediaEntryDao,
    private val aniListApi: AniListApi,
) : ApiRepository<MediaEntry>(application) {

    override suspend fun fetch(id: String) = aniListApi.getMedia(id)?.let(::makeEntry)

    override suspend fun getLocal(id: String) = mediaEntryDao.getEntry(id.toInt())

    override suspend fun insertCachedEntry(value: MediaEntry) = mediaEntryDao.insertEntries(value)

    override suspend fun ensureSaved(ids: List<String>) = try {
        (ids - mediaEntryDao.getEntriesById(ids).toSet())
            .map(String::toInt)
            .let { aniListApi.getMedias(it) }
            .map(::makeEntry)
            .forEach { insertCachedEntry(it) }
        null
    } catch (e: Exception) {
        R.string.aniList_error_fetching_series to e
    }

    private fun makeEntry(media: AniListMedia) = MediaEntry(
        id = media.id.toString(),
        title = MediaEntry.Title(
            romaji = media.title?.romaji?.trim(),
            english = media.title?.english?.trim(),
            native = media.title?.native?.trim(),
        ),
        type = media.type?.rawValue?.let(MediaEntry.Type::valueOf),
        image = MediaEntry.CoverImage(
            extraLarge = media.coverImage?.extraLarge,
            large = media.coverImage?.large,
            medium = media.coverImage?.medium,
            color = media.coverImage?.color,
        ),
        synonyms = media.synonyms?.filterNotNull()?.map(String::trim),
    )
}