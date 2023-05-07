package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.type.MediaListStatus
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import java.time.LocalDate

class MediaEditData {
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

    var showing by mutableStateOf(false)
    var deleting by mutableStateOf(false)
    var saving by mutableStateOf(false)
    var errorRes by mutableStateOf<Pair<Int, Exception?>?>(null)

    var showConfirmClose by mutableStateOf(false)

    fun isEqualTo(listEntry: MediaDetailsListEntry?, scoreFormat: ScoreFormat): Boolean {
        return status == listEntry?.status
                && fieldAsInt(progress) == (listEntry?.progress ?: 0)
                && fieldAsInt(repeat) == (listEntry?.repeat ?: 0)
                && fieldAsInt(priority) == (listEntry?.priority ?: 0)
                && scoreRaw(scoreFormat) == (listEntry?.score?.toInt() ?: 0)
                && startDate == MediaUtils.parseLocalDate(listEntry?.startedAt)
                && endDate == MediaUtils.parseLocalDate(listEntry?.completedAt)
                && private == (listEntry?.private ?: false)
    }

    fun scoreRaw(scoreFormat: ScoreFormat): Int? {
        val score = score
        if (score.isBlank()) return 0

        return when (scoreFormat) {
            ScoreFormat.POINT_10_DECIMAL -> {
                val scoreAsFloat = score.toFloatOrNull()?.let {
                    (it * 10).takeIf { it < 101f }
                }
                scoreAsFloat?.toInt()?.coerceAtMost(100)
            }
            ScoreFormat.POINT_10 -> score.toIntOrNull()?.let { it * 10 }
            ScoreFormat.POINT_100,
            ScoreFormat.POINT_5,
            ScoreFormat.POINT_3,
            ScoreFormat.UNKNOWN__ -> score.toIntOrNull()
        }
    }

    private fun fieldAsInt(field: String): Int? {
        if (field.isBlank()) return 0
        return field.toIntOrNull()
    }
}

