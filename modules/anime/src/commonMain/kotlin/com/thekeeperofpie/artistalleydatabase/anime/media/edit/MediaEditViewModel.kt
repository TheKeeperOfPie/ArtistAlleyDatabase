package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_error_deleting
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_error_invalid_priority
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_error_invalid_progress
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_error_invalid_repeat
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_error_invalid_score
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_error_invalid_status
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_media_edit_error_saving
import com.anilist.data.fragment.MediaDetailsListEntry
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaQuickEditData
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.utils.SimpleResult
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Inject
class MediaEditViewModel(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val settings: AnimeSettings,
) : ViewModel() {

    val state = MediaEditState()

    private val mediaEntryRequest = MutableStateFlow<MediaRequest?>(null)
    private val rawScore = MutableSharedFlow<Double?>(1, 1)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            aniListApi.authedUser
                .mapNotNull { it?.scoreFormat }
                .collectLatest {
                    withContext(Dispatchers.Main) {
                        state.scoreFormat = it
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            mediaEntryRequest
                .filterNotNull()
                .mapLatest {
                    Result.success(it to aniListApi.mediaListEntry(it.mediaId).media)
                }
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        if (it.isSuccess) {
                            val (request, media) = it.getOrThrow()
                            initialize(
                                mediaId = request.mediaId,
                                coverImage = request.coverImage,
                                title = request.title,
                                mediaListEntry = media.mediaListEntry,
                                mediaType = media.type,
                                status = media.mediaListEntry?.status,
                                maxProgress = MediaUtils.maxProgress(media),
                                maxProgressVolumes = media.volumes,
                            )
                        } else {
                            state.error = Res.string.anime_media_edit_error_loading to
                                    it.exceptionOrNull()
                            mediaEntryRequest.emit(null)
                        }
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { state.initialParams }
                .flatMapLatest {
                    if (it?.mediaId != null) {
                        statusController.allChanges(it.mediaId)
                    } else {
                        emptyFlow()
                    }
                }
                .filterNotNull()
                .collectLatest { state.status = it.entry?.status }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            combine(snapshotFlow { state.scoreFormat }, rawScore, ::Pair)
                .collectLatest { (format, score) ->
                    // TODO: Move locale to view layer
                    state.score = score?.let {
                        MediaUtils.scoreFormatToText(it, format)
                    }.orEmpty()
                }
        }
    }

    fun initialize(media: MediaNavigationData) {
        val mediaId = media.id.toString()
        val initialParams = state.initialParams
        if (initialParams?.mediaId != mediaId) {
            val title = media.title?.primaryTitle(settings.languageOptionMedia.value)
            mediaEntryRequest.tryEmit(
                MediaRequest(
                    mediaId = mediaId,
                    coverImage = media.coverImage?.extraLarge,
                    type = media.type,
                    title = title,
                )
            )
            initialize(
                mediaId = mediaId,
                coverImage = media.coverImage?.extraLarge,
                title = title,
                mediaListEntry = null,
                mediaType = null,
                status = null,
                maxProgress = null,
                maxProgressVolumes = null,
                loading = true
            )
        } else {
            state.hasConfirmedClose = false
        }
        state.showing = true
    }

    fun initialize(mediaQuickEditData: MediaQuickEditData) = initialize(
        mediaId = mediaQuickEditData.mediaId,
        coverImage = mediaQuickEditData.coverImageUrl,
        type = when (mediaQuickEditData.mediaType) {
            com.thekeeperofpie.artistalleydatabase.anime.data.MediaType.ANIME -> MediaType.ANIME
            com.thekeeperofpie.artistalleydatabase.anime.data.MediaType.MANGA -> MediaType.MANGA
            com.thekeeperofpie.artistalleydatabase.anime.data.MediaType.UNKNOWN -> MediaType.UNKNOWN__
            null -> null
        },
        titleRomaji = mediaQuickEditData.titleRomaji,
        titleEnglish = mediaQuickEditData.titleEnglish,
        titleNative = mediaQuickEditData.titleNative,
    )

    fun initialize(
        mediaId: String,
        coverImage: String?,
        type: MediaType?,
        titleRomaji: String?,
        titleEnglish: String?,
        titleNative: String?,
    ) {
        val initialParams = state.initialParams
        if (initialParams?.mediaId != mediaId) {
            val title = MediaUtils.userPreferredTitle(
                titleRomaji = titleRomaji,
                titleEnglish = titleEnglish,
                titleNative = titleNative,
                titleLanguage = aniListApi.authedUser.value?.titleLanguage,
                languageOption = settings.languageOptionMedia.value,
            )
            mediaEntryRequest.tryEmit(MediaRequest(mediaId, coverImage, title, type))
            initialize(
                mediaId = mediaId,
                coverImage = coverImage,
                title = title,
                mediaListEntry = null,
                mediaType = null,
                status = null,
                maxProgress = null,
                maxProgressVolumes = null,
                loading = true
            )
        } else {
            state.hasConfirmedClose = false
        }
        state.showing = true
    }

    fun initialize(
        mediaId: String,
        coverImage: String?,
        // TODO: Pass all translations so that UI can react to language changes down the line
        title: String?,
        mediaListEntry: MediaDetailsListEntry?,
        mediaType: MediaType?,
        status: MediaListStatus?,
        maxProgress: Int?,
        maxProgressVolumes: Int?,
        loading: Boolean = false,
    ) {
        state.initialParams = MediaEditState.InitialParams(
            mediaId = mediaId,
            coverImage = coverImage,
            title = title,
            mediaListEntry = mediaListEntry,
            mediaType = mediaType,
            maxProgress = maxProgress,
            maxProgressVolumes = maxProgressVolumes,
            loading = loading,
        )
        state.status = status
        state.progress = mediaListEntry?.progress.takeUnless { it == 0 }?.toString().orEmpty()
        state.progressVolumes =
            mediaListEntry?.progressVolumes.takeUnless { it == 0 }?.toString().orEmpty()
        state.repeat = mediaListEntry?.repeat.takeUnless { it == 0 }?.toString().orEmpty()
        state.startDate = MediaUtils.parseLocalDate(mediaListEntry?.startedAt)
        state.endDate = MediaUtils.parseLocalDate(mediaListEntry?.completedAt)
        state.priority = mediaListEntry?.priority.takeUnless { it == 0 }?.toString().orEmpty()
        state.private = mediaListEntry?.private ?: false
        state.hiddenFromStatusLists = mediaListEntry?.hiddenFromStatusLists ?: false
        state.updatedAt = mediaListEntry?.updatedAt?.toLong()
        state.createdAt = mediaListEntry?.createdAt?.toLong()
        state.notes = mediaListEntry?.notes.orEmpty()

        state.hasConfirmedClose = false

        rawScore.tryEmit(mediaListEntry?.score)
    }

    fun incrementProgress(entry: UserMediaListController.MediaEntry) {
        val newProgress = (entry.mediaFilterable.progress ?: 0) + 1
        if (newProgress > (MediaUtils.maxProgress(entry.media) ?: 0)) return
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val mediaId = entry.media.id.toString()
                val result = aniListApi.saveMediaListEntryProgressOnly(
                    entry.media.mediaListEntry?.id?.toString(),
                    mediaId,
                    newProgress,
                )
                statusController.onUpdate(mediaId, result)
            } catch (ignored: Throwable) {
                // TODO: Handle loading state and errors
            }
        }
    }

    fun onEvent(event: AnimeMediaEditBottomSheet.Event) = when (event) {
        is AnimeMediaEditBottomSheet.Event.DateChange ->
            onDateChange(event.start, event.selectedMillisUtc)
        AnimeMediaEditBottomSheet.Event.Delete -> onClickDelete()
        AnimeMediaEditBottomSheet.Event.Save -> onClickSave()
        is AnimeMediaEditBottomSheet.Event.StatusChange -> onStatusChange(event.status)
        is AnimeMediaEditBottomSheet.Event.Open -> {
            initialize(
                mediaId = event.mediaId,
                coverImage = event.coverImage,
                title = event.title,
                mediaListEntry = event.mediaListEntry,
                mediaType = event.mediaType,
                status = event.status,
                maxProgress = event.maxProgress,
                maxProgressVolumes = event.maxProgressVolumes,
                loading = event.loading,
            )
            state.showing = true
        }
    }

    private fun onDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
        }

        if (start) {
            state.startDate = selectedDate
        } else {
            state.endDate = selectedDate
        }
    }

    private fun onStatusChange(status: MediaListStatus?) {
        when (status) {
            MediaListStatus.CURRENT,
            MediaListStatus.PLANNING,
            MediaListStatus.PAUSED,
            MediaListStatus.REPEATING,
            MediaListStatus.UNKNOWN__, null,
                -> Unit
            MediaListStatus.COMPLETED -> {
                val initialParams = state.initialParams
                initialParams?.maxProgress
                    ?.let { state.progress = it.toString() }
                initialParams?.maxProgressVolumes
                    ?.let { state.progressVolumes = it.toString() }
                state.endDate =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            MediaListStatus.DROPPED -> {
                state.endDate =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
        }
    }

    private fun onClickDelete() {
        if (state.saving || state.deleting) return
        state.deleting = true
        val initialParams = state.initialParams
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                aniListApi.deleteMediaListEntry(initialParams?.id!!)

                val mediaId = initialParams.mediaId!!
                statusController.onUpdate(mediaId, null)
                withContext(CustomDispatchers.Main) {
                    state.deleting = false
                    state.showConfirmClose = false
                    initialize(
                        mediaId = mediaId,
                        coverImage = null,
                        title = null,
                        mediaListEntry = null,
                        mediaType = null,
                        status = null,
                        maxProgress = null,
                        maxProgressVolumes = null,
                    )
                    state.showing = false
                }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    state.deleting = false
                    state.error = Res.string.anime_media_edit_error_deleting to e
                }
            }
        }
    }

    private fun onClickSave() {
        if (state.status == null) {
            state.saving = false
            state.error = Res.string.anime_media_edit_error_invalid_status to null
            return
        }

        if (state.saving || state.deleting) return
        state.saving = true

        // Read values on main thread before entering coroutine
        val scoreRaw = state.scoreRaw()
        val progress = state.progress
        val progressVolumes = state.progressVolumes
        val repeat = state.repeat
        val priority = state.priority
        val status = state.status
        val private = state.private
        val hiddenFromStatusLists = state.hiddenFromStatusLists
        val startDate = state.startDate
        val endDate = state.endDate
        val notes = state.notes

        val initialParams = state.initialParams ?: return
        viewModelScope.launch(CustomDispatchers.IO) {
            fun validateFieldAsInt(field: String): SimpleResult<Int> {
                if (field.isBlank()) return SimpleResult.Success(0)
                return SimpleResult.successIfNotNull(field.toIntOrNull())
            }

            if (scoreRaw is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    state.saving = false
                    state.error = Res.string.anime_media_edit_error_invalid_score to null
                }
                return@launch
            }

            val progressAsInt = validateFieldAsInt(progress)
            if (progressAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    state.saving = false
                    state.error = Res.string.anime_media_edit_error_invalid_progress to null
                }
                return@launch
            }

            val progressVolumesAsInt = if (initialParams.mediaType != MediaType.ANIME) {
                validateFieldAsInt(progressVolumes).also {
                    if (it is SimpleResult.Failure) {
                        withContext(CustomDispatchers.Main) {
                            state.saving = false
                            state.error =
                                Res.string.anime_media_edit_error_invalid_progress to null
                        }
                        return@launch
                    }
                }
            } else null

            val repeatAsInt = validateFieldAsInt(repeat)
            if (repeatAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    state.saving = false
                    state.error = Res.string.anime_media_edit_error_invalid_repeat to null
                }
                return@launch
            }

            val priorityAsInt = validateFieldAsInt(priority)
            if (priorityAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    state.saving = false
                    state.error = Res.string.anime_media_edit_error_invalid_priority to null
                }
                return@launch
            }

            try {
                val mediaId = initialParams.mediaId!!
                val result = aniListApi.saveMediaListEntry(
                    id = initialParams.id,
                    mediaId = mediaId,
                    status = status,
                    scoreRaw = scoreRaw.getOrThrow(),
                    progress = progressAsInt.getOrThrow(),
                    progressVolumes = progressVolumesAsInt?.getOrThrow(),
                    repeat = repeatAsInt.getOrThrow(),
                    priority = priorityAsInt.getOrThrow(),
                    private = private,
                    notes = notes,
                    startedAt = startDate,
                    completedAt = endDate,
                    hiddenFromStatusLists = hiddenFromStatusLists,
                )

                statusController.onUpdate(mediaId, result)

                withContext(CustomDispatchers.Main) {
                    state.saving = false
                    state.showConfirmClose = false
                    initialize(
                        mediaId = mediaId,
                        coverImage = initialParams.coverImage,
                        title = initialParams.title,
                        mediaListEntry = result,
                        mediaType = initialParams.mediaType,
                        status = result.status,
                        maxProgress = initialParams.maxProgress,
                        maxProgressVolumes = initialParams.maxProgressVolumes,
                    )
                    state.showing = false
                }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    state.saving = false
                    state.error = Res.string.anime_media_edit_error_saving to e
                }
            }
        }
    }

    data class MediaRequest(
        val mediaId: String,
        val coverImage: String?,
        val title: String?,
        val type: MediaType?,
    )
}
