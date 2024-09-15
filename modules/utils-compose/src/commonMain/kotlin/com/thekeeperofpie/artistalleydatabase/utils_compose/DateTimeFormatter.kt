package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.CompositionLocal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

expect val LocalDateTimeFormatter: CompositionLocal<DateTimeFormatter>

expect class DateTimeFormatter {
    fun formatDateTime(year: Int?, month: Int?, dayOfMonth: Int?): String?
    fun formatEntryDateTime(timeInMillis: Long): String
    fun formatAiringAt(instant: Instant, showDate: Boolean = true): String
    fun formatRemainingTime(instant: Instant): CharSequence
    fun formatShortDay(localDate: LocalDate): String
    fun formatShortWeekday(localDate: LocalDate): String
}
