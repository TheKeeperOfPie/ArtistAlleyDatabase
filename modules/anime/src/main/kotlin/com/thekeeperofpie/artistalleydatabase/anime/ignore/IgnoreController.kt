package com.thekeeperofpie.artistalleydatabase.anime.ignore

import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.MediaPreview
import com.anilist.fragment.StaffDetailsStaffMediaPage
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.Instant

class IgnoreController(
    val scopedApplication: ScopedApplication,
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
        scopedApplication.scope.launch(CustomDispatchers.IO) {
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
                            viewedAt = Instant.now().toEpochMilli(),
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

    fun toggle(media: StaffDetailsStaffMediaPage.Edge.Node) = toggle(
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
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            ignoreDao.deleteAll()
        }
    }
}
