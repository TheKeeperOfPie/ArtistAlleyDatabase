package com.thekeeperofpie.artistalleydatabase.anilist.media

import artistalleydatabase.modules.anilist.generated.resources.Res
import artistalleydatabase.modules.anilist.generated.resources.aniList_error_fetching_series
import com.anilist.data.fragment.AniListMedia
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.ApiRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class MediaRepository(
    scope: ApplicationScope,
    private val mediaEntryDao: MediaEntryDao,
    private val aniListApi: AniListApi,
) : ApiRepository<MediaEntry>(scope) {

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
        Res.string.aniList_error_fetching_series to e
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
