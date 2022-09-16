package com.thekeeperofpie.artistalleydatabase.anilist.media

import com.thekeeperofpie.artistalleydatabase.android_utils.ApiRepository
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class MediaRepository(
    application: ScopedApplication,
    private val mediaEntryDao: MediaEntryDao,
    private val aniListApi: AniListApi,
) : ApiRepository<MediaEntry>(application) {

    override suspend fun fetch(id: String) = aniListApi.getMedia(id)
        .mapNotNull { it?.aniListMedia }
        .map {
            MediaEntry(
                id = it.id.toString(),
                title = MediaEntry.Title(
                    romaji = it.title?.romaji?.trim(),
                    english = it.title?.english?.trim(),
                    native = it.title?.native?.trim(),
                ),
                type = it.type?.rawValue?.let(MediaEntry.Type::valueOf),
                image = MediaEntry.CoverImage(
                    extraLarge = it.coverImage?.extraLarge,
                    large = it.coverImage?.large,
                    medium = it.coverImage?.medium,
                    color = it.coverImage?.color,
                ),
                synonyms = it.synonyms?.filterNotNull()?.map(String::trim),
            )
        }

    override suspend fun getLocal(id: String) = mediaEntryDao.getEntry(id.toInt())

    override suspend fun insertCachedEntry(value: MediaEntry) = mediaEntryDao.insertEntries(value)
}