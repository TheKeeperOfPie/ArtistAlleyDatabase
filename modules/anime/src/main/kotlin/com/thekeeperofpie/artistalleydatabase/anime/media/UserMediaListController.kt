package com.thekeeperofpie.artistalleydatabase.anime.media

import android.os.SystemClock
import com.anilist.UserMediaListQuery
import com.anilist.ViewerMediaListQuery
import com.anilist.fragment.UserMediaListMedia
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
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
    private val ignoreController: IgnoreController,
    private val statusController: MediaListStatusController,
    private val settings: AnimeSettings,
) {
    private val refreshAnime = MutableStateFlow(-1L)
    private val refreshManga = MutableStateFlow(-1L)

    private val includeDescription = MutableStateFlow(false)

    private var anime: Flow<LoadingResult<List<ListEntry>>>
    private var manga: Flow<LoadingResult<List<ListEntry>>>

    init {
        anime = loadMedia(refreshAnime, MediaType.ANIME)
        manga = loadMedia(refreshManga, MediaType.MANGA)
    }

    private fun loadMedia(refresh: StateFlow<Long>, mediaType: MediaType) =
        combine(aniListApi.authedUser, includeDescription, refresh, ::Triple)
            .flatMapLatest { (viewer, includeDescription) ->
                if (viewer == null) return@flatMapLatest flowOf(LoadingResult.empty())
                aniListApi.viewerMediaList(
                    userId = viewer.id,
                    type = mediaType,
                    includeDescription = includeDescription
                ).map {
                    it.transformResult {
                        it.lists?.filterNotNull()?.map(::ListEntry)
                            .orEmpty()
                    }
                }
            }
            .runningFold(
                LoadingResult<List<ListEntry>>(
                    loading = true,
                    success = true
                )
            ) { accumulator, value ->
                value.transformIf(value.loading && value.result == null) {
                    copy(result = accumulator.result)
                }
            }
            .flatMapLatest { entry ->
                combine(
                    statusController.allChanges(),
                    ignoreController.updates(),
                    settings.showAdult,
                    settings.showIgnored,
                    settings.showLessImportantTags,
                    settings.showSpoilerTags,
                ) { statuses, _, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                    applyStatus(
                        statuses = statuses,
                        showAdult = showAdult,
                        showIgnored = showIgnored,
                        showLessImportantTags = showLessImportantTags,
                        showSpoilerTags = showSpoilerTags,
                        result = entry,
                    )
                }
            }
            .flowOn(CustomDispatchers.IO)
            .shareIn(scopedApplication.scope, SharingStarted.Lazily, replay = 1)

    private suspend fun applyStatus(
        statuses: Map<String, MediaListStatusController.Update>,
        showAdult: Boolean,
        showIgnored: Boolean,
        showLessImportantTags: Boolean,
        showSpoilerTags: Boolean,
        result: LoadingResult<List<ListEntry>>,
    ) = result.transformResult {
        it.map {
            it.copy(entries = it.entries.mapNotNull {
                applyMediaFiltering(
                    statuses = statuses,
                    ignoreController = ignoreController,
                    showAdult = showAdult,
                    showIgnored = showIgnored,
                    showLessImportantTags = showLessImportantTags,
                    showSpoilerTags = showSpoilerTags,
                    entry = it,
                    transform = { it },
                    media = it.media,
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        MediaEntry(
                            media = media,
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            scoreRaw = scoreRaw,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    }
                )
            })
        }
    }

    fun anime(includeDescription: Boolean): Flow<LoadingResult<List<ListEntry>>> {
        if (includeDescription) {
            this.includeDescription.value = true
        }
        return anime
    }

    fun manga(includeDescription: Boolean): Flow<LoadingResult<List<ListEntry>>> {
        if (includeDescription) {
            this.includeDescription.value = true
        }
        return manga
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

        // TODO: This can be moved up a level
        val scoreFormat: ScoreFormat?,
        val entries: List<MediaEntry>,
    ) {
        constructor(list: ViewerMediaListQuery.Data.MediaListCollection.List) : this(
            name = list.name.orEmpty(),
            status = list.status,
            scoreFormat = null,
            entries = list.entries?.filterNotNull()
                ?.map { MediaEntry(media = it.media, authorData = null) }
                .orEmpty()
        )

        constructor(
            scoreFormat: ScoreFormat?,
            list: UserMediaListQuery.Data.MediaListCollection.List,
        ) : this(
            name = list.name.orEmpty(),
            status = list.status,
            scoreFormat = scoreFormat,
            entries = list.entries?.filterNotNull()
                ?.map { MediaEntry(list.status, it) }
                .orEmpty()
        )
    }

    data class MediaEntry(
        val media: UserMediaListMedia,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = media.mediaListEntry?.progress,
        override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
        override val scoreRaw: Double? = media.mediaListEntry?.score,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
        val authorData: AuthorData? = null,
    ) : MediaStatusAware {
        constructor(
            status: MediaListStatus?,
            entry: UserMediaListQuery.Data.MediaListCollection.List.Entry,
        ) : this(
            media = entry.media,
            authorData = AuthorData(
                status = status,
                rawScore = entry.score,
                progress = entry.progress,
            ),
        )

        data class AuthorData(
            val status: MediaListStatus?,
            val rawScore: Double?,
            val progress: Int?,
        )
    }
}
