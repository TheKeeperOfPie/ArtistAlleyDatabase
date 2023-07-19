package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.fragment.MediaNavigationData
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.android_utils.SimpleResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@HiltViewModel
class MediaEditViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
) : ViewModel() {

    val initialParams = MutableStateFlow<MediaEditData.InitialParams?>(null)
    private val mediaEntryRequest = MutableStateFlow<MediaNavigationData?>(null)

    val editData = MediaEditData()

    private val rawScore = MutableSharedFlow<Double?>(1, 1)
    val scoreFormat = MutableStateFlow(ScoreFormat.POINT_100)

    val dismissRequests = MutableSharedFlow<Long>()

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            aniListApi.authedUser
                .mapNotNull { it?.mediaListOptions?.scoreFormat }
                .collect(scoreFormat::emit)
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            mediaEntryRequest
                .filterNotNull()
                .mapLatest {
                    Result.success(it to aniListApi.mediaListEntry(it.id.toString()).media)
                }
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        if (it.isSuccess) {
                            val (mediaNavData, media) = it.getOrThrow()
                            initialize(
                                mediaId = mediaNavData.id.toString(),
                                media = mediaNavData,
                                mediaListEntry = media.mediaListEntry,
                                mediaType = media.type,
                                status = media.mediaListEntry?.status,
                                maxProgress = MediaUtils.maxProgress(media),
                                maxProgressVolumes = media.volumes,
                            )
                        } else {
                            editData.error = R.string.anime_media_edit_error_loading to
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
                    editData.score = score?.let { MediaUtils.scoreFormatToText(it, format) } ?: ""
                }
        }
    }

    fun initialize(media: MediaNavigationData) {
        val mediaId = media.id.toString()
        val initialParams = initialParams.value
        if (initialParams?.mediaId != mediaId) {
            mediaEntryRequest.tryEmit(media)
            initialize(
                mediaId = mediaId,
                media = media,
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
        media: MediaNavigationData?,
        mediaListEntry: MediaDetailsListEntry?,
        mediaType: MediaType?,
        status: MediaListStatus?,
        maxProgress: Int?,
        maxProgressVolumes: Int?,
        loading: Boolean = false,
    ) {
        initialParams.value = MediaEditData.InitialParams(
            mediaId = mediaId,
            media = media,
            mediaListEntry = mediaListEntry,
            mediaType = mediaType,
            maxProgress = maxProgress ?: 1,
            maxProgressVolumes = maxProgressVolumes ?: 1,
            loading = loading,
        )
        editData.status = status
        editData.progress = mediaListEntry?.progress.takeUnless { it == 0 }?.toString().orEmpty()
        editData.progressVolumes = mediaListEntry?.progressVolumes.takeUnless { it == 0 }?.toString().orEmpty()
        editData.repeat = mediaListEntry?.repeat.takeUnless { it == 0 }?.toString().orEmpty()
        editData.startDate = MediaUtils.parseLocalDate(mediaListEntry?.startedAt)
        editData.endDate = MediaUtils.parseLocalDate(mediaListEntry?.completedAt)
        editData.priority = mediaListEntry?.priority.takeUnless { it == 0 }?.toString().orEmpty()
        editData.private = mediaListEntry?.private ?: false
        editData.updatedAt = mediaListEntry?.updatedAt?.toLong()
        editData.createdAt = mediaListEntry?.createdAt?.toLong()
        rawScore.tryEmit(mediaListEntry?.score)
    }

    fun hide() {
        editData.showing = false
        editData.error = null
    }

    fun onEditSheetValueChange(sheetValue: SheetValue): Boolean {
        if (sheetValue != SheetValue.Hidden) return true
        if (editData.isEqualTo(initialParams.value, scoreFormat.value)) return true
        editData.showConfirmClose = true
        return false
    }

    fun onDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
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
                editData.endDate = LocalDate.now()
            }
            MediaListStatus.DROPPED -> {
                editData.endDate = LocalDate.now()
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
                    dismissRequests.emit(System.currentTimeMillis())
                    initialize(
                        mediaId = mediaId,
                        media = null,
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
                    editData.error = R.string.anime_media_edit_error_deleting to e
                }
            }
        }
    }

    fun onClickSave() {
        if (editData.status == null) {
            editData.saving = false
            editData.error = R.string.anime_media_edit_error_invalid_status to null
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
        val startDate = editData.startDate
        val endDate = editData.endDate

        viewModelScope.launch(CustomDispatchers.IO) {
            val initialParams = initialParams.value!!
            fun validateFieldAsInt(field: String): SimpleResult<Int> {
                if (field.isBlank()) return SimpleResult.Success(0)
                return SimpleResult.successIfNotNull(field.toIntOrNull())
            }

            if (scoreRaw is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = R.string.anime_media_edit_error_invalid_score to null
                }
                return@launch
            }

            val progressAsInt = validateFieldAsInt(progress)
            if (progressAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = R.string.anime_media_edit_error_invalid_progress to null
                }
                return@launch
            }

            val progressVolumesAsInt = if (initialParams.mediaType != MediaType.ANIME) {
                validateFieldAsInt(progressVolumes).also {
                    if (it is SimpleResult.Failure) {
                        withContext(CustomDispatchers.Main) {
                            editData.saving = false
                            editData.error =
                                R.string.anime_media_edit_error_invalid_progress to null
                        }
                        return@launch
                    }
                }
            } else null

            val repeatAsInt = validateFieldAsInt(repeat)
            if (repeatAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = R.string.anime_media_edit_error_invalid_repeat to null
                }
                return@launch
            }

            val priorityAsInt = validateFieldAsInt(priority)
            if (priorityAsInt is SimpleResult.Failure) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.error = R.string.anime_media_edit_error_invalid_priority to null
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
                    startedAt = startDate,
                    completedAt = endDate,
                    hiddenFromStatusLists = null,
                )

                statusController.onUpdate(mediaId, result)

                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.showing = false
                    editData.showConfirmClose = false
                    dismissRequests.emit(System.currentTimeMillis())
                    initialize(
                        mediaId = mediaId,
                        media = initialParams.media,
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
                    editData.error = R.string.anime_media_edit_error_saving to e
                }
            }
        }
    }
}
