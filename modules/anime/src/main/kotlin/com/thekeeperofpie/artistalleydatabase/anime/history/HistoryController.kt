package com.thekeeperofpie.artistalleydatabase.anime.history

import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class HistoryController(
    private val scopedApplication: ScopedApplication,
    private val historyDao: AnimeHistoryDao,
    private val settings: AnimeSettings,
) {
    fun onVisitMediaDetails(
        mediaId: String,
        type: MediaType?,
        isAdult: Boolean?,
        bannerImage: String?,
        coverImage: String?,
        titleRomaji: String?,
        titleEnglish: String?,
        titleNative: String?,
    ) {
        if (!settings.mediaHistoryEnabled.value) return
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            try {
                historyDao.insertEntry(
                    AnimeMediaHistoryEntry(
                        id = mediaId,
                        type = type,
                        isAdult = isAdult,
                        bannerImage = bannerImage,
                        coverImage = coverImage,
                        title = AnimeMediaHistoryEntry.Title(
                            romaji = titleRomaji,
                            english = titleEnglish,
                            native = titleNative,
                        ),
                        viewedAt = Instant.now().toEpochMilli(),
                    ),
                    maxEntries = settings.mediaHistoryMaxEntries.value,
                )
            } catch (ignored: Throwable) {
            }
        }
    }

    fun clear() {
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            try {
                historyDao.deleteAll()
            } catch (ignored: Throwable) {
            }
        }
    }
}
