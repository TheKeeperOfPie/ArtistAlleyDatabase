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

    var status by mutableStateOf<MediaListStatus?>(null)
    var score by mutableStateOf("")
    var progress by mutableStateOf("")
    var repeat by mutableStateOf("")
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)

    var saving by mutableStateOf(false)
    var errorRes by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun initialize(data: MediaEditData) {
        if (isInitialized) return
        initialData = data
        status = initialData.status
        progress = initialData.progress?.toString().orEmpty()
        repeat = initialData.repeat?.toString().orEmpty()
        startDate = initialData.startedAt
        endDate = initialData.completedAt

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

    fun onClickSave(navController: NavController) {
        if (saving) return
        saving = true

        viewModelScope.launch(CustomDispatchers.IO) {
            val scoreAsInt = score.toIntOrNull()
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
                ScoreFormat.UNKNOWN__ -> {
                    if (score.isNotBlank() && scoreAsInt == null) {
                        withContext(CustomDispatchers.Main) {
                            saving = false
                            errorRes = R.string.anime_media_edit_error_invalid_score to null
                        }
                        return@launch
                    } else scoreAsInt
                }
            }

            val progressAsInt = progress.toIntOrNull()
            if (progress.isNotBlank() && progressAsInt == null) {
                withContext(CustomDispatchers.Main) {
                    saving = false
                    errorRes = R.string.anime_media_edit_error_invalid_progress to null
                }
                return@launch
            }

            try {
                val result = aniListApi.saveMediaEntry(
                    id = initialData.id,
                    mediaId = initialData.mediaId,
                    type = initialData.type,
                    status = status,
                    scoreRaw = scoreRaw,
                    progress = progressAsInt,
                    repeat = null, // TODO
                    priority = null,
                    private = null,
                    startedAt = startDate,
                    completedAt = endDate,
                    hiddenFromStatusLists = null,
                )
                withContext(CustomDispatchers.Main) {
                    AnimeMediaEditProxy.editResults.value = result
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
