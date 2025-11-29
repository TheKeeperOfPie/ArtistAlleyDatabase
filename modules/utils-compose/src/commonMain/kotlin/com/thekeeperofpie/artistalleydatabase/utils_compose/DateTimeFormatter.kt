package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.CompositionLocal
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

expect val LocalDateTimeFormatter: CompositionLocal<DateTimeFormatter>

expect class DateTimeFormatter {
    fun formatDateTime(year: Int?, month: Int?, dayOfMonth: Int?): String?
    fun formatEntryDateTime(timeInMillis: Long): String
    fun formatAiringAt(instant: Instant, showDate: Boolean = true): String
    fun formatRemainingTime(instant: Instant): CharSequence
    fun formatShortDay(localDate: LocalDate): String
    fun formatShortWeekday(localDate: LocalDate): String
    fun formatSubtitleMonthYear(year: Int?, month: Int?): String?
    fun formatDateTime(instant: Instant): String
}
