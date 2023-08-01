package com.thekeeperofpie.artistalleydatabase.anime.media

import android.os.SystemClock
import com.anilist.UserMediaListQuery
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn

/**
 * User's MediaListCollection API is very broken, it makes sense to query a known working complete
 * value and just split it off into the various functionality in the app.
 *
 * This also allows for easier caching/offline syncing from a central location, although that's not
 * implemented.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserMediaListController(
    private val scopedApplication: ScopedApplication,
    private val aniListApi: AuthedAniListApi,
    private val ignoreList: AnimeMediaIgnoreList,
    private val statusController: MediaListStatusController,
    private val settings: AnimeSettings,
) {
    private val refreshAnime = MutableStateFlow(-1L)
    private val refreshManga = MutableStateFlow(-1L)

    var anime: Flow<LoadingResult<List<ListEntry>>>
        private set

    var manga: Flow<LoadingResult<List<ListEntry>>>
        private set

    init {
        anime = loadMedia(refreshAnime, MediaType.ANIME)
        manga = loadMedia(refreshManga, MediaType.MANGA)
    }

    private fun loadMedia(refresh: StateFlow<Long>, mediaType: MediaType) =
        combine(aniListApi.authedUser, refresh, ::Pair)
            .flatMapLatest { (viewer) ->
                if (viewer == null) return@flatMapLatest flowOf(LoadingResult.empty())
                aniListApi.userMediaList(viewer.id, mediaType)
                    .map {
                        it.transformResult {
                            it.lists?.filterNotNull()?.map(::ListEntry)
                                .orEmpty()
                        }
                    }
            }
            .runningFold(LoadingResult<List<ListEntry>>(loading = true, success = true)) { accumulator, value ->
                value.transformIf(value.loading && value.result == null) {
                    copy(result = accumulator.result)
                }
            }
            .flatMapLatest { entry ->
                combine(
                    statusController.allChanges(),
                    ignoreList.updates,
                    settings.showAdult,
                    settings.showIgnored,
                    settings.showLessImportantTags,
                    settings.showSpoilerTags,
                ) { statuses, ignoredIds, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                    applyStatus(
                        statuses = statuses,
                        showAdult = showAdult,
                        showIgnored = showIgnored,
                        ignoredIds = ignoredIds,
                        showLessImportantTags = showLessImportantTags,
                        showSpoilerTags = showSpoilerTags,
                        result = entry,
                    )
                }
            }
            .flowOn(CustomDispatchers.IO)
            .shareIn(scopedApplication.scope, SharingStarted.Lazily, replay = 1)

    private fun applyStatus(
        statuses: Map<String, MediaListStatusController.Update>,
        showAdult: Boolean,
        showIgnored: Boolean,
        ignoredIds: Set<Int>,
        showLessImportantTags: Boolean,
        showSpoilerTags: Boolean,
        result: LoadingResult<List<ListEntry>>,
    ) = result.transformResult {
        it.map {
            it.copy(entries = it.entries.mapNotNull {
                applyMediaFiltering(
                    statuses = statuses,
                    ignoredIds = ignoredIds,
                    showAdult = showAdult,
                    showIgnored = showIgnored,
                    showLessImportantTags = showLessImportantTags,
                    showSpoilerTags = showSpoilerTags,
                    entry = it,
                    transform = { it },
                    media = it.media,
                    copy = { mediaListStatus, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                        MediaEntry(
                            media = media,
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    }
                )
            })
        }
    }

    fun refresh(mediaType: MediaType) {
        if (mediaType == MediaType.ANIME) {
            refreshAnime.value = SystemClock.uptimeMillis()
        } else {
            refreshManga.value = SystemClock.uptimeMillis()
        }
    }

    data class ListEntry(
        val name: String,
        val status: MediaListStatus?,
        val entries: List<MediaEntry>,
    ) {
        constructor(list: UserMediaListQuery.Data.MediaListCollection.List) : this(
            name = list.name.orEmpty(),
            status = list.status,
            entries = list.entries?.filterNotNull()
                ?.map { MediaEntry(it.media) }
                .orEmpty()
        )
    }

    data class MediaEntry(
        val media: UserMediaListQuery.Data.MediaListCollection.List.Entry.Media,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = null,
        override val progressVolumes: Int? = null,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : MediaStatusAware
}