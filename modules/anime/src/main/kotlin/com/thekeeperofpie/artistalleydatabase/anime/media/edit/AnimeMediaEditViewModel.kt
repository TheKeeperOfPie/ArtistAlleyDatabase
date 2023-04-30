package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.anilist.type.MediaListStatus
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class AnimeMediaEditViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    // TODO: Store in saved state
    private lateinit var initialData: MediaEditData

    val isInitialized get() = ::initialData.isInitialized

    val type get() = initialData.type
    val progressMax get() = initialData.progressMax

    val scoreFormat = aniListApi.authedUser
        .map { it?.mediaListOptions?.scoreFormat ?: ScoreFormat.POINT_100 }

    var id by mutableStateOf<String?>(null)
    var status by mutableStateOf<MediaListStatus?>(null)
    var score by mutableStateOf("")
    var progress by mutableStateOf("")
    var repeat by mutableStateOf("")
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)
    var priority by mutableStateOf("")
    var private by mutableStateOf(false)
    var updatedAt by mutableStateOf<Long?>(null)
    var createdAt by mutableStateOf<Long?>(null)

    var deleting by mutableStateOf(false)
    var saving by mutableStateOf(false)
    var errorRes by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun initialize(data: MediaEditData) {
        if (isInitialized) return
        initialData = data
        id = initialData.id
        status = initialData.status
        progress = initialData.progress?.toString().orEmpty()
        repeat = initialData.repeat?.toString().orEmpty()
        startDate = initialData.startedAt
        endDate = initialData.completedAt
        priority = initialData.priority?.toString().orEmpty()
        private = initialData.private
        updatedAt = initialData.updatedAt
        createdAt = initialData.createdAt

        viewModelScope.launch(Dispatchers.IO) {
            scoreFormat.collectLatest { format ->
                data.scoreRaw?.let {
                    val scoreString = when (format) {
                        ScoreFormat.POINT_10_DECIMAL -> String.format("%.1f", it / 10f)
                        ScoreFormat.POINT_10 -> (it / 10).toString()
                        ScoreFormat.POINT_100,
                        ScoreFormat.POINT_5,
                        ScoreFormat.POINT_3,
                        ScoreFormat.UNKNOWN__ -> it.toString()
                    }

                    withContext(CustomDispatchers.Main) {
                        score = scoreString
                    }
                }
            }
        }
    }

    fun onDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
        }

        if (start) {
            startDate = selectedDate
        } else {
            endDate = selectedDate
        }
    }

    fun onClickDelete(navController: NavController) {
        if (saving || deleting) return
        deleting = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                aniListApi.deleteMediaListEntry(initialData.id!!)
                withContext(CustomDispatchers.Main) {
                    AnimeMediaEditProxy.editResults.value = initialData.mediaId to null
                    navController.popBackStack()
                }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    deleting = false
                    errorRes = R.string.anime_media_edit_error_deleting to e
                }
            }
        }
    }

    fun onClickSave(navController: NavController) {
        if (status == null) {
            saving = false
            errorRes = R.string.anime_media_edit_error_invalid_status to null
            return
        }

        if (saving || deleting) return
        saving = true

        viewModelScope.launch(CustomDispatchers.IO) {
            fun validateFieldAsInt(field: String): Int? {
                if (field.isBlank()) return 0
                return field.toIntOrNull()
            }

            val scoreRaw = if (score.isBlank()) {
                0
            } else when (scoreFormat.first()) {
                ScoreFormat.POINT_10_DECIMAL -> {
                    val scoreAsFloat = score.toFloatOrNull()?.let {
                        (it * 10).takeIf { it < 101f }
                    }
                    if (scoreAsFloat == null) {
                        withContext(CustomDispatchers.Main) {
                            saving = false
                            errorRes = R.string.anime_media_edit_error_invalid_score to null
                        }
                        return@launch
                    } else scoreAsFloat.toInt().coerceAtMost(100)
                }
                ScoreFormat.POINT_100,
                ScoreFormat.POINT_10,
                ScoreFormat.POINT_5,
                ScoreFormat.POINT_3,
                ScoreFormat.UNKNOWN__ -> validateFieldAsInt(score)
            }

            if (scoreRaw == null) {
                withContext(CustomDispatchers.Main) {
                    saving = false
                    errorRes = R.string.anime_media_edit_error_invalid_score to null
                }
                return@launch
            }

            val progressAsInt = validateFieldAsInt(progress)
            if (progressAsInt == null) {
                withContext(CustomDispatchers.Main) {
                    saving = false
                    errorRes = R.string.anime_media_edit_error_invalid_progress to null
                }
                return@launch
            }

            val repeatAsInt = validateFieldAsInt(repeat)
            if (repeatAsInt == null) {
                withContext(CustomDispatchers.Main) {
                    saving = false
                    errorRes = R.string.anime_media_edit_error_invalid_repeat to null
                }
                return@launch
            }

            val priorityAsInt = validateFieldAsInt(priority)
            if (priorityAsInt == null) {
                withContext(CustomDispatchers.Main) {
                    saving = false
                    errorRes = R.string.anime_media_edit_error_invalid_priority to null
                }
                return@launch
            }

            try {
                val result = aniListApi.saveMediaListEntry(
                    id = initialData.id,
                    mediaId = initialData.mediaId,
                    type = initialData.type,
                    status = status,
                    scoreRaw = scoreRaw,
                    progress = progressAsInt,
                    repeat = repeatAsInt,
                    priority = priorityAsInt,
                    private = private,
                    startedAt = startDate,
                    completedAt = endDate,
                    hiddenFromStatusLists = null,
                )
                withContext(CustomDispatchers.Main) {
                    AnimeMediaEditProxy.editResults.value = initialData.mediaId to result
                    navController.popBackStack()
                }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    saving = false
                    errorRes = R.string.anime_media_edit_error_saving to e
                }
            }
        }
    }
}
