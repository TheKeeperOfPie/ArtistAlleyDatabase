package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
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
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.fragment.MediaNavigationData
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaQuickEditData
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.utils.SimpleResult
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
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

    val initialParams = MutableStateFlow<MediaEditData.InitialParams?>(null)
    private val mediaEntryRequest = MutableStateFlow<MediaRequest?>(null)

    val editData = MediaEditData()

    private val rawScore = MutableSharedFlow<Double?>(1, 1)
    val scoreFormat = MutableStateFlow(ScoreFormat.POINT_100)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            aniListApi.authedUser
                .mapNotNull { it?.scoreFormat }
                .collect(scoreFormat::emit)
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
                            editData.error = Res.string.anime_media_edit_error_loading to
                                    it.exceptionOrNull()
                            mediaEntryRequest.emit(null)
                        }
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            initialParams.flatMapLatest {
                if (it?.mediaId != null) {
                    statusController.allChanges(it.mediaId)
                } else {
                    emptyFlow()
                }
            }
                .filterNotNull()
                .collectLatest { editData.status = it.entry?.status }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            combine(scoreFormat, rawScore, ::Pair)
                .collectLatest { (format, score) ->
                    // TODO: Move locale to view layer
                    editData.score = score?.let {
                        MediaUtils.scoreFormatToText(it, format)
                    }.orEmpty()
                }
        }
    }

    fun initialize(media: MediaNavigationData) {
        val mediaId = media.id.toString()
        val initialParams = initialParams.value
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
        }
        editData.showing = true
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
        val initialParams = initialParams.value
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
        }
        editData.showing = true
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
        initialParams.value = MediaEditData.InitialParams(
            mediaId = mediaId,
            coverImage = coverImage,
            title = title,
            mediaListEntry = mediaListEntry,
            mediaType = mediaType,
            maxProgress = maxProgress,
            maxProgressVolumes = maxProgressVolumes,
            loading = loading,
        )
        editData.status = status
        editData.progress = mediaListEntry?.progress.takeUnless { it == 0 }?.toString().orEmpty()
        editData.progressVolumes =
            mediaListEntry?.progressVolumes.takeUnless { it == 0 }?.toString().orEmpty()
        editData.repeat = mediaListEntry?.repeat.takeUnless { it == 0 }?.toString().orEmpty()
        editData.startDate = MediaUtils.parseLocalDate(mediaListEntry?.startedAt)
        editData.endDate = MediaUtils.parseLocalDate(mediaListEntry?.completedAt)
        editData.priority = mediaListEntry?.priority.takeUnless { it == 0 }?.toString().orEmpty()
        editData.private = mediaListEntry?.private ?: false
        editData.hiddenFromStatusLists = mediaListEntry?.hiddenFromStatusLists ?: false
        editData.updatedAt = mediaListEntry?.updatedAt?.toLong()
        editData.createdAt = mediaListEntry?.createdAt?.toLong()
        editData.notes = mediaListEntry?.notes.orEmpty()
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

    fun hide() {
        editData.showing = false
        editData.error = null
    }

    fun onEditSheetValueChange(sheetValue: SheetValue): Boolean {
        // TODO: Due to a bug with recent Compose versions, confirmValueChange can be called with
        //  PartiallyExpanded even if the sheet is fully expanded, which means it's not possible to
        //  block PartiallyExpanded to show an exit dialog. Instead, this is handled in the UI
        //  layer by observing the sheet value change. For now, no-op this method, with another
        //  one (attemptDismiss) created to handle real dismisses.
        return true
//        if (!editData.showing) return true
//        if (sheetValue == SheetValue.Expanded) return true
//        if (editData.isEqualTo(initialParams.value, scoreFormat.value)) return true
//        editData.showConfirmClose = true
//        return false
    }

    fun attemptDismiss(): Boolean {
        if (!editData.showing) return true
        if (editData.isEqualTo(initialParams.value, scoreFormat.value)) return true
        editData.showConfirmClose = true
        return false
    }

    fun onDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
        }

        if (start) {
            editData.startDate = selectedDate
        } else {
            editData.endDate = selectedDate
        }
    }

    fun onStatusChange(status: MediaListStatus?) {
        editData.status = status
        when (status) {
            MediaListStatus.CURRENT,
            MediaListStatus.PLANNING,
            MediaListStatus.PAUSED,
            MediaListStatus.REPEATING,
            MediaListStatus.UNKNOWN__, null,
                -> Unit
            MediaListStatus.COMPLETED -> {
                initialParams.value?.maxProgress
                    ?.let { editData.progress = it.toString() }
                initialParams.value?.maxProgressVolumes
                    ?.let { editData.progressVolumes = it.toString() }
                editData.endDate =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            MediaListStatus.DROPPED -> {
                editData.endDate =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
        }
    }

    fun onClickDelete() {
        if (editData.saving || editData.deleting) return
        editData.deleting = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val initialParams = initialParams.value
                aniListApi.deleteMediaListEntry(initialParams?.id!!)

                val mediaId = initialParams.mediaId!!
                statusController.onUpdate(mediaId, null)
                withContext(CustomDispatchers.Main) {
                    editData.deleting = false
                    editData.showing = false
                    editData.showConfirmClose = false
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
                }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    editData.deleting = false
                    editData.error = Res.string.anime_media_edit_error_deleting to e
                }
            }
        }
    }

    fun onClickSave() {
        if (editData.status == null) {
            editData.saving = false
            editData.error = Res.string.anime_media_edit_error_invalid_status to null
            return
        }

        if (editData.saving || editData.deleting) return
        editData.saving = true

        // Read values on main thread before entering coroutine
        val scoreRaw = editData.scoreRaw(scoreFormat.value)
        val progress = editData.progress
        val progressVolumes = editData.progressVolumes
        val repeat = editData.repeat
        val priority = editData.priority
        val status = editData.status
        val private = editData.private
        val hiddenFromStatusLists = editData.hiddenFromStatusLists
        val startDate = editData.startDate
        val endDate = editData.endDate
        val notes = editData.notes

        viewModelScope.launch(CustomDispatchers.IO) {
            val initialParams = initialParams.value!!
            fun validateFieldAsInt(field: String): SimpleResult<Int> {
                if (field.isBlank()) return SimpleResult.Success(0)
                return SimpleResult.successIfNotNull(field.toIntOrNull())
            }

            if (scoreRaw is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = Res.string.anime_media_edit_error_invalid_score to null
                }
                return@launch
            }

            val progressAsInt = validateFieldAsInt(progress)
            if (progressAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = Res.string.anime_media_edit_error_invalid_progress to null
                }
                return@launch
            }

            val progressVolumesAsInt = if (initialParams.mediaType != MediaType.ANIME) {
                validateFieldAsInt(progressVolumes).also {
                    if (it is SimpleResult.Failure) {
                        withContext(CustomDispatchers.Main) {
                            editData.saving = false
                            editData.error =
                                Res.string.anime_media_edit_error_invalid_progress to null
                        }
                        return@launch
                    }
                }
            } else null

            val repeatAsInt = validateFieldAsInt(repeat)
            if (repeatAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = Res.string.anime_media_edit_error_invalid_repeat to null
                }
                return@launch
            }

            val priorityAsInt = validateFieldAsInt(priority)
            if (priorityAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = Res.string.anime_media_edit_error_invalid_priority to null
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
                    editData.saving = false
                    editData.showing = false
                    editData.showConfirmClose = false
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
                }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = Res.string.anime_media_edit_error_saving to e
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
