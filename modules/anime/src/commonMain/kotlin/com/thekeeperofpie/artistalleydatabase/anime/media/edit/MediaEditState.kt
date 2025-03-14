package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anilist.data.fragment.MediaDetailsListEntry
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.anilist.data.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.utils.SimpleResult
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

@Stable
class MediaEditState {
    var initialParams by mutableStateOf<InitialParams?>(null)
    var scoreFormat by mutableStateOf(ScoreFormat.POINT_100)
    var status by mutableStateOf<MediaListStatus?>(null)
    var score by mutableStateOf("")
    var progress by mutableStateOf("")
    var progressVolumes by mutableStateOf("")
    var repeat by mutableStateOf("")
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)
    var priority by mutableStateOf("")
    var private by mutableStateOf(false)
    var hiddenFromStatusLists by mutableStateOf(false)
    var updatedAt by mutableStateOf<Long?>(null)
    var createdAt by mutableStateOf<Long?>(null)
    var notes by mutableStateOf("")

    var deleting by mutableStateOf(false)
    var saving by mutableStateOf(false)
    var error by mutableStateOf<Pair<StringResource, Throwable?>?>(null)

    var showConfirmClose by mutableStateOf(false)
    var hasConfirmedClose by mutableStateOf(false)

    var showing by mutableStateOf(false)

    fun hasChanged() = status != initialParams?.status
            || fieldAsInt(progress) != (initialParams?.progress ?: 0)
            || fieldAsInt(repeat) != (initialParams?.repeat ?: 0)
            || fieldAsInt(priority) != (initialParams?.priority ?: 0)
            || scoreRaw().getOrNull() != (initialParams?.score?.toInt() ?: 0)
            || startDate != MediaUtils.parseLocalDate(
        year = initialParams?.startedAtYear,
        month = initialParams?.startedAtMonth,
        dayOfMonth = initialParams?.startedAtDay
    )
            || endDate != MediaUtils.parseLocalDate(
        year = initialParams?.completedAtYear,
        month = initialParams?.completedAtMonth,
        dayOfMonth = initialParams?.completedAtDay
    )
            || private != (initialParams?.private == true)
            || hiddenFromStatusLists != (initialParams?.hiddenFromStatusLists == true)
            || notes != (initialParams?.notes.orEmpty())

    fun scoreRaw(): SimpleResult<Int> {
        val score = score
        if (score.isBlank()) return SimpleResult.Success(0)

        return SimpleResult.successIfNotNull(
            when (scoreFormat) {
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
                ScoreFormat.UNKNOWN__,
                    -> score.toIntOrNull()
            }
        )
    }

    private fun fieldAsInt(field: String): Int? {
        if (field.isBlank()) return 0
        return field.toIntOrNull()
    }

    data class InitialParams(
        val id: String?,
        val mediaId: String?,
        val coverImage: String?,
        val title: String?,
        val status: MediaListStatus?,
        val score: Double?,
        val progress: Int?,
        val progressVolumes: Int?,
        val repeat: Int?,
        val priority: Int?,
        val private: Boolean?,
        val hiddenFromStatusLists: Boolean?,
        val notes: String?,
        val startedAtYear: Int?,
        val startedAtMonth: Int?,
        val startedAtDay: Int?,
        val completedAtYear: Int?,
        val completedAtMonth: Int?,
        val completedAtDay: Int?,
        val updatedAt: Int?,
        val createdAt: Int?,
        val mediaType: MediaType?,
        val maxProgress: Int?,
        val maxProgressVolumes: Int?,
        val loading: Boolean,
    ) {
        constructor(
            mediaId: String?,
            coverImage: String?,
            title: String?,
            mediaListEntry: MediaDetailsListEntry?,
            mediaType: MediaType?,
            maxProgress: Int?,
            maxProgressVolumes: Int?,
            loading: Boolean,
        ) : this(
            id = mediaListEntry?.id?.toString(),
            mediaId = mediaId,
            coverImage = coverImage,
            title = title,
            status = mediaListEntry?.status,
            score = mediaListEntry?.score,
            progress = mediaListEntry?.progress,
            progressVolumes = mediaListEntry?.progressVolumes,
            repeat = mediaListEntry?.repeat,
            priority = mediaListEntry?.priority,
            private = mediaListEntry?.private,
            hiddenFromStatusLists = mediaListEntry?.hiddenFromStatusLists,
            notes = mediaListEntry?.notes,
            startedAtYear = mediaListEntry?.startedAt?.year,
            startedAtMonth = mediaListEntry?.startedAt?.month,
            startedAtDay = mediaListEntry?.startedAt?.day,
            completedAtYear = mediaListEntry?.completedAt?.year,
            completedAtMonth = mediaListEntry?.completedAt?.month,
            completedAtDay = mediaListEntry?.completedAt?.day,
            updatedAt = mediaListEntry?.updatedAt,
            createdAt = mediaListEntry?.createdAt,
            mediaType = mediaType,
            maxProgress = maxProgress,
            maxProgressVolumes = maxProgressVolumes,
            loading = loading,
        )
    }
}

