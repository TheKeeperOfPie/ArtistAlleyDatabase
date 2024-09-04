package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.compose.runtime.staticCompositionLocalOf
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.MediaPreview
import com.anilist.fragment.MediaWithListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class IgnoreController(
    private val scope: ApplicationScope,
    private val ignoreDao: AnimeIgnoreDao,
    private val settings: AnimeSettings,
) {
    suspend fun isIgnored(mediaId: String) = ignoreDao.exists(mediaId)

    fun updates(): Flow<Int> = ignoreDao.entryCountFlow()

    fun toggle(
        mediaId: String,
        type: MediaType?,
        isAdult: Boolean?,
        bannerImage: String?,
        coverImage: String?,
        titleRomaji: String?,
        titleEnglish: String?,
        titleNative: String?,
    ) {
        if (!settings.mediaIgnoreEnabled.value) return
        scope.launch(CustomDispatchers.IO) {
            try {
                if (ignoreDao.exists(mediaId)) {
                    ignoreDao.delete(mediaId)
                } else {
                    ignoreDao.insertEntries(
                        AnimeMediaIgnoreEntry(
                            id = mediaId,
                            type = type,
                            isAdult = isAdult,
                            bannerImage = bannerImage,
                            coverImage = coverImage,
                            title = AnimeMediaIgnoreEntry.Title(
                                romaji = titleRomaji,
                                english = titleEnglish,
                                native = titleNative,
                            ),
                            viewedAt = Clock.System.now().toEpochMilliseconds(),
                        ),
                    )
                }
            } catch (ignored: Throwable) {
            }
        }
    }

    fun toggle(media: MediaPreview) = toggle(
        mediaId = media.id.toString(),
        type = media.type,
        isAdult = media.isAdult,
        bannerImage = media.bannerImage,
        coverImage = media.coverImage?.extraLarge,
        titleRomaji = media.title?.romaji,
        titleEnglish = media.title?.english,
        titleNative = media.title?.native,
    )

    fun toggle(media: MediaCompactWithTags) = toggle(
        mediaId = media.id.toString(),
        type = media.type,
        isAdult = media.isAdult,
        // TODO: Should this try to fill the missing banner image?
        bannerImage = null,
        coverImage = media.coverImage?.extraLarge,
        titleRomaji = media.title?.romaji,
        titleEnglish = media.title?.english,
        titleNative = media.title?.native,
    )

    fun toggle(media: MediaWithListStatus) = toggle(
        mediaId = media.id.toString(),
        type = media.type,
        isAdult = media.isAdult,
        // TODO: Should this try to fill the missing banner image?
        bannerImage = null,
        coverImage = media.coverImage?.extraLarge,
        titleRomaji = media.title?.romaji,
        titleEnglish = media.title?.english,
        titleNative = media.title?.native,
    )

    fun clear() {
        scope.launch(CustomDispatchers.IO) {
            ignoreDao.deleteAll()
        }
    }
}

val LocalIgnoreController = staticCompositionLocalOf<IgnoreController> { throw IllegalStateException() }
