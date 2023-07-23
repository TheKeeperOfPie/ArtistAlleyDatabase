package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.type.MediaSeason
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed interface AiringDate {

    data class Basic(
        val season: MediaSeason? = null,
        val seasonYear: String = "",
    ) : AiringDate

    data class Advanced(
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
    ) : AiringDate {

        fun summaryText(): String? {
            val startDateString =
                startDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            val endDateString =
                endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

            return when {
                startDateString != null && endDateString != null -> {
                    if (startDateString == endDateString) {
                        startDateString
                    } else {
                        "$startDateString - $endDateString"
                    }
                }
                startDateString != null -> "≥ $startDateString"
                endDateString != null -> "≤ $endDateString"
                else -> null
            }
        }
    }
}
