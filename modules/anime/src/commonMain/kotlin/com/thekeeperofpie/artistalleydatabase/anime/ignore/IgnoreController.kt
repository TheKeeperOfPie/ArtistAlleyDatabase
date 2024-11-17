package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.paging.PagingSource
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeRootNavDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class IgnoreController(
    private val scope: ApplicationScope,
    private val ignoreDao: AnimeIgnoreDao,
    private val settings: AnimeSettings,
) {
    suspend fun isIgnored(mediaId: String) = ignoreDao.exists(mediaId)

    fun updates(): Flow<Int> = ignoreDao.entryCountFlow()

    fun toggle(data: Data) = toggle(
        mediaId = data.mediaId,
        type = data.type,
        isAdult = data.isAdult,
        bannerImage = data.bannerImageUrl,
        coverImage = data.coverImageUrl,
        titleRomaji = data.titleRomaji,
        titleEnglish = data.titleEnglish,
        titleNative = data.titleNative,
    )

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

    interface Data {
        val mediaId: String
        val type: MediaType?
        val isAdult: Boolean?
        val bannerImageUrl: String?
        val coverImageUrl: String?
        val titleRomaji: String?
        val titleEnglish: String?
        val titleNative: String?
    }
}

val LocalIgnoreController = compositionLocalWithComputedDefaultOf<IgnoreController> {
    if (LocalInspectionMode.currentValue) {
        IgnoreController(MainScope(), object : AnimeIgnoreDao {
            override fun getEntries(type: MediaType): PagingSource<Int, AnimeMediaIgnoreEntry> {
                TODO("Not yet implemented")
            }

            override suspend fun getEntries(
                limit: Int,
                offset: Int,
                type: MediaType,
            ): List<AnimeMediaIgnoreEntry> {
                TODO("Not yet implemented")
            }

            override suspend fun insertEntries(vararg entries: AnimeMediaIgnoreEntry) {
                TODO("Not yet implemented")
            }

            override suspend fun getEntryCount(): Int {
                TODO("Not yet implemented")
            }

            override fun entryCountFlow(): Flow<Int> {
                TODO("Not yet implemented")
            }

            override suspend fun getEntryAtIndex(
                index: Int,
                type: MediaType,
            ): AnimeMediaIgnoreEntry? {
                TODO("Not yet implemented")
            }

            override suspend fun exists(id: String): Boolean {
                TODO("Not yet implemented")
            }

            override suspend fun delete(id: String) {
                TODO("Not yet implemented")
            }

            override suspend fun deleteAll() {
                TODO("Not yet implemented")
            }

        }, object : AnimeSettings {
            override val savedAnimeFilters: MutableStateFlow<Map<String, FilterData>>
                get() = TODO("Not yet implemented")
            override val showAdult: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val collapseAnimeFiltersOnClose: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val showLessImportantTags: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val showSpoilerTags: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val preferredMediaType: MutableStateFlow<MediaType>
                get() = TODO("Not yet implemented")
            override val mediaViewOption: MutableStateFlow<MediaViewOption>
                get() = TODO("Not yet implemented")
            override val rootNavDestination: MutableStateFlow<AnimeRootNavDestination>
                get() = TODO("Not yet implemented")
            override val mediaHistoryEnabled: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val mediaHistoryMaxEntries: MutableStateFlow<Int>
                get() = TODO("Not yet implemented")
            override val mediaIgnoreEnabled: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val mediaIgnoreHide: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val showIgnored: StateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val languageOptionMedia: MutableStateFlow<AniListLanguageOption>
                get() = TODO("Not yet implemented")
            override val languageOptionCharacters: MutableStateFlow<AniListLanguageOption>
                get() = TODO("Not yet implemented")
            override val languageOptionStaff: MutableStateFlow<AniListLanguageOption>
                get() = TODO("Not yet implemented")
            override val languageOptionVoiceActor: MutableStateFlow<VoiceActorLanguageOption>
                get() = TODO("Not yet implemented")
            override val showFallbackVoiceActor: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")
            override val currentMediaListSizeAnime: MutableStateFlow<Int>
                get() = TODO("Not yet implemented")
            override val currentMediaListSizeManga: MutableStateFlow<Int>
                get() = TODO("Not yet implemented")
            override val lastCrash: MutableStateFlow<String>
                get() = TODO("Not yet implemented")
            override val lastCrashShown: MutableStateFlow<Boolean>
                get() = TODO("Not yet implemented")

        })
    } else {
        throw IllegalStateException()
    }
}
