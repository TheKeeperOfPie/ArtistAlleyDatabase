package com.thekeeperofpie.artistalleydatabase.anime.history

import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

@SingleIn(AppScope::class)
@Inject
class HistoryController(
    private val scope: ApplicationScope,
    private val historyDao: AnimeHistoryDao,
    private val settings: HistorySettings,
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
        scope.launch(CustomDispatchers.Companion.IO) {
            try {
                historyDao.insertEntry(
                    AnimeMediaHistoryEntry(
                        id = mediaId,
                        type = type,
                        isAdult = isAdult,
                        bannerImage = bannerImage,
                        coverImage = coverImage,
                        title = AnimeMediaHistoryEntryTitle(
                            romaji = titleRomaji,
                            english = titleEnglish,
                            native = titleNative,
                        ),
                        viewedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                    maxEntries = settings.mediaHistoryMaxEntries.value,
                )
            } catch (ignored: Throwable) {
            }
        }
    }

    fun clear() {
        scope.launch(CustomDispatchers.Companion.IO) {
            try {
                historyDao.deleteAll()
            } catch (ignored: Throwable) {
            }
        }
    }
}
